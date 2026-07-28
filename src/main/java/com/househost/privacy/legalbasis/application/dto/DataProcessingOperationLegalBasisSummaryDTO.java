package com.househost.privacy.legalbasis.application.dto;

import com.househost.privacy.legalbasis.domain.model.LegalBasisAssessmentStatus;
import com.househost.privacy.legalbasis.domain.model.LegalBasisType;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;

public record DataProcessingOperationLegalBasisSummaryDTO(
        Long id,
        String purpose,
        LegalBasisType legalBasis,
        LegalBasisAssessmentStatus status,
        int assessmentVersion,
        boolean current
) {
    public DataProcessingOperationLegalBasisSummaryDTO(ProcessingLegalBasisAssessment assessment, boolean current) {
        this(assessment.getId(), assessment.getPurpose(), assessment.getLegalBasis(), assessment.getStatus(),
                assessment.getAssessmentVersion(), current);
    }
}
