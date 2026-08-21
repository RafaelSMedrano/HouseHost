import { createLogger } from "./logger.js";

const AUTH_TOKEN_KEY = "househost_token";
const DEFAULT_QUEUE_CAPACITY = 20;
const DEFAULT_DEDUPLICATION_CAPACITY = 100;
const DEFAULT_DEDUPLICATION_WINDOW_MS = 5000;
const DEFAULT_MAX_RETRIES = 2;
const DEFAULT_RETRY_DELAY_MS = 250;

export function createClientLogTransport(options = {}) {
    const normalizedOptions = options && typeof options === "object" ? options : {};
    const fetchTarget = normalizedOptions.fetchTarget ?? globalThis.fetch?.bind(globalThis);
    const getAuthToken = typeof normalizedOptions.getAuthToken === "function"
            ? normalizedOptions.getAuthToken
            : readStoredAuthToken;
    const now = typeof normalizedOptions.now === "function" ? normalizedOptions.now : () => Date.now();
    const schedule = typeof normalizedOptions.schedule === "function"
            ? normalizedOptions.schedule
            : (delayMs) => new Promise((resolve) => setTimeout(resolve, delayMs));
    const queueCapacity = positiveIntegerOrDefault(
            normalizedOptions.queueCapacity,
            DEFAULT_QUEUE_CAPACITY
    );
    const deduplicationCapacity = positiveIntegerOrDefault(
            normalizedOptions.deduplicationCapacity,
            DEFAULT_DEDUPLICATION_CAPACITY
    );
    const deduplicationWindowMs = nonNegativeNumberOrDefault(
            normalizedOptions.deduplicationWindowMs,
            DEFAULT_DEDUPLICATION_WINDOW_MS
    );
    const maxRetries = nonNegativeIntegerOrDefault(
            normalizedOptions.maxRetries,
            DEFAULT_MAX_RETRIES
    );
    const retryDelayMs = nonNegativeNumberOrDefault(
            normalizedOptions.retryDelayMs,
            DEFAULT_RETRY_DELAY_MS
    );
    const autoFlush = normalizedOptions.autoFlush !== false;
    const endpoint = normalizedOptions.endpoint ?? resolveClientLogEndpoint();
    const queueList = [];
    const lastSubmissionByDeduplicationKeyMap = new Map();
    const lifecycleTargetSet = new WeakSet();
    let droppedCount = 0;
    let drainingPromise = null;
    let pendingTimestamp = null;
    let sanitizedRecord = null;

    const recordSanitizer = createLogger({
        consoleTarget: null,
        now: () => pendingTimestamp ?? new Date(),
        transport(record) {
            sanitizedRecord = record;
        },
    });

    function transport(record) {
        const token = getTokenSafely(getAuthToken);
        if (!token) {
            droppedCount++;
            return Promise.resolve(false);
        }

        const normalizedRecord = normalizeRecord(record);
        if (!normalizedRecord) {
            droppedCount++;
            return Promise.resolve(false);
        }

        const currentTime = finiteNow(now);
        removeExpiredDeduplicationEntries(currentTime);
        const deduplicationKey = createDeduplicationKey(normalizedRecord);
        const previousSubmission = lastSubmissionByDeduplicationKeyMap.get(deduplicationKey);
        if (previousSubmission !== undefined
                && currentTime - previousSubmission < deduplicationWindowMs) {
            droppedCount++;
            return Promise.resolve(false);
        }
        rememberDeduplicationKey(deduplicationKey, currentTime);

        if (queueList.length >= queueCapacity) {
            droppedCount++;
            return Promise.resolve(false);
        }

        queueList.push({ record: normalizedRecord, retries: 0 });
        return autoFlush ? flush() : Promise.resolve(true);
    }

    function normalizeRecord(record) {
        if (!record || typeof record !== "object") {
            return null;
        }

        pendingTimestamp = typeof record.clientTimestamp === "string"
                ? record.clientTimestamp
                : new Date();
        sanitizedRecord = null;
        const context = {
            correlationId: record.correlationId,
            route: record.route,
            method: record.method,
            status: record.status,
            durationMs: record.durationMs,
        };
        if (record.level === "WARN") {
            recordSanitizer.warn(record.event, record.message, context);
        } else if (record.level === "ERROR") {
            recordSanitizer.error(record.event, record.message, { stack: record.stack }, context);
        }
        pendingTimestamp = null;
        return sanitizedRecord;
    }

    function flush(flushOptions = {}) {
        if (drainingPromise) {
            return drainingPromise;
        }
        const keepalive = flushOptions?.keepalive === true;
        drainingPromise = drainQueue(keepalive).finally(() => {
            drainingPromise = null;
        });
        return drainingPromise;
    }

    async function drainQueue(keepalive) {
        while (queueList.length > 0) {
            const token = getTokenSafely(getAuthToken);
            if (!token || typeof fetchTarget !== "function") {
                droppedCount += queueList.length;
                queueList.length = 0;
                return false;
            }

            const queueItem = queueList[0];
            const outcome = await submitRecord(queueItem.record, token, keepalive);
            if (outcome === "accepted" || outcome === "permanent-failure") {
                queueList.shift();
                if (outcome === "permanent-failure") {
                    droppedCount++;
                }
                continue;
            }

            if (queueItem.retries >= maxRetries) {
                queueList.shift();
                droppedCount++;
                continue;
            }

            queueItem.retries++;
            await schedule(retryDelayMs);
        }
        return true;
    }

    async function submitRecord(record, token, keepalive) {
        try {
            const response = await fetchTarget(endpoint, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(record),
                keepalive,
            });
            if (response?.ok) {
                return "accepted";
            }
            return response?.status === 429 || response?.status >= 500
                    ? "retryable-failure"
                    : "permanent-failure";
        } catch {
            return "retryable-failure";
        }
    }

    function installLifecycleFlush(eventTarget = globalThis) {
        if (!eventTarget
                || (typeof eventTarget !== "object" && typeof eventTarget !== "function")
                || typeof eventTarget.addEventListener !== "function"
                || lifecycleTargetSet.has(eventTarget)) {
            return false;
        }

        try {
            eventTarget.addEventListener("pagehide", () => {
                if (!getTokenSafely(getAuthToken)) {
                    discard();
                    return;
                }
                flush({ keepalive: true }).catch(() => {});
            });
            lifecycleTargetSet.add(eventTarget);
            return true;
        } catch {
            return false;
        }
    }

    function discard() {
        droppedCount += queueList.length;
        queueList.length = 0;
    }

    function getState() {
        return Object.freeze({
            queueSize: queueList.length,
            deduplicationSize: lastSubmissionByDeduplicationKeyMap.size,
            droppedCount,
            draining: drainingPromise !== null,
        });
    }

    function removeExpiredDeduplicationEntries(currentTime) {
        for (const [key, submittedAt] of lastSubmissionByDeduplicationKeyMap) {
            if (currentTime - submittedAt >= deduplicationWindowMs) {
                lastSubmissionByDeduplicationKeyMap.delete(key);
            }
        }
    }

    function rememberDeduplicationKey(key, currentTime) {
        if (lastSubmissionByDeduplicationKeyMap.size >= deduplicationCapacity) {
            const oldestKey = lastSubmissionByDeduplicationKeyMap.keys().next().value;
            lastSubmissionByDeduplicationKeyMap.delete(oldestKey);
        }
        lastSubmissionByDeduplicationKeyMap.set(key, currentTime);
    }

    Object.defineProperties(transport, {
        flush: { value: flush },
        discard: { value: discard },
        getState: { value: getState },
        installLifecycleFlush: { value: installLifecycleFlush },
    });
    return Object.freeze(transport);
}

