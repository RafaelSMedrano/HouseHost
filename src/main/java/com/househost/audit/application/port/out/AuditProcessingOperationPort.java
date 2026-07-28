package com.househost.audit.application.port.out;

import java.util.Optional;

public interface AuditProcessingOperationPort {
    Optional<Long> findIdByOperationCode(String operationCode);
}
