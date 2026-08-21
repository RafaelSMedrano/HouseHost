import assert from "node:assert/strict";
import test from "node:test";

import { createLogger } from "../js/logger.js";

const FIXED_TIME = "2026-08-11T12:34:56.000Z";

function captureConsole() {
    const callList = [];
    return {
        callList,
        consoleTarget: {
            debug(record) {
                callList.push({ method: "debug", record });
            },
            info(record) {
                callList.push({ method: "info", record });
            },
            warn(record) {
                callList.push({ method: "warn", record });
            },
            error(record) {
                callList.push({ method: "error", record });
            },
        },
    };
}

test("development logger emits normalized levels and allowlisted scalar context", () => {
    const { callList, consoleTarget } = captureConsole();
    const logger = createLogger({
        environment: "development",
        consoleTarget,
        now: () => new Date(FIXED_TIME),
    });

    assert.equal(logger.debug("navigation.opened", "Navigation opened", {
        correlationId: "request-123",
        route: "/bookings?guest=private",
        method: "get",
        status: 200,
        durationMs: 12.5,
        arbitrary: "must-not-be-copied",
        nested: { password: "must-not-be-copied" },
    }), true);

    assert.equal(callList.length, 1);
    assert.equal(callList[0].method, "debug");
    assert.deepEqual(callList[0].record, {
        level: "DEBUG",
        event: "navigation.opened",
        message: "Navigation opened",
        correlationId: "request-123",
        route: "/bookings",
        method: "GET",
        status: 200,
        durationMs: 13,
        clientTimestamp: FIXED_TIME,
    });
    assert.equal(Object.isFrozen(callList[0].record), true);
});

test("production suppresses debug and info console noise and transports only warn and error", () => {
    const { callList, consoleTarget } = captureConsole();
    const transportRecordList = [];
    const logger = createLogger({
        environment: "production",
        consoleTarget,
        transport: (record) => transportRecordList.push(record),
        now: () => FIXED_TIME,
    });

    logger.debug("logger.debug", "debug");
    logger.info("logger.info", "info");
    logger.warn("logger.warn", "warn");
    logger.error("logger.error", "error", new Error("safe"));

    assert.deepEqual(callList.map(({ method }) => method), ["warn", "error"]);
    assert.deepEqual(transportRecordList.map(({ level }) => level), ["WARN", "ERROR"]);
});

test("sanitizes secrets personal data queries and control characters", () => {
    const { callList, consoleTarget } = captureConsole();
    const logger = createLogger({ environment: "development", consoleTarget });
    const error = new Error("failure");
    error.stack = "stack\nAuthorization=Bearer-marker cookie=marker-cookie cpf=123.456.789-10 eyJabcdefghijk.abcdefghijk.abcdefghijk";

    logger.error(
            "client.failed",
            "line1\r\nline2 password=marker-password email=guest@example.com phone=11987654321 https://example.com/rooms?token=marker-query",
            error,
            { route: "/rooms?documentNumber=marker-document" }
    );

    const serializedRecord = JSON.stringify(callList[0].record);
    for (const marker of [
        "marker-password",
        "guest@example.com",
        "11987654321",
        "Bearer-marker",
        "marker-cookie",
        "123.456.789-10",
        "eyJabcdefghijk",
        "marker-query",
        "marker-document",
    ]) {
        assert.equal(serializedRecord.includes(marker), false, marker);
    }
    assert.equal(serializedRecord.includes("\\r"), false);
    assert.equal(serializedRecord.includes("\\n"), false);
    assert.equal(callList[0].record.route, "/rooms");
    assert.match(callList[0].record.message, /\[REDACTED/);
});

test("enforces event message and stack contracts", () => {
    const { callList, consoleTarget } = captureConsole();
    const logger = createLogger({ environment: "development", consoleTarget });

    assert.equal(logger.info("Invalid Event", "ignored"), false);
    assert.equal(logger.info(`a.${"b".repeat(80)}`, "ignored"), false);
    assert.equal(logger.info("valid.event", { arbitrary: "object" }), false);

    const error = new Error("failure");
    error.stack = "s".repeat(9000);
    logger.error("valid.error", "m".repeat(1200), error);

    assert.equal(callList.length, 1);
    assert.equal(callList[0].record.message.length, 1000);
    assert.equal(callList[0].record.stack.length, 8000);
});

test("ignores arbitrary objects and property getters that fail", () => {
    const { callList, consoleTarget } = captureConsole();
    const context = {
        nested: {
            toJSON() {
                throw new Error("must not be serialized");
            },
        },
    };
    Object.defineProperty(context, "route", {
        get() {
            throw new Error("formatter failure");
        },
    });
    const logger = createLogger({ environment: "development", consoleTarget });

    assert.doesNotThrow(() => logger.warn("context.checked", "safe", context));
    assert.equal(callList.length, 1);
    assert.equal("nested" in callList[0].record, false);
    assert.equal("route" in callList[0].record, false);
});

test("console and transport failures never propagate or recursively log", async () => {
    let logger;
    let transportCalls = 0;
    const consoleTarget = {
        warn() {
            throw new Error("console unavailable");
        },
    };
    const transport = () => {
        transportCalls++;
        logger.warn("transport.recursive", "must be suppressed");
        return Promise.reject(new Error("transport unavailable"));
    };
    logger = createLogger({ environment: "production", consoleTarget, transport });

    assert.doesNotThrow(() => logger.warn("client.warning", "safe"));
    await new Promise((resolve) => setImmediate(resolve));
    assert.equal(transportCalls, 1);
});
