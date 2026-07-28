package com.househost.privacy.legalbasis.application.port.in;

import com.househost.privacy.legalbasis.application.dto.ProcessingLegalBasisAssessmentRequestDTO;
import com.househost.privacy.legalbasis.application.dto.ProcessingLegalBasisAssessmentResponseDTO;
import com.househost.privacy.legalbasis.application.dto.DataProcessingOperationLegalBasisSummaryDTO;
import java.util.List;

public interface ProcessingLegalBasisAssessmentUseCase {
    ProcessingLegalBasisAssessmentResponseDTO createDraft(Long operationId,
            ProcessingLegalBasisAssessmentRequestDTO request);
    List<DataProcessingOperationLegalBasisSummaryDTO> findByOperation(Long operationId);
    ProcessingLegalBasisAssessmentResponseDTO findById(Long assessmentId);
    ProcessingLegalBasisAssessmentResponseDTO updateDraft(Long assessmentId,
            ProcessingLegalBasisAssessmentRequestDTO request);
    ProcessingLegalBasisAssessmentResponseDTO submit(Long assessmentId);
    ProcessingLegalBasisAssessmentResponseDTO approve(Long assessmentId, String reviewerEmail);
    ProcessingLegalBasisAssessmentResponseDTO reject(Long assessmentId, String reviewerEmail, String reason);
    ProcessingLegalBasisAssessmentResponseDTO createRevision(Long assessmentId);
}
