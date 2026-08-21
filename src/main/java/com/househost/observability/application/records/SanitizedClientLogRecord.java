package com.househost.observability.application.records;

import com.househost.observability.domain.model.ClientLogLevel;
import java.time.Instant;

public record SanitizedClientLogRecord(
        ClientLogLevel level,
        String event,
        String message,
        String correlationId,
        String route,
        String method,
        Integer status,
        Long durationMs,
        String stack,
        Instant clientTimestamp,
        String actorReference,
        String originReference,
        String requestCorrelationId,
        Instant receivedAt
) {
}
