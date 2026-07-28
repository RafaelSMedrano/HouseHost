package com.househost.privacy.application.port.in;

import com.househost.privacy.application.dto.DataProcessingOperationResponseDTO;
import com.househost.privacy.processing.application.dto.DataProcessingOperationRequestDTO;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import java.util.List;

public interface DataProcessingOperationGovernanceUseCase {
    DataProcessingOperationResponseDTO create(DataProcessingOperationRequestDTO request);

    List<DataProcessingOperationResponseDTO> findAll(DataProcessingOperationStatus status);

    DataProcessingOperationResponseDTO findById(Long id);

    DataProcessingOperationResponseDTO update(Long id, DataProcessingOperationRequestDTO request);

    DataProcessingOperationResponseDTO changeStatus(Long id, DataProcessingOperationStatus status);

    DataProcessingOperationResponseDTO review(Long id, String authenticatedEmail);
}
