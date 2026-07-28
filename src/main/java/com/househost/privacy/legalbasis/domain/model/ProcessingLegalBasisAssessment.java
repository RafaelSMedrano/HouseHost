package com.househost.privacy.legalbasis.domain.model;

import com.househost.shared.exception.PrivacyException;
import java.time.LocalDateTime;

public class ProcessingLegalBasisAssessment {
    private Long id;
    private final Long processingOperationId;
    private String purpose;
    private LegalBasisType legalBasis;
    private String justification;
    private String personalDataCategories;
    private String necessityAssessment;
    private String legalReference;
    private String legalObligationDescription;
    private String contractualContext;
    private String consentCollectionMechanism;
    private String consentEvidenceMechanism;
    private String consentWithdrawalMechanism;
    private String legitimateInterest;
    private String legitimateExpectation;
    private String rightsImpactAssessment;
    private String safeguards;
    private String balancingConclusion;
    private boolean sensitiveData;
    private SensitiveDataLegalBasisType sensitiveDataLegalBasis;
    private String sensitiveDataIndispensability;
    private LegalBasisAssessmentStatus status;
    private int assessmentVersion;
    private Long previousVersionId;
    private Long reviewedByUserId;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProcessingLegalBasisAssessment(
            Long processingOperationId,
            String purpose,
            LegalBasisType legalBasis
    ) {
        this.processingOperationId = processingOperationId;
        this.purpose = purpose;
        this.legalBasis = legalBasis;
        this.status = LegalBasisAssessmentStatus.DRAFT;
        this.assessmentVersion = 1;
    }

    public void updateDetails(
            String purpose,
            LegalBasisType legalBasis,
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
            String sensitiveDataIndispensability
    ) {
        requireDraft("Somente uma avaliacao em rascunho pode ser alterada.");
        this.purpose = purpose;
        this.legalBasis = legalBasis;
        this.justification = justification;
        this.personalDataCategories = personalDataCategories;
        this.necessityAssessment = necessityAssessment;
        this.legalReference = legalReference;
        this.legalObligationDescription = legalObligationDescription;
        this.contractualContext = contractualContext;
        this.consentCollectionMechanism = consentCollectionMechanism;
        this.consentEvidenceMechanism = consentEvidenceMechanism;
        this.consentWithdrawalMechanism = consentWithdrawalMechanism;
        this.legitimateInterest = legitimateInterest;
        this.legitimateExpectation = legitimateExpectation;
        this.rightsImpactAssessment = rightsImpactAssessment;
        this.safeguards = safeguards;
        this.balancingConclusion = balancingConclusion;
        this.sensitiveData = sensitiveData;
        this.sensitiveDataLegalBasis = sensitiveDataLegalBasis;
        this.sensitiveDataIndispensability = sensitiveDataIndispensability;
        touch();
    }

    public void submit() {
        requireDraft("Somente uma avaliacao em rascunho pode ser enviada para revisao.");
        status = LegalBasisAssessmentStatus.UNDER_REVIEW;
        submittedAt = LocalDateTime.now();
        touch();
    }

    public void approve(Long reviewerId) {
        requireStatus(
                LegalBasisAssessmentStatus.UNDER_REVIEW,
                "Somente uma avaliacao em revisao pode ser aprovada."
        );
        status = LegalBasisAssessmentStatus.APPROVED;
        reviewedByUserId = reviewerId;
        reviewedAt = LocalDateTime.now();
        rejectionReason = null;
        touch();
    }

    public void reject(Long reviewerId, String reason) {
        requireStatus(
                LegalBasisAssessmentStatus.UNDER_REVIEW,
                "Somente uma avaliacao em revisao pode ser rejeitada."
        );
        if (reason == null || reason.isBlank()) {
            throw new PrivacyException("O motivo da rejeicao e obrigatorio.");
        }
        status = LegalBasisAssessmentStatus.REJECTED;
        reviewedByUserId = reviewerId;
        reviewedAt = LocalDateTime.now();
        rejectionReason = reason.trim();
        touch();
    }

    public void supersede() {
        requireStatus(
                LegalBasisAssessmentStatus.APPROVED,
                "Somente uma avaliacao aprovada pode ser substituida."
        );
        status = LegalBasisAssessmentStatus.SUPERSEDED;
        touch();
    }

