package com.househost.privacy.processing.application.service;

import com.househost.privacy.processing.application.dto.ProcessingOperationResponseDTO;
import com.househost.privacy.processing.application.port.in.DataProcessingOperationReviewUseCase;
import com.househost.privacy.processing.application.port.out.DataProcessingOperationPersistencePort;
import com.househost.privacy.processing.application.port.out.ProcessingOperationReviewerPort;
import com.househost.privacy.processing.domain.model.DataProcessingOperation;
import com.househost.shared.exception.PrivacyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataProcessingOperationReviewService implements DataProcessingOperationReviewUseCase {
    private final DataProcessingOperationPersistencePort persistencePort;
    private final ProcessingOperationReviewerPort reviewerPort;

    public DataProcessingOperationReviewService(
            DataProcessingOperationPersistencePort persistencePort,
            ProcessingOperationReviewerPort reviewerPort
    ) {
        this.persistencePort = persistencePort;
        this.reviewerPort = reviewerPort;
    }

    @Override
    @Transactional
    public ProcessingOperationResponseDTO review(Long id, String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new PrivacyException("Usuario autenticado nao identificado.");
        }
        if (id == null) {
            throw new PrivacyException("Operacao de tratamento nao encontrada.");
        }

        DataProcessingOperation operation = persistencePort.findById(id)
                .orElseThrow(() -> new PrivacyException("Operacao de tratamento nao encontrada."));
        Long reviewerId = reviewerPort.findReviewerIdByEmail(authenticatedEmail);
        operation.markReviewed(reviewerId);

        DataProcessingOperation savedOperation = persistencePort.save(operation);
        return new ProcessingOperationResponseDTO(savedOperation);
    }
}
