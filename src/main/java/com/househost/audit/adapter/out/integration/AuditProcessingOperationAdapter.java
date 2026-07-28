package com.househost.audit.adapter.out.integration;

import com.househost.audit.application.port.out.AuditProcessingOperationPort;
import com.househost.privacy.processing.application.port.in.DataProcessingOperationUseCase;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AuditProcessingOperationAdapter implements AuditProcessingOperationPort {
    private final DataProcessingOperationUseCase operationUseCase;
    public AuditProcessingOperationAdapter(DataProcessingOperationUseCase operationUseCase) { this.operationUseCase = operationUseCase; }
    public Optional<Long> findIdByOperationCode(String operationCode) {
        return operationUseCase.findIdByOperationCode(operationCode);
    }
}
