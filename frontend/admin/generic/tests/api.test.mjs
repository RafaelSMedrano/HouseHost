import assert from "node:assert/strict";
import test from "node:test";

const storageMap = new Map();
globalThis.localStorage = {
    getItem(key) {
        return storageMap.get(key) ?? null;
    },
    setItem(key, value) {
        storageMap.set(key, value);
    },
    removeItem(key) {
        storageMap.delete(key);
    },
};

const {
    ApiError,
    findAllDataProcessingOperations,
    findMetricsSummary,
    getApiResponseDiagnostics,
    login,
} = await import("../js/api.js?v=api-error-tests");
const { configureLoggerTransport } = await import("../js/logger.js");

test("every API request sends correlation and exposes authoritative response diagnostics", async () => {
    storageMap.clear();
    let requestCorrelationId;
    globalThis.fetch = async (url, options) => {
        requestCorrelationId = options.headers["X-Correlation-ID"];
        return new Response(
                JSON.stringify({ status: "success", data: { rooms: 3 } }),
                {
                    status: 200,
                    headers: { "X-Correlation-ID": "server-correlation-123" },
                }
        );
    };

    const payload = await findMetricsSummary();
    const diagnostics = getApiResponseDiagnostics(payload);

    assert.match(requestCorrelationId, /^[A-Za-z0-9][A-Za-z0-9._:-]{0,63}$/);
    assert.equal(diagnostics.correlationId, "server-correlation-123");
    assert.equal(diagnostics.method, "GET");
    assert.equal(diagnostics.route, "/metrics/summary");
    assert.equal(Number.isInteger(diagnostics.durationMs), true);
    assert.equal(diagnostics.durationMs >= 0, true);
});

test("HTTP failures use server correlation and log only safe request diagnostics", async () => {
    storageMap.set("househost_token", "marker-authorization-token");
    const logRecordList = [];
    configureLoggerTransport((record) => logRecordList.push(record));
    globalThis.fetch = async () => new Response(
            JSON.stringify({
                message: "password=marker-password email=guest@example.com documentNumber=12345678910 phone=11987654321",
            }),
            {
                status: 500,
                headers: { "X-Correlation-ID": "server-failure-456" },
            }
    );

    try {
        await assert.rejects(
                () => findAllDataProcessingOperations({ status: "token=marker-query-token" }),
                (error) => {
                    assert.equal(error instanceof ApiError, true);
                    assert.equal(error.status, 500);
                    assert.equal(error.correlationId, "server-failure-456");
                    assert.equal(error.method, "GET");
                    assert.equal(error.route, "/data-processing-operations");
                    assert.equal(Number.isInteger(error.durationMs), true);
                    return true;
                }
        );

        assert.equal(logRecordList.length, 1);
        assert.equal(logRecordList[0].event, "api.request_failed");
        assert.equal(logRecordList[0].level, "ERROR");
        assert.equal(logRecordList[0].correlationId, "server-failure-456");
        assert.equal(logRecordList[0].route, "/data-processing-operations");
        const serializedLogRecord = JSON.stringify(logRecordList[0]);
        for (const marker of [
            "marker-authorization-token",
            "marker-password",
            "guest@example.com",
            "12345678910",
            "11987654321",
            "marker-query-token",
        ]) {
            assert.equal(serializedLogRecord.includes(marker), false, marker);
        }
    } finally {
        configureLoggerTransport(null);
    }
});

test("network failures are safe while expected cancellation is not logged", async () => {
    storageMap.clear();
    const logRecordList = [];
    configureLoggerTransport((record) => logRecordList.push(record));
    globalThis.fetch = async () => {
        throw new Error("password=marker-network-password email=network@example.com");
    };

    try {
        await assert.rejects(() => login("login@example.com", "marker-login-password"));
        assert.equal(logRecordList.length, 1);
        assert.equal(logRecordList[0].event, "api.network_failed");
        const serializedLogRecord = JSON.stringify(logRecordList[0]);
        assert.equal(serializedLogRecord.includes("marker-network-password"), false);
        assert.equal(serializedLogRecord.includes("network@example.com"), false);
        assert.equal(serializedLogRecord.includes("marker-login-password"), false);
        assert.equal(serializedLogRecord.includes("login@example.com"), false);

        logRecordList.length = 0;
        globalThis.fetch = async () => {
            throw new DOMException("Aborted", "AbortError");
        };
        await assert.rejects(() => login("ignored@example.com", "ignored"));
        assert.equal(logRecordList.length, 0);
    } finally {
        configureLoggerTransport(null);
    }
});

