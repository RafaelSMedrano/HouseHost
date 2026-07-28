package com.househost.privacy.application.service;

import com.househost.privacy.application.dto.DataProcessingOperationResponseDTO;
import com.househost.privacy.application.port.in.DataProcessingOperationGovernanceUseCase;
import com.househost.privacy.legalbasis.application.records.LegalBasisAssessmentOverviewRecord;
import com.househost.privacy.legalbasis.application.service.LegalBasisAssessmentQueryService;
import com.househost.privacy.processing.application.dto.DataProcessingOperationRequestDTO;
import com.househost.privacy.processing.application.dto.ProcessingOperationResponseDTO;
import com.househost.privacy.processing.application.port.in.DataProcessingOperationReviewUseCase;
import com.househost.privacy.processing.application.port.in.DataProcessingOperationUseCase;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataProcessingOperationGovernanceService implements DataProcessingOperationGovernanceUseCase {
    private final DataProcessingOperationUseCase operationUseCase;
    private final DataProcessingOperationReviewUseCase reviewUseCase;
    private final LegalBasisAssessmentQueryService legalBasisAssessmentQueryService;

    public DataProcessingOperationGovernanceService(
            DataProcessingOperationUseCase operationUseCase,
            DataProcessingOperationReviewUseCase reviewUseCase,
            LegalBasisAssessmentQueryService legalBasisAssessmentQueryService
    ) {
        this.operationUseCase = operationUseCase;
        this.reviewUseCase = reviewUseCase;
        this.legalBasisAssessmentQueryService = legalBasisAssessmentQueryService;
    }

    @Override
    @Transactional
    public DataProcessingOperationResponseDTO create(DataProcessingOperationRequestDTO request) {
        return compose(operationUseCase.create(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataProcessingOperationResponseDTO> findAll(DataProcessingOperationStatus status) {
        return operationUseCase.findAll(status).stream()
                .map(this::compose)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DataProcessingOperationResponseDTO findById(Long id) {
        return compose(operationUseCase.findById(id));
    }

    @Override
    @Transactional
    public DataProcessingOperationResponseDTO update(Long id, DataProcessingOperationRequestDTO request) {
        return compose(operationUseCase.update(id, request));
    }

    @Override
    @Transactional
    public DataProcessingOperationResponseDTO changeStatus(
            Long id,
            DataProcessingOperationStatus status
    ) {
        return compose(operationUseCase.changeStatus(id, status));
    }

    @Override
    @Transactional
    public DataProcessingOperationResponseDTO review(Long id, String authenticatedEmail) {
        return compose(reviewUseCase.review(id, authenticatedEmail));
    }

    private DataProcessingOperationResponseDTO compose(
            ProcessingOperationResponseDTO processingOperationResponseDTO
    ) {
        if (processingOperationResponseDTO.getId() == null) {
            return new DataProcessingOperationResponseDTO(processingOperationResponseDTO);
        }

        LegalBasisAssessmentOverviewRecord legalBasisAssessmentOverviewRecord =
                legalBasisAssessmentQueryService.findOverviewByOperationId(
                        processingOperationResponseDTO.getId()
                );
        return new DataProcessingOperationResponseDTO(
                processingOperationResponseDTO,
                legalBasisAssessmentOverviewRecord.readiness(),
                legalBasisAssessmentOverviewRecord.legalBasisAssessmentSummaryDTOList()
        );
    }
}
