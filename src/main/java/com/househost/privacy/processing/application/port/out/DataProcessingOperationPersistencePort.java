package com.househost.privacy.processing.application.port.out;

import com.househost.privacy.processing.domain.model.DataProcessingOperation;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import java.util.List;
import java.util.Optional;

public interface DataProcessingOperationPersistencePort {
    DataProcessingOperation save(DataProcessingOperation operation);
    Optional<DataProcessingOperation> findById(Long id);
    Optional<DataProcessingOperation> findByOperationCode(String operationCode);
    List<DataProcessingOperation> findAllOrderedByName();
    List<DataProcessingOperation> findAllByStatusOrderedByName(DataProcessingOperationStatus status);
    boolean existsByOperationName(String operationName);
    boolean existsByOperationNameExcludingId(String operationName, Long id);
    boolean existsByOperationCode(String operationCode);
}
