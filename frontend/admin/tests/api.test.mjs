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

const { ApiError, findAllDataProcessingOperations, findMetricsSummary, login } = await import("../js/api.js?v=api-error-tests");

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
