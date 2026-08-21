import { createClientLogTransport } from "./clientLogTransport.js";
import { configureLoggerTransport, logger } from "./logger.js";

const defaultClientLogTransport = createClientLogTransport();

export function initializeAdministrativeLogging(options = {}) {
    const normalizedOptions = options && typeof options === "object" ? options : {};
    const loggerInstance = normalizedOptions.loggerInstance ?? logger;
    const eventTarget = normalizedOptions.eventTarget ?? globalThis;
    const clientLogTransport = normalizedOptions.clientLogTransport ?? defaultClientLogTransport;

    if (loggerInstance === logger) {
        configureLoggerTransport(clientLogTransport);
        clientLogTransport.installLifecycleFlush?.(eventTarget);
    }

    installGlobalListenersSafely(loggerInstance, eventTarget);

    return Object.freeze({
        logApplicationStarted() {
            try {
                return loggerInstance.info(
                        "application.started",
                        "HouseHost iniciado."
                );
            } catch {
                return false;
            }
        },
    });
}

function installGlobalListenersSafely(loggerInstance, eventTarget) {
    try {
        if (loggerInstance && typeof loggerInstance.installGlobalErrorLogging === "function") {
            loggerInstance.installGlobalErrorLogging({ eventTarget });
        }
    } catch {
        // Logging initialization must not prevent the administrative UI startup.
    }
}
