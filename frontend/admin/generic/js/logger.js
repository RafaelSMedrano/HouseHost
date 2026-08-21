const LEVEL = Object.freeze({
    DEBUG: "DEBUG",
    INFO: "INFO",
    WARN: "WARN",
    ERROR: "ERROR",
});

const EVENT_PATTERN = /^[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*$/;
const CORRELATION_PATTERN = /^[A-Za-z0-9][A-Za-z0-9._:-]{0,63}$/;
const CONTROL_CHARACTER_PATTERN = /[\u0000-\u001f\u007f-\u009f]/g;
const URL_QUERY_PATTERN = /\b([a-z][a-z0-9+.-]*:\/\/[^\s?]+)\?[^\s]*/gi;
const SENSITIVE_VALUE_PATTERN = /\b(password|passwd|senha|token|authorization|cookie|session|document(?:number)?|cpf|email|phone|telefone|creditcard|card)\b\s*[:=]\s*[^\s,;]+/gi;
const JWT_PATTERN = /\beyJ[A-Za-z0-9_-]{8,}\.[A-Za-z0-9_-]{8,}(?:\.[A-Za-z0-9_-]{8,})?\b/g;
const EMAIL_PATTERN = /\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}\b/gi;
const PHONE_PATTERN = /(^|\D)(?:\+?55\s*)?(?:\(?\d{2}\)?[ .-]*)?9?\d{4}[ .-]*\d{4}(?!\d)/g;
const DOCUMENT_PATTERN = /(^|\D)\d{3}[.-]?\d{3}[.-]?\d{3}-?\d{2}(?!\d)/g;

const MESSAGE_MAX_LENGTH = 1000;
const STACK_MAX_LENGTH = 8000;
const ROUTE_MAX_LENGTH = 512;
const METHOD_MAX_LENGTH = 10;
const SAFE_CONTEXT_FIELD_SET = new Set([
    "correlationId",
    "route",
    "method",
    "status",
    "durationMs",
]);
const INSTALLED_EVENT_TARGET_SET = new WeakSet();
let configuredTransport = null;

export function createLogger(options = {}) {
    const environment = normalizeEnvironment(options.environment);
    const consoleTarget = Object.hasOwn(options, "consoleTarget")
            ? options.consoleTarget
            : globalThis.console;
    const transport = typeof options.transport === "function" ? options.transport : null;
    const now = typeof options.now === "function" ? options.now : () => new Date();
    let emitting = false;

    function emit(level, event, message, error = null, context = {}) {
        if (emitting) {
            return false;
        }

        emitting = true;
        try {
            const record = buildRecord(level, event, message, error, context, now);
            if (!record) {
                return false;
            }

            writeToConsole(record, environment, consoleTarget);
            submitToTransport(record, transport);
            return true;
        } catch {
            return false;
        } finally {
            emitting = false;
        }
    }

    const loggerInstance = {
        debug(event, message, context) {
            return emit(LEVEL.DEBUG, event, message, null, context);
        },
        info(event, message, context) {
            return emit(LEVEL.INFO, event, message, null, context);
        },
        warn(event, message, context) {
            return emit(LEVEL.WARN, event, message, null, context);
        },
        error(event, message, error, context) {
            return emit(LEVEL.ERROR, event, message, error, context);
        },
        installGlobalErrorLogging(installationOptions = {}) {
            const normalizedOptions = installationOptions && typeof installationOptions === "object"
                    ? installationOptions
                    : {};
            return installGlobalErrorLogging({
                ...normalizedOptions,
                loggerInstance,
            });
        },
    };

    return Object.freeze(loggerInstance);
}

export function installGlobalErrorLogging(options = {}) {
    const normalizedOptions = options && typeof options === "object" ? options : {};
    const eventTarget = normalizedOptions.eventTarget ?? globalThis;
    const loggerInstance = normalizedOptions.loggerInstance ?? logger;

    if (!isValidGlobalLoggingTarget(eventTarget, loggerInstance)
            || INSTALLED_EVENT_TARGET_SET.has(eventTarget)) {
        return false;
    }

    let reporting = false;
    const safelyReport = (reporter, event) => {
        if (reporting) {
            return;
        }
        reporting = true;
        try {
            reporter(loggerInstance, eventTarget, event);
        } finally {
            reporting = false;
        }
    };
    const errorListener = (event) => safelyReport(reportUnhandledError, event);
    const rejectionListener = (event) => safelyReport(reportUnhandledRejection, event);

    try {
        eventTarget.addEventListener("error", errorListener);
        eventTarget.addEventListener("unhandledrejection", rejectionListener);
        INSTALLED_EVENT_TARGET_SET.add(eventTarget);
        return true;
    } catch {
        removeListenerSafely(eventTarget, "error", errorListener);
        removeListenerSafely(eventTarget, "unhandledrejection", rejectionListener);
        return false;
    }
}

