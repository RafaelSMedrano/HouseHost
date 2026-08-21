package com.househost.observability.adapter.in.rest;

import com.househost.observability.adapter.in.web.CorrelationLoggingFilter;
import com.househost.observability.application.dto.ClientLogRequestDTO;
import com.househost.observability.application.port.in.ClientLogUseCase;
import com.househost.observability.application.records.ClientLogRequestContextRecord;
import com.househost.observability.application.service.ClientLogContextService;
import com.househost.shared.dto.ResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientLogController {

    private final ClientLogUseCase clientLogUseCase;
    private final ClientLogContextService clientLogContextService;

    public ClientLogController(
            ClientLogUseCase clientLogUseCase,
            ClientLogContextService clientLogContextService
    ) {
        this.clientLogUseCase = clientLogUseCase;
        this.clientLogContextService = clientLogContextService;
    }

    @PostMapping("/client-logs")
    public ResponseEntity<ResponseDTO> record(
            @Valid @RequestBody ClientLogRequestDTO request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        ClientLogRequestContextRecord contextRecord = clientLogContextService.create(
                authentication,
                httpRequest.getRemoteAddr(),
                MDC.get(CorrelationLoggingFilter.CORRELATION_MDC_KEY)
        );
        clientLogUseCase.record(request, contextRecord);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ResponseDTO("success", "Log recebido.", null));
    }
}