    public ProcessingLegalBasisAssessment createRevision() {
        return createRevision(assessmentVersion + 1);
    }

    public ProcessingLegalBasisAssessment createRevision(int nextVersion) {
        requireStatus(
                LegalBasisAssessmentStatus.APPROVED,
                "Somente uma avaliacao aprovada pode originar uma revisao."
        );
        if (nextVersion <= assessmentVersion) {
            throw new PrivacyException("A versao da revisao deve ser superior a versao aprovada.");
        }
        ProcessingLegalBasisAssessment revision = new ProcessingLegalBasisAssessment(
                processingOperationId,
                purpose,
                legalBasis
        );
        revision.copyDetailsFrom(this);
        revision.assessmentVersion = nextVersion;
        revision.previousVersionId = id;
        return revision;
    }

    private void copyDetailsFrom(ProcessingLegalBasisAssessment source) {
        justification = source.justification;
        personalDataCategories = source.personalDataCategories;
        necessityAssessment = source.necessityAssessment;
        legalReference = source.legalReference;
        legalObligationDescription = source.legalObligationDescription;
        contractualContext = source.contractualContext;
        consentCollectionMechanism = source.consentCollectionMechanism;
        consentEvidenceMechanism = source.consentEvidenceMechanism;
        consentWithdrawalMechanism = source.consentWithdrawalMechanism;
        legitimateInterest = source.legitimateInterest;
        legitimateExpectation = source.legitimateExpectation;
        rightsImpactAssessment = source.rightsImpactAssessment;
        safeguards = source.safeguards;
        balancingConclusion = source.balancingConclusion;
        sensitiveData = source.sensitiveData;
        sensitiveDataLegalBasis = source.sensitiveDataLegalBasis;
        sensitiveDataIndispensability = source.sensitiveDataIndispensability;
    }

    public void prepareForCreation() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    private void touch() {
        updatedAt = LocalDateTime.now();
    }

    private void requireDraft(String message) {
        requireStatus(LegalBasisAssessmentStatus.DRAFT, message);
    }

    private void requireStatus(LegalBasisAssessmentStatus expected, String message) {
        if (status != expected) {
            throw new PrivacyException(message);
        }
    }

    public void restorePersistenceState(
            Long id,
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
        this.id = id;
        this.status = status;
        this.assessmentVersion = assessmentVersion;
        this.previousVersionId = previousVersionId;
        this.reviewedByUserId = reviewedByUserId;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getProcessingOperationId() {
        return processingOperationId;
    }

    public String getPurpose() {
        return purpose;
    }

    public LegalBasisType getLegalBasis() {
        return legalBasis;
    }

    public String getJustification() {
        return justification;
    }

    public String getPersonalDataCategories() {
        return personalDataCategories;
    }

    public String getNecessityAssessment() {
        return necessityAssessment;
    }

    public String getLegalReference() {
        return legalReference;
    }

    public String getLegalObligationDescription() {
        return legalObligationDescription;
    }

    public String getContractualContext() {
        return contractualContext;
    }

    public String getConsentCollectionMechanism() {
        return consentCollectionMechanism;
    }

    public String getConsentEvidenceMechanism() {
        return consentEvidenceMechanism;
    }

    public String getConsentWithdrawalMechanism() {
        return consentWithdrawalMechanism;
    }

    public String getLegitimateInterest() {
        return legitimateInterest;
    }

    public String getLegitimateExpectation() {
        return legitimateExpectation;
    }

    public String getRightsImpactAssessment() {
        return rightsImpactAssessment;
    }

    public String getSafeguards() {
        return safeguards;
    }

    public String getBalancingConclusion() {
        return balancingConclusion;
    }

    public boolean isSensitiveData() {
        return sensitiveData;
    }

    public SensitiveDataLegalBasisType getSensitiveDataLegalBasis() {
        return sensitiveDataLegalBasis;
    }

    public String getSensitiveDataIndispensability() {
        return sensitiveDataIndispensability;
    }

    public LegalBasisAssessmentStatus getStatus() {
        return status;
    }

    public int getAssessmentVersion() {
        return assessmentVersion;
    }

    public Long getPreviousVersionId() {
        return previousVersionId;
    }

    public Long getReviewedByUserId() {
        return reviewedByUserId;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
