package com.househost.privacy.legalbasis.application.dto;

import com.househost.privacy.legalbasis.domain.model.LegalBasisAssessmentStatus;
import com.househost.privacy.legalbasis.domain.model.LegalBasisType;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import com.househost.privacy.legalbasis.domain.model.SensitiveDataLegalBasisType;
import java.time.LocalDateTime;

public record ProcessingLegalBasisAssessmentResponseDTO(
        Long id,
        Long processingOperationId,
        String purpose,
        LegalBasisType legalBasis,
        String lgpdReference,
        String justification,
        String personalDataCategories,
        String necessityAssessment,
        String legalReference,
        String legalObligationDescription,
        String contractualContext,
        String consentCollectionMechanism,
        String consentEvidenceMechanism,
        String consentWithdrawalMechanism,
        String legitimateInterest,
        String legitimateExpectation,
        String rightsImpactAssessment,
        String safeguards,
        String balancingConclusion,
        boolean sensitiveData,
        SensitiveDataLegalBasisType sensitiveDataLegalBasis,
        String sensitiveDataLegalBasisLgpdReference,
        String sensitiveDataIndispensability,
        LegalBasisAssessmentStatus status,
        int assessmentVersion,
        Long previousVersionId,
        Long reviewedByUserId,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt,
        String rejectionReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public ProcessingLegalBasisAssessmentResponseDTO(ProcessingLegalBasisAssessment assessment) {
        this(assessment.getId(), assessment.getProcessingOperationId(), assessment.getPurpose(),
                assessment.getLegalBasis(), assessment.getLegalBasis().getLgpdReference(),
                assessment.getJustification(), assessment.getPersonalDataCategories(),
                assessment.getNecessityAssessment(), assessment.getLegalReference(),
                assessment.getLegalObligationDescription(), assessment.getContractualContext(),
                assessment.getConsentCollectionMechanism(), assessment.getConsentEvidenceMechanism(),
                assessment.getConsentWithdrawalMechanism(), assessment.getLegitimateInterest(),
                assessment.getLegitimateExpectation(), assessment.getRightsImpactAssessment(),
                assessment.getSafeguards(), assessment.getBalancingConclusion(), assessment.isSensitiveData(),
                assessment.getSensitiveDataLegalBasis(), sensitiveDataReference(assessment),
                assessment.getSensitiveDataIndispensability(),
                assessment.getStatus(), assessment.getAssessmentVersion(), assessment.getPreviousVersionId(),
                assessment.getReviewedByUserId(), assessment.getSubmittedAt(), assessment.getReviewedAt(),
                assessment.getRejectionReason(), assessment.getCreatedAt(), assessment.getUpdatedAt());
    }

    private static String sensitiveDataReference(ProcessingLegalBasisAssessment assessment) {
        return assessment.getSensitiveDataLegalBasis() == null
                ? null
                : assessment.getSensitiveDataLegalBasis().getLgpdReference();
    }
}
