package com.househost.observability.application.records;

import java.time.Instant;

public record ClientLogRequestContextRecord(
        String actorReference,
        String originReference,
        String requestCorrelationId,
        Instant receivedAt
) {
}
