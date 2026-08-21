package com.househost.observability.application.service;

import com.househost.observability.application.dto.ClientLogRequestDTO;
import com.househost.observability.application.port.in.ClientLogUseCase;
import com.househost.observability.application.port.out.ClientLogSinkPort;
import com.househost.observability.application.records.ClientLogRequestContextRecord;
import com.househost.observability.application.records.SanitizedClientLogRecord;
import com.househost.observability.domain.exception.ClientLogUnavailableException;
import org.springframework.stereotype.Service;

@Service
public class ClientLogService implements ClientLogUseCase {

    private final ClientLogValidationService clientLogValidationService;
    private final ClientLogRateLimiter clientLogRateLimiter;
    private final ClientLogSinkPort clientLogSinkPort;

    public ClientLogService(
            ClientLogValidationService clientLogValidationService,
            ClientLogRateLimiter clientLogRateLimiter,
            ClientLogSinkPort clientLogSinkPort
    ) {
        this.clientLogValidationService = clientLogValidationService;
        this.clientLogRateLimiter = clientLogRateLimiter;
        this.clientLogSinkPort = clientLogSinkPort;
    }

    @Override
    public void record(ClientLogRequestDTO request, ClientLogRequestContextRecord contextRecord) {
        clientLogRateLimiter.verify(contextRecord);
        SanitizedClientLogRecord clientLogRecord = clientLogValidationService.sanitize(request, contextRecord);
        try {
            clientLogSinkPort.emit(clientLogRecord);
        } catch (RuntimeException exception) {
            throw new ClientLogUnavailableException();
        }
    }
}
