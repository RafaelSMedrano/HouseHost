package com.househost.privacy.processing.application.records;

import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;

public record ProcessingOperationRecord(
        Long operationId,
        String operationCode,
        DataProcessingOperationStatus status
) {
}
