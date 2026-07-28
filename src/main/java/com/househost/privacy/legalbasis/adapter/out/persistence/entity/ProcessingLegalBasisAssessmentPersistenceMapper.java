package com.househost.privacy.legalbasis.adapter.out.persistence.entity;

import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import java.util.Locale;

public final class ProcessingLegalBasisAssessmentPersistenceMapper {
    private ProcessingLegalBasisAssessmentPersistenceMapper() {}

    public static ProcessingLegalBasisAssessment toDomain(ProcessingLegalBasisAssessmentJpaEntity entity) {
        ProcessingLegalBasisAssessment assessment = new ProcessingLegalBasisAssessment(
                entity.processingOperationId, entity.purpose, entity.legalBasis);
        assessment.updateDetails(entity.purpose, entity.legalBasis, entity.justification,
                entity.personalDataCategories, entity.necessityAssessment, entity.legalReference,
                entity.legalObligationDescription, entity.contractualContext, entity.consentCollectionMechanism,
                entity.consentEvidenceMechanism, entity.consentWithdrawalMechanism, entity.legitimateInterest,
                entity.legitimateExpectation, entity.rightsImpactAssessment, entity.safeguards,
                entity.balancingConclusion, entity.sensitiveData, entity.sensitiveDataLegalBasis,
                entity.sensitiveDataIndispensability);
        assessment.restorePersistenceState(entity.id, entity.status, entity.assessmentVersion,
                entity.previousVersionId, entity.reviewedByUserId, entity.submittedAt, entity.reviewedAt,
                entity.rejectionReason, entity.createdAt, entity.updatedAt);
        return assessment;
    }

    public static ProcessingLegalBasisAssessmentJpaEntity toEntity(ProcessingLegalBasisAssessment assessment) {
        ProcessingLegalBasisAssessmentJpaEntity entity = new ProcessingLegalBasisAssessmentJpaEntity();
        entity.id = assessment.getId();
        entity.processingOperationId = assessment.getProcessingOperationId();
        entity.purposeKey = purposeKey(assessment.getPurpose());
        entity.purpose = assessment.getPurpose();
        entity.legalBasis = assessment.getLegalBasis();
        entity.justification = assessment.getJustification();
        entity.personalDataCategories = assessment.getPersonalDataCategories();
        entity.necessityAssessment = assessment.getNecessityAssessment();
        entity.legalReference = assessment.getLegalReference();
        entity.legalObligationDescription = assessment.getLegalObligationDescription();
        entity.contractualContext = assessment.getContractualContext();
        entity.consentCollectionMechanism = assessment.getConsentCollectionMechanism();
        entity.consentEvidenceMechanism = assessment.getConsentEvidenceMechanism();
        entity.consentWithdrawalMechanism = assessment.getConsentWithdrawalMechanism();
        entity.legitimateInterest = assessment.getLegitimateInterest();
        entity.legitimateExpectation = assessment.getLegitimateExpectation();
        entity.rightsImpactAssessment = assessment.getRightsImpactAssessment();
        entity.safeguards = assessment.getSafeguards();
        entity.balancingConclusion = assessment.getBalancingConclusion();
        entity.sensitiveData = assessment.isSensitiveData();
        entity.sensitiveDataLegalBasis = assessment.getSensitiveDataLegalBasis();
        entity.sensitiveDataIndispensability = assessment.getSensitiveDataIndispensability();
        entity.status = assessment.getStatus();
        entity.assessmentVersion = assessment.getAssessmentVersion();
        entity.previousVersionId = assessment.getPreviousVersionId();
        entity.reviewedByUserId = assessment.getReviewedByUserId();
        entity.submittedAt = assessment.getSubmittedAt();
        entity.reviewedAt = assessment.getReviewedAt();
        entity.rejectionReason = assessment.getRejectionReason();
        entity.createdAt = assessment.getCreatedAt();
        entity.updatedAt = assessment.getUpdatedAt();
        return entity;
    }

    public static String purposeKey(String purpose) {
        return purpose.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
