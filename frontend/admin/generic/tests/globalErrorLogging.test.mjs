import assert from "node:assert/strict";
import test from "node:test";

import { createLogger, installGlobalErrorLogging } from "../js/logger.js";

class TestEventTarget {
    constructor(pathname = "/admin") {
        this.location = { pathname };
        this.listenerListByEventMap = new Map();
        this.addCountByEventMap = new Map();
    }

    addEventListener(eventName, listener) {
        const listenerList = this.listenerListByEventMap.get(eventName) ?? [];
        listenerList.push(listener);
        this.listenerListByEventMap.set(eventName, listenerList);
        this.addCountByEventMap.set(eventName, (this.addCountByEventMap.get(eventName) ?? 0) + 1);
    }

    removeEventListener(eventName, listener) {
        const listenerList = this.listenerListByEventMap.get(eventName) ?? [];
        this.listenerListByEventMap.set(
                eventName,
                listenerList.filter((candidate) => candidate !== listener)
        );
    }

    dispatch(eventName, event = {}) {
        for (const listener of this.listenerListByEventMap.get(eventName) ?? []) {
            listener(event);
        }
    }
}

function createCapturedLogger() {
    const recordList = [];
    const logger = createLogger({
        environment: "development",
        consoleTarget: {
            error(record) {
                recordList.push(record);
            },
        },
        now: () => "2026-08-11T12:34:56.000Z",
    });
    return { logger, recordList };
}

test("browser error events produce one stable sanitized event", () => {
    const eventTarget = new TestEventTarget("/admin?token=route-marker");
    const { logger, recordList } = createCapturedLogger();
    const error = new Error("email=guest@example.com");
    error.stack = "failure\npassword=marker-password https://example.com/source.js?token=query-marker";

    assert.equal(logger.installGlobalErrorLogging({ eventTarget }), true);
    eventTarget.dispatch("error", {
        message: "email=guest@example.com\r\nforged=true",
        error,
    });

    assert.equal(recordList.length, 1);
    assert.equal(recordList[0].event, "client.unhandled_error");
    assert.equal(recordList[0].level, "ERROR");
    assert.equal(recordList[0].route, "/admin");
    const serializedRecord = JSON.stringify(recordList[0]);
    assert.equal(serializedRecord.includes("guest@example.com"), false);
    assert.equal(serializedRecord.includes("marker-password"), false);
    assert.equal(serializedRecord.includes("query-marker"), false);
    assert.equal(serializedRecord.includes("route-marker"), false);
    assert.equal(serializedRecord.includes("\\r"), false);
    assert.equal(serializedRecord.includes("\\n"), false);
});

test("unhandled rejections safely normalize Error string and unknown reasons", () => {
    const eventTarget = new TestEventTarget();
    const { logger, recordList } = createCapturedLogger();
    const error = new Error("token=marker-token");
    error.stack = "rejected cookie=marker-cookie";
    const unknownReason = {
        nested: { password: "must-not-be-serialized" },
        toJSON() {
            throw new Error("must not be called");
        },
    };

    installGlobalErrorLogging({ eventTarget, loggerInstance: logger });
    eventTarget.dispatch("unhandledrejection", { reason: error });
    eventTarget.dispatch("unhandledrejection", { reason: "phone=11987654321" });
    eventTarget.dispatch("unhandledrejection", { reason: unknownReason });

    assert.equal(recordList.length, 3);
    assert.deepEqual(recordList.map(({ event }) => event), [
        "client.unhandled_rejection",
        "client.unhandled_rejection",
        "client.unhandled_rejection",
    ]);
    assert.equal(recordList[2].message, "Unhandled promise rejection.");
    const serializedRecordList = JSON.stringify(recordList);
    assert.equal(serializedRecordList.includes("marker-token"), false);
    assert.equal(serializedRecordList.includes("marker-cookie"), false);
    assert.equal(serializedRecordList.includes("11987654321"), false);
    assert.equal(serializedRecordList.includes("must-not-be-serialized"), false);
});

test("repeated installation registers each listener exactly once", () => {
    const eventTarget = new TestEventTarget();
    const { logger, recordList } = createCapturedLogger();

    assert.equal(installGlobalErrorLogging({ eventTarget, loggerInstance: logger }), true);
    assert.equal(installGlobalErrorLogging({ eventTarget, loggerInstance: logger }), false);
    assert.equal(eventTarget.addCountByEventMap.get("error"), 1);
    assert.equal(eventTarget.addCountByEventMap.get("unhandledrejection"), 1);

    eventTarget.dispatch("error", { message: "failure" });
    assert.equal(recordList.length, 1);
});

test("listener and logger failures do not propagate or recursively report", () => {
    const eventTarget = new TestEventTarget();
    let loggerCalls = 0;
    const failingLogger = {
        error() {
            loggerCalls++;
            eventTarget.dispatch("error", { message: "recursive" });
            throw new Error("logger unavailable");
        },
    };

    assert.equal(installGlobalErrorLogging({ eventTarget, loggerInstance: failingLogger }), true);
    assert.doesNotThrow(() => eventTarget.dispatch("error", { message: "original" }));
    assert.equal(loggerCalls, 1);
});

test("failed or invalid listener installation remains a safe no-op", () => {
    const invalidTarget = {};
    const partiallyFailingTarget = new TestEventTarget();
    partiallyFailingTarget.addEventListener = function (eventName, listener) {
        if (eventName === "unhandledrejection") {
            throw new Error("listener unavailable");
        }
        TestEventTarget.prototype.addEventListener.call(this, eventName, listener);
    };

    assert.equal(installGlobalErrorLogging({ eventTarget: invalidTarget, loggerInstance: { error() {} } }), false);
    assert.doesNotThrow(() => installGlobalErrorLogging({
        eventTarget: partiallyFailingTarget,
        loggerInstance: { error() {} },
    }));
    assert.equal(partiallyFailingTarget.listenerListByEventMap.get("error")?.length ?? 0, 0);
});
