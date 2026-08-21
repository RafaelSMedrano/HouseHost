package com.househost.observability.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.househost.observability.application.dto.ClientLogRequestDTO;
import com.househost.observability.application.port.out.ClientLogSinkPort;
import com.househost.observability.application.records.ClientLogRequestContextRecord;
import com.househost.observability.domain.exception.ClientLogUnavailableException;
import com.househost.observability.domain.model.ClientLogLevel;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ClientLogServiceTest {

    @Test
    void emitsOnlyTheSanitizedRecordAndMapsSinkFailureSafely() {
        ClientLogSinkPort sinkPort = mock(ClientLogSinkPort.class);
        ClientLogService clientLogService = createClientLogService(sinkPort);
        ClientLogRequestDTO request = request();
        ClientLogRequestContextRecord contextRecord = context();

        clientLogService.record(request, contextRecord);
        verify(sinkPort).emit(any());

        doThrow(new RuntimeException("marker-database-detail")).when(sinkPort).emit(any());
        assertThrows(
                ClientLogUnavailableException.class,
                () -> clientLogService.record(request, contextRecord)
        );
    }

    private ClientLogService createClientLogService(ClientLogSinkPort sinkPort) {
        return new ClientLogService(
                new ClientLogValidationService(new CorrelationIdService()),
                new ClientLogRateLimiter(10, 10, 10, Duration.ofMinutes(1), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)),
                sinkPort
        );
    }

    private ClientLogRequestDTO request() {
        ClientLogRequestDTO request = new ClientLogRequestDTO();
        request.setLevel(ClientLogLevel.WARN);
        request.setEvent("client.api_failed");
        request.setMessage("safe message");
        return request;
    }

    private ClientLogRequestContextRecord context() {
        return new ClientLogRequestContextRecord("actor", "origin", "correlation", Instant.EPOCH);
    }
}