function createDeduplicationKey(record) {
    return [
        record.level,
        record.event,
        record.message,
        record.correlationId,
        record.route,
        record.method,
        record.status,
    ].join("|");
}

function resolveClientLogEndpoint() {
    const configuredBaseUrl = globalThis.HOUSEHOST_API_BASE_URL;
    if (configuredBaseUrl) {
        return `${String(configuredBaseUrl).replace(/\/$/, "")}/client-logs`;
    }

    const { protocol, hostname, port } = globalThis.location || {};
    const isLocalHost = hostname === "localhost" || hostname === "127.0.0.1";
    if (protocol === "file:" || (isLocalHost && port !== "8080")) {
        return "http://localhost:8080/client-logs";
    }
    return "/client-logs";
}

function readStoredAuthToken() {
    try {
        return globalThis.localStorage?.getItem(AUTH_TOKEN_KEY) ?? null;
    } catch {
        return null;
    }
}

function getTokenSafely(getAuthToken) {
    try {
        const token = getAuthToken();
        return typeof token === "string" && token ? token : null;
    } catch {
        return null;
    }
}

function finiteNow(now) {
    try {
        const value = Number(now());
        return Number.isFinite(value) ? value : 0;
    } catch {
        return 0;
    }
}

function positiveIntegerOrDefault(value, defaultValue) {
    return Number.isInteger(value) && value > 0 ? value : defaultValue;
}

function nonNegativeIntegerOrDefault(value, defaultValue) {
    return Number.isInteger(value) && value >= 0 ? value : defaultValue;
}

function nonNegativeNumberOrDefault(value, defaultValue) {
    return Number.isFinite(value) && value >= 0 ? value : defaultValue;
}
