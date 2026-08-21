package com.househost.observability.application.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.househost.observability.application.dto.ClientLogRequestDTO;
import com.househost.observability.application.records.ClientLogRequestContextRecord;
import com.househost.observability.application.records.SanitizedClientLogRecord;
import com.househost.observability.domain.exception.ClientLogRejectedException;
import com.househost.observability.domain.model.ClientLogLevel;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ClientLogValidationServiceTest {

    private final ClientLogValidationService clientLogValidationService =
            new ClientLogValidationService(new CorrelationIdService());

    @Test
    void stripsLogForgingQueriesAndSensitiveValues() {
        ClientLogRequestDTO request = validRequest();
        request.setRoute("/bookings?token=marker-query-token");
        request.setMessage("failure\r\nforged=true password=marker-password email=guest@example.com phone=11987654321");
        request.setStack("Authorization=Bearer-secret cookie=marker-cookie cpf=123.456.789-10 eyJabcdefghijk.abcdefghijk.abcdefghijk");

        SanitizedClientLogRecord clientLogRecord = clientLogValidationService.sanitize(request, context());
        String output = clientLogRecord.message() + clientLogRecord.stack() + clientLogRecord.route();

        assertFalse(output.contains("\r"));
        assertFalse(output.contains("\n"));
        assertFalse(output.contains("marker-password"));
        assertFalse(output.contains("guest@example.com"));
        assertFalse(output.contains("11987654321"));
        assertFalse(output.contains("marker-cookie"));
        assertFalse(output.contains("123.456.789-10"));
        assertFalse(output.contains("eyJabcdefghijk"));
        assertFalse(output.contains("marker-query-token"));
        assertTrue(clientLogRecord.route().equals("/bookings"));
    }

    @Test
    void rejectsClientCorrelationOutsideSharedPolicy() {
        ClientLogRequestDTO request = validRequest();
        request.setCorrelationId("invalid correlation");

        assertThrows(
                ClientLogRejectedException.class,
                () -> clientLogValidationService.sanitize(request, context())
        );
    }

    private ClientLogRequestDTO validRequest() {
        ClientLogRequestDTO request = new ClientLogRequestDTO();
        request.setLevel(ClientLogLevel.ERROR);
        request.setEvent("client.unhandled_error");
        request.setMessage("Failure");
        request.setCorrelationId("client-123");
        return request;
    }

    private ClientLogRequestContextRecord context() {
        return new ClientLogRequestContextRecord("actor-ref", "origin-ref", "request-123", Instant.now());
    }
}