test("login preserves 401 status and clears an existing session", async () => {
    storageMap.set("househost_token", "stale-token");
    globalThis.fetch = async () => new Response(
            JSON.stringify({ message: "invalid" }),
            { status: 401 }
    );

    await assert.rejects(
            () => login("admin@example.com", "wrong-password"),
            (error) => {
                assert.equal(error instanceof ApiError, true);
                assert.equal(error.status, 401);
                assert.equal(storageMap.has("househost_token"), false);
                return true;
            }
    );
});

test("login preserves 429 status and valid Retry-After seconds", async () => {
    globalThis.fetch = async () => new Response(
            JSON.stringify({ message: "restricted" }),
            {
                status: 429,
                headers: {
                    "Content-Type": "application/json",
                    "Retry-After": "37",
                },
            }
    );

    await assert.rejects(
            () => login("admin@example.com", "secret"),
            (error) => {
                assert.equal(error instanceof ApiError, true);
                assert.equal(error.status, 429);
                assert.equal(error.retryAfterSeconds, 37);
                return true;
            }
    );
});

test("login rejects malformed Retry-After without inventing a duration", async () => {
    globalThis.fetch = async () => new Response(
            JSON.stringify({ message: "restricted" }),
            {
                status: 429,
                headers: { "Retry-After": "tomorrow" },
            }
    );

    await assert.rejects(
            () => login("admin@example.com", "secret"),
            (error) => {
                assert.equal(error.retryAfterSeconds, null);
                return true;
            }
    );
});

test("login preserves service-unavailable status", async () => {
    globalThis.fetch = async () => new Response(
            JSON.stringify({ message: "unavailable" }),
            { status: 503 }
    );

    await assert.rejects(
            () => login("admin@example.com", "secret"),
            (error) => {
                assert.equal(error instanceof ApiError, true);
                assert.equal(error.status, 503);
                return true;
            }
    );
});

test("authenticated 401 clears the session and announces its expiration", async () => {
    storageMap.set("househost_token", "expired-token");
    const eventTarget = new EventTarget();
    globalThis.addEventListener = eventTarget.addEventListener.bind(eventTarget);
    globalThis.removeEventListener = eventTarget.removeEventListener.bind(eventTarget);
    globalThis.dispatchEvent = eventTarget.dispatchEvent.bind(eventTarget);
    let expirationEvents = 0;
    const onSessionExpired = () => expirationEvents++;
    globalThis.addEventListener("househost:session-expired", onSessionExpired);
    globalThis.fetch = async () => new Response(
            JSON.stringify({ message: "Autenticacao obrigatoria." }),
            { status: 401 }
    );

    try {
        await assert.rejects(
                () => findAllDataProcessingOperations(),
                (error) => error instanceof ApiError && error.status === 401
        );
        assert.equal(storageMap.has("househost_token"), false);
        assert.equal(expirationEvents, 1);
    } finally {
        globalThis.removeEventListener("househost:session-expired", onSessionExpired);
        delete globalThis.addEventListener;
        delete globalThis.removeEventListener;
        delete globalThis.dispatchEvent;
    }
});

test("401 without a token does not announce another session expiration", async () => {
    storageMap.clear();
    const eventTarget = new EventTarget();
    globalThis.dispatchEvent = eventTarget.dispatchEvent.bind(eventTarget);
    let expirationEvents = 0;
    eventTarget.addEventListener("househost:session-expired", () => expirationEvents++);
    globalThis.fetch = async () => new Response(
            JSON.stringify({ message: "Autenticacao obrigatoria." }),
            { status: 401 }
    );

    try {
        await assert.rejects(
                () => findMetricsSummary(),
                (error) => error instanceof ApiError && error.status === 401
        );
        assert.equal(expirationEvents, 0);
    } finally {
        delete globalThis.dispatchEvent;
    }
});
