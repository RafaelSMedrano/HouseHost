import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

import { configureLoggerTransport, createLogger, logger } from "../js/logger.js";
import { initializeAdministrativeLogging } from "../js/loggerBootstrap.js";

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

    removeEventListener(eventName, listener) {
        const listenerList = this.listenerListByEventMap.get(eventName) ?? [];
        this.listenerListByEventMap.set(
                eventName,
                listenerList.filter((candidate) => candidate !== listener)
        );
    }
}

test("logging initializes global capture before the stable startup event", () => {
    const callList = [];
    const loggerInstance = {
        installGlobalErrorLogging() {
            callList.push("listeners.installed");
            return true;
        },
        info(event, message) {
            callList.push({ event, message });
            return true;
        },
    };

    const loggingBootstrap = initializeAdministrativeLogging({ loggerInstance, eventTarget: {} });
    assert.deepEqual(callList, ["listeners.installed"]);

    assert.equal(loggingBootstrap.logApplicationStarted(), true);
    assert.deepEqual(callList, [
        "listeners.installed",
        {
            event: "application.started",
            message: "HouseHost iniciado.",
        },
    ]);
});

test("main initializes logging before registering and starting the administrative UI", async () => {
    const mainSource = await readFile(
            new URL("../js/controllers/main.js", import.meta.url),
            "utf8"
    );
    const initializationIndex = mainSource.indexOf("initializeAdministrativeLogging()");
    const domReadyIndex = mainSource.indexOf('document.addEventListener("DOMContentLoaded"');
    const startupLogIndex = mainSource.indexOf("loggingBootstrap.logApplicationStarted()");
    const userLookupIndex = mainSource.indexOf("getStoredUser()");

    assert.ok(initializationIndex >= 0);
    assert.ok(initializationIndex < domReadyIndex);
    assert.ok(startupLogIndex > domReadyIndex);
    assert.ok(startupLogIndex < userLookupIndex);
    assert.equal(mainSource.includes('console.log("HouseHost iniciado.")'), false);
});

test("administrative initialization installs each global listener only once", () => {
    const eventTarget = new TestEventTarget();
    const loggerInstance = createLogger({ consoleTarget: null });

    initializeAdministrativeLogging({ loggerInstance, eventTarget });
    initializeAdministrativeLogging({ loggerInstance, eventTarget });

    assert.equal(eventTarget.addCountByEventMap.get("error"), 1);
    assert.equal(eventTarget.addCountByEventMap.get("unhandledrejection"), 1);
});

test("development emits startup info while production can suppress it", () => {
    const developmentRecordList = [];
    const productionRecordList = [];
    const eventTarget = new TestEventTarget();
    const developmentLogger = createLogger({
        environment: "development",
        consoleTarget: {
            info(record) {
                developmentRecordList.push(record);
            },
        },
    });
    const productionLogger = createLogger({
        environment: "production",
        consoleTarget: {
            info(record) {
                productionRecordList.push(record);
            },
        },
    });

    initializeAdministrativeLogging({
        loggerInstance: developmentLogger,
        eventTarget,
    }).logApplicationStarted();
    initializeAdministrativeLogging({
        loggerInstance: productionLogger,
        eventTarget: new TestEventTarget(),
    }).logApplicationStarted();

    assert.equal(developmentRecordList.length, 1);
    assert.equal(developmentRecordList[0].event, "application.started");
    assert.equal(productionRecordList.length, 0);
});

test("logger and listener initialization failures never interrupt startup", () => {
    let applicationStarted = false;
    const failingLogger = {
        installGlobalErrorLogging() {
            throw new Error("listener setup failed");
        },
        info() {
            throw new Error("console failed");
        },
    };

    assert.doesNotThrow(() => {
        const loggingBootstrap = initializeAdministrativeLogging({
            loggerInstance: failingLogger,
            eventTarget: {},
        });
        loggingBootstrap.logApplicationStarted();
        applicationStarted = true;
    });
    assert.equal(applicationStarted, true);
});

test("default logger connects the authenticated transport and lifecycle flush", () => {
    const eventTarget = new TestEventTarget();
    const recordList = [];
    let lifecycleTarget = null;
    const clientLogTransport = (record) => {
        recordList.push(record);
        return Promise.resolve(true);
    };
    Object.defineProperty(clientLogTransport, "installLifecycleFlush", {
        value(target) {
            lifecycleTarget = target;
            return true;
        },
    });

    try {
        initializeAdministrativeLogging({ eventTarget, clientLogTransport });
        logger.warn("client.bootstrap_warning", "Safe warning.");

        assert.equal(lifecycleTarget, eventTarget);
        assert.equal(recordList.length, 1);
        assert.equal(recordList[0].event, "client.bootstrap_warning");
        assert.equal(recordList[0].level, "WARN");
    } finally {
        configureLoggerTransport(null);
    }
});
