package com.househost.observability.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.househost.observability.application.records.ClientLogRequestContextRecord;
import com.househost.observability.domain.exception.ClientLogRateLimitExceededException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ClientLogRateLimiterTest {

    @Test
    void throttlesActorAndFailsClosedWhenKeyCapacityIsReached() {
        ClientLogRateLimiter clientLogRateLimiter = new ClientLogRateLimiter(
                1, 10, 2, Duration.ofMinutes(1), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)
        );
        ClientLogRequestContextRecord first = context("actor-a", "origin-a");

        assertDoesNotThrow(() -> clientLogRateLimiter.verify(first));
        assertThrows(ClientLogRateLimitExceededException.class, () -> clientLogRateLimiter.verify(first));
        assertThrows(
                ClientLogRateLimitExceededException.class,
                () -> clientLogRateLimiter.verify(context("actor-b", "origin-b"))
        );
    }

    private ClientLogRequestContextRecord context(String actor, String origin) {
        return new ClientLogRequestContextRecord(actor, origin, "correlation", Instant.EPOCH);
    }
}