export function configureLoggerTransport(transport) {
    configuredTransport = typeof transport === "function" ? transport : null;
    return configuredTransport !== null;
}

function isValidGlobalLoggingTarget(eventTarget, loggerInstance) {
    return eventTarget !== null
            && (typeof eventTarget === "object" || typeof eventTarget === "function")
            && typeof eventTarget.addEventListener === "function"
            && loggerInstance !== null
            && typeof loggerInstance === "object"
            && typeof loggerInstance.error === "function";
}

function reportUnhandledError(loggerInstance, eventTarget, event) {
    try {
        const error = readProperty(event, "error");
        const eventMessage = readProperty(event, "message");
        const message = firstSafeMessage(eventMessage, readErrorText(error, "message"))
                || "Unhandled browser error.";
        loggerInstance.error(
                "client.unhandled_error",
                message,
                isObject(error) ? error : null,
                resolveGlobalContext(eventTarget)
        );
    } catch {
        // A global listener must not create another application failure.
    }
}

function reportUnhandledRejection(loggerInstance, eventTarget, event) {
    try {
        const reason = readProperty(event, "reason");
        const message = typeof reason === "string"
                ? reason
                : firstSafeMessage(readErrorText(reason, "message")) || "Unhandled promise rejection.";
        loggerInstance.error(
                "client.unhandled_rejection",
                message,
                isObject(reason) ? reason : null,
                resolveGlobalContext(eventTarget)
        );
    } catch {
        // A global listener must not produce another unhandled rejection.
    }
}

function firstSafeMessage(...candidateList) {
    return candidateList.find((candidate) => typeof candidate === "string" && candidate.trim()) ?? "";
}

function resolveGlobalContext(eventTarget) {
    const location = readProperty(eventTarget, "location");
    const pathname = isObject(location) ? readProperty(location, "pathname") : undefined;
    return typeof pathname === "string" ? { route: pathname } : {};
}

function removeListenerSafely(eventTarget, eventName, listener) {
    try {
        if (typeof eventTarget.removeEventListener === "function") {
            eventTarget.removeEventListener(eventName, listener);
        }
    } catch {
        // Failed installation remains a safe no-op for the application.
    }
}

function isObject(value) {
    return value !== null && typeof value === "object";
}

function buildRecord(level, event, message, error, context, now) {
    if (!Object.values(LEVEL).includes(level) || !isValidEvent(event)) {
        return null;
    }

    const sanitizedMessage = sanitizeFreeText(toSafeScalarText(message), MESSAGE_MAX_LENGTH);
    if (!sanitizedMessage) {
        return null;
    }

    const sanitizedContext = sanitizeContext(context);
    const stack = sanitizeFreeText(readErrorText(error, "stack"), STACK_MAX_LENGTH);
    const clientTimestamp = resolveTimestamp(now);

    return Object.freeze({
        level,
        event,
        message: sanitizedMessage,
        ...sanitizedContext,
        ...(stack ? { stack } : {}),
        clientTimestamp,
    });
}

function isValidEvent(event) {
    return typeof event === "string"
            && event.length <= 80
            && EVENT_PATTERN.test(event);
}

function sanitizeContext(context) {
    if (!context || typeof context !== "object" || Array.isArray(context)) {
        return {};
    }

    const sanitizedContext = {};
    for (const field of SAFE_CONTEXT_FIELD_SET) {
        const value = readProperty(context, field);
        const sanitizedValue = sanitizeContextValue(field, value);
        if (sanitizedValue !== undefined) {
            sanitizedContext[field] = sanitizedValue;
        }
    }
    return sanitizedContext;
}

