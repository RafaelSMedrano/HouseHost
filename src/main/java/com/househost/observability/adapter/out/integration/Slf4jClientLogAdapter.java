package com.househost.observability.adapter.out.integration;

import com.househost.observability.application.port.out.ClientLogSinkPort;
import com.househost.observability.application.records.SanitizedClientLogRecord;
import com.househost.observability.domain.model.ClientLogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Slf4jClientLogAdapter implements ClientLogSinkPort {

    public static final String LOGGER_NAME = "HOUSEHOST_CLIENT_LOG";
    private static final Logger LOGGER = LoggerFactory.getLogger(LOGGER_NAME);
    private static final String LOG_FORMAT = "event=client.reported clientEvent={} actorRef={} originRef={} requestCorrelationId={} clientCorrelationId={} route={} method={} status={} durationMs={} clientTimestamp={} receivedAt={} message={} stack={}";

    @Override
    public void emit(SanitizedClientLogRecord clientLogRecord) {
        Object[] arguments = {
                clientLogRecord.event(),
                clientLogRecord.actorReference(),
                clientLogRecord.originReference(),
                clientLogRecord.requestCorrelationId(),
                clientLogRecord.correlationId(),
                clientLogRecord.route(),
                clientLogRecord.method(),
                clientLogRecord.status(),
                clientLogRecord.durationMs(),
                clientLogRecord.clientTimestamp(),
                clientLogRecord.receivedAt(),
                clientLogRecord.message(),
                clientLogRecord.stack()
        };
        if (clientLogRecord.level() == ClientLogLevel.ERROR) {
            LOGGER.error(LOG_FORMAT, arguments);
        } else {
            LOGGER.warn(LOG_FORMAT, arguments);
        }
    }
}
