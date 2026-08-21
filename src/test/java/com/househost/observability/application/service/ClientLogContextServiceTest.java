package com.househost.observability.application.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.househost.observability.application.records.ClientLogRequestContextRecord;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class ClientLogContextServiceTest {

    @Test
    void derivesPseudonymousActorOriginAndReceiptTimeOnServer() {
        Instant before = Instant.now();
        ClientLogContextService clientLogContextService = new ClientLogContextService();

        ClientLogRequestContextRecord contextRecord = clientLogContextService.create(
                UsernamePasswordAuthenticationToken.authenticated("operator@example.invalid", "ignored", java.util.List.of()),
                "203.0.113.10",
                "request-correlation"
        );

        assertFalse(contextRecord.actorReference().contains("operator@example.invalid"));
        assertFalse(contextRecord.originReference().contains("203.0.113.10"));
        assertTrue(contextRecord.actorReference().startsWith("actor-"));
        assertTrue(contextRecord.originReference().startsWith("origin-"));
        assertNotNull(contextRecord.receivedAt());
        assertFalse(contextRecord.receivedAt().isBefore(before));
    }
}