function sanitizeContextValue(field, value) {
    if (field === "correlationId") {
        return typeof value === "string" && CORRELATION_PATTERN.test(value) ? value : undefined;
    }
    if (field === "route") {
        if (typeof value !== "string") {
            return undefined;
        }
        return truncate(removeQueryString(value), ROUTE_MAX_LENGTH) || undefined;
    }
    if (field === "method") {
        if (typeof value !== "string") {
            return undefined;
        }
        const method = removeControlCharacters(value).trim().toUpperCase();
        return /^[A-Z]{3,10}$/.test(method) ? truncate(method, METHOD_MAX_LENGTH) : undefined;
    }
    if (field === "status") {
        return Number.isInteger(value) && value >= 100 && value <= 599 ? value : undefined;
    }
    if (field === "durationMs") {
        return Number.isFinite(value) && value >= 0 && value <= 86_400_000
                ? Math.round(value)
                : undefined;
    }
    return undefined;
}

function sanitizeFreeText(value, maximumLength) {
    if (!value) {
        return "";
    }

    let sanitized = removeControlCharacters(value).replaceAll(/\s+/g, " ");
    sanitized = sanitized.replace(URL_QUERY_PATTERN, "$1?[REDACTED]");
    sanitized = sanitized.replace(SENSITIVE_VALUE_PATTERN, "$1=[REDACTED]");
    sanitized = sanitized.replace(JWT_PATTERN, "[REDACTED_JWT]");
    sanitized = sanitized.replace(EMAIL_PATTERN, "[REDACTED_EMAIL]");
    sanitized = sanitized.replace(PHONE_PATTERN, "$1[REDACTED_PHONE]");
    sanitized = sanitized.replace(DOCUMENT_PATTERN, "$1[REDACTED_DOCUMENT]");
    return truncate(sanitized.trim(), maximumLength);
}

function removeQueryString(value) {
    const withoutControls = removeControlCharacters(value).trim();
    const queryIndex = withoutControls.indexOf("?");
    return queryIndex >= 0 ? withoutControls.slice(0, queryIndex) : withoutControls;
}

function removeControlCharacters(value) {
    return value.replace(CONTROL_CHARACTER_PATTERN, " ");
}

function toSafeScalarText(value) {
    if (typeof value === "string") {
        return value;
    }
    if (typeof value === "number" || typeof value === "boolean" || typeof value === "bigint") {
        return String(value);
    }
    return "";
}

function readErrorText(error, field) {
    if (!error || typeof error !== "object") {
        return "";
    }
    const value = readProperty(error, field);
    return typeof value === "string" ? value : "";
}

function readProperty(target, property) {
    try {
        return target[property];
    } catch {
        return undefined;
    }
}

function resolveTimestamp(now) {
    try {
        const current = now();
        const date = current instanceof Date ? current : new Date(current);
        return Number.isNaN(date.getTime()) ? new Date(0).toISOString() : date.toISOString();
    } catch {
        return new Date(0).toISOString();
    }
}

function normalizeEnvironment(environment) {
    const configuredEnvironment = environment ?? globalThis.HOUSEHOST_ENVIRONMENT ?? "development";
    return String(configuredEnvironment).trim().toLowerCase();
}

function writeToConsole(record, environment, consoleTarget) {
    if (environment === "production" && (record.level === LEVEL.DEBUG || record.level === LEVEL.INFO)) {
        return;
    }
    if (!consoleTarget) {
        return;
    }

    const methodName = record.level.toLowerCase();
    const consoleMethod = typeof consoleTarget[methodName] === "function"
            ? consoleTarget[methodName]
            : consoleTarget.log;
    if (typeof consoleMethod !== "function") {
        return;
    }

    try {
        consoleMethod.call(consoleTarget, record);
    } catch {
        // Operational logging is best effort and must not affect the caller.
    }
}

function submitToTransport(record, transport) {
    if (!transport || (record.level !== LEVEL.WARN && record.level !== LEVEL.ERROR)) {
        return;
    }

    try {
        const transportResult = transport(record);
        if (transportResult && typeof transportResult.then === "function") {
            Promise.resolve(transportResult).catch(() => {});
        }
    } catch {
        // The transport boundary must never report its own failure recursively.
    }
}

function truncate(value, maximumLength) {
    return value.length <= maximumLength ? value : value.slice(0, maximumLength);
}

export const logger = createLogger({
    transport(record) {
        return configuredTransport?.(record);
    },
});
