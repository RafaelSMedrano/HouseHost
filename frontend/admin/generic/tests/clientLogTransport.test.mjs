import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

import { createClientLogTransport } from "../js/clientLogTransport.js";

const VALID_RECORD = Object.freeze({
    level: "WARN",
    event: "client.warning",
    message: "Safe warning",
    correlationId: "client-123",
    route: "/rooms",
    method: "GET",
    status: 400,
    durationMs: 12,
    clientTimestamp: "2026-08-11T12:34:56.000Z",
});

class TestEventTarget {
    constructor() {
        this.listenerListByEventMap = new Map();
        this.addCountByEventMap = new Map();
    }

    addEventListener(eventName, listener) {
        const listenerList = this.listenerListByEventMap.get(eventName) ?? [];
        listenerList.push(listener);
        this.listenerListByEventMap.set(eventName, listenerList);
        this.addCountByEventMap.set(eventName, (this.addCountByEventMap.get(eventName) ?? 0) + 1);
    }

    dispatch(eventName) {
        for (const listener of this.listenerListByEventMap.get(eventName) ?? []) {
            listener({ type: eventName });
        }
    }
}

test("transport submits only sanitized WARN and ERROR with protected authentication", async () => {
    const requestList = [];
    const transport = createClientLogTransport({
        endpoint: "https://api.example.test/client-logs",
        getAuthToken: () => "marker-auth-token",
        fetchTarget: async (url, options) => {
            requestList.push({ url, options });
            return new Response(null, { status: 202 });
        },
    });

    assert.equal(await transport({ ...VALID_RECORD, level: "INFO" }), false);
    assert.equal(await transport({
        ...VALID_RECORD,
        message: "password=marker-password email=guest@example.com",
        route: "/rooms?token=marker-query",
        unknown: "must-not-be-sent",
    }), true);

    assert.equal(requestList.length, 1);
    assert.equal(requestList[0].url, "https://api.example.test/client-logs");
    assert.equal(requestList[0].options.headers.Authorization, "Bearer marker-auth-token");
    assert.equal(requestList[0].url.includes("marker-auth-token"), false);
    const body = requestList[0].options.body;
    assert.equal(body.includes("marker-auth-token"), false);
    assert.equal(body.includes("marker-password"), false);
    assert.equal(body.includes("guest@example.com"), false);
    assert.equal(body.includes("marker-query"), false);
    assert.equal(body.includes("must-not-be-sent"), false);
    assert.equal(JSON.parse(body).route, "/rooms");
});

test("anonymous records are dropped without network delivery", async () => {
    let fetchCalls = 0;
    const transport = createClientLogTransport({
        getAuthToken: () => null,
        fetchTarget: async () => {
            fetchCalls++;
            return new Response(null, { status: 202 });
        },
    });

    assert.equal(await transport(VALID_RECORD), false);
    assert.equal(fetchCalls, 0);
    assert.deepEqual(transport.getState(), {
        queueSize: 0,
        deduplicationSize: 0,
        droppedCount: 1,
        draining: false,
    });
});

test("retryable failures use a bounded retry ceiling and never reject", async () => {
    let fetchCalls = 0;
    const transport = createClientLogTransport({
        getAuthToken: () => "valid-token",
        maxRetries: 2,
        schedule: async () => {},
        fetchTarget: async () => {
            fetchCalls++;
            if (fetchCalls < 3) {
                throw new Error("client-log endpoint unavailable");
            }
            return new Response(null, { status: 202 });
        },
    });

    await assert.doesNotReject(() => transport(VALID_RECORD));
    assert.equal(fetchCalls, 3);
    assert.equal(transport.getState().queueSize, 0);
    assert.equal(transport.getState().droppedCount, 0);
});

test("queue deduplication and internal state remain explicitly bounded", async () => {
    const transport = createClientLogTransport({
        autoFlush: false,
        getAuthToken: () => "valid-token",
        queueCapacity: 2,
        deduplicationCapacity: 2,
        deduplicationWindowMs: 5000,
        now: () => 100,
    });

    assert.equal(await transport(VALID_RECORD), true);
    assert.equal(await transport(VALID_RECORD), false);
    assert.equal(await transport({ ...VALID_RECORD, event: "client.second" }), true);
    assert.equal(await transport({ ...VALID_RECORD, event: "client.third" }), false);

    assert.deepEqual(transport.getState(), {
        queueSize: 2,
        deduplicationSize: 2,
        droppedCount: 2,
        draining: false,
    });

    transport.discard();
    assert.equal(transport.getState().queueSize, 0);
    assert.equal(transport.getState().droppedCount, 4);
});

test("page lifecycle flush keeps authentication headers or discards without a token", async () => {
    const eventTarget = new TestEventTarget();
    const requestList = [];
    let token = "valid-token";
    const transport = createClientLogTransport({
        autoFlush: false,
        getAuthToken: () => token,
        fetchTarget: async (url, options) => {
            requestList.push({ url, options });
            return new Response(null, { status: 202 });
        },
    });

    assert.equal(transport.installLifecycleFlush(eventTarget), true);
    assert.equal(transport.installLifecycleFlush(eventTarget), false);
    assert.equal(eventTarget.addCountByEventMap.get("pagehide"), 1);

    await transport(VALID_RECORD);
    eventTarget.dispatch("pagehide");
    await new Promise((resolve) => setImmediate(resolve));
    assert.equal(requestList.length, 1);
    assert.equal(requestList[0].options.keepalive, true);
    assert.equal(requestList[0].options.headers.Authorization, "Bearer valid-token");

    await transport({ ...VALID_RECORD, event: "client.pending" });
    token = null;
    eventTarget.dispatch("pagehide");
    assert.equal(transport.getState().queueSize, 0);
});

test("client-log delivery is independent from instrumented apiRequest", async () => {
    const source = await readFile(
            new URL("../js/clientLogTransport.js", import.meta.url),
            "utf8"
    );

    assert.equal(source.includes("api.js"), false);
    assert.equal(source.includes("apiRequest"), false);
    assert.equal(source.includes("sendBeacon"), false);
});
