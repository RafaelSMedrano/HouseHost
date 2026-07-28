package com.househost.privacy.processing.application.port.in;

import com.househost.privacy.processing.application.dto.ProcessingOperationResponseDTO;

public interface DataProcessingOperationReviewUseCase {

    ProcessingOperationResponseDTO review(Long id, String authenticatedEmail);
}
