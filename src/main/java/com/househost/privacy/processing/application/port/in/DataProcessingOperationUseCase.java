package com.househost.privacy.processing.application.port.in;

import com.househost.privacy.processing.application.dto.DataProcessingOperationRequestDTO;
import com.househost.privacy.processing.application.dto.ProcessingOperationResponseDTO;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import java.util.List;
import java.util.Optional;

public interface DataProcessingOperationUseCase {
    ProcessingOperationResponseDTO create(DataProcessingOperationRequestDTO request);

    List<ProcessingOperationResponseDTO> findAll(DataProcessingOperationStatus status);

    ProcessingOperationResponseDTO findById(Long id);

    ProcessingOperationResponseDTO update(Long id, DataProcessingOperationRequestDTO request);

    ProcessingOperationResponseDTO changeStatus(Long id, DataProcessingOperationStatus status);

    Optional<Long> findIdByOperationCode(String operationCode);
}
