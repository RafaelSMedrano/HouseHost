package com.househost.observability.adapter.out.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.househost.observability.application.records.SanitizedClientLogRecord;
import com.househost.observability.domain.model.ClientLogLevel;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class Slf4jClientLogAdapterTest {

    @Test
    void mapsSeverityAndEmitsOnlyExplicitFields() {
        Logger logger = (Logger) LoggerFactory.getLogger(Slf4jClientLogAdapter.LOGGER_NAME);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            new Slf4jClientLogAdapter().emit(new SanitizedClientLogRecord(
                    ClientLogLevel.ERROR, "client.failed", "safe", "client-correlation", "/rooms",
                    "GET", 500, 12L, "safe stack", Instant.EPOCH, "actor-ref", "origin-ref",
                    "request-correlation", Instant.EPOCH
            ));

            ILoggingEvent event = appender.list.get(appender.list.size() - 1);
            assertEquals(Level.ERROR, event.getLevel());
            assertTrue(event.getFormattedMessage().contains("event=client.reported"));
            assertTrue(event.getFormattedMessage().contains("actorRef=actor-ref"));
            assertFalse(event.getFormattedMessage().contains("ClientLogRequestDTO"));
        } finally {
            logger.detachAppender(appender);
        }
    }
}
