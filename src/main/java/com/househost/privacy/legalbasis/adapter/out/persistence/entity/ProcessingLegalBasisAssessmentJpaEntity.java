package com.househost.privacy.legalbasis.adapter.out.persistence.entity;

import com.househost.privacy.legalbasis.domain.model.LegalBasisAssessmentStatus;
import com.househost.privacy.legalbasis.domain.model.LegalBasisType;
import com.househost.privacy.legalbasis.domain.model.SensitiveDataLegalBasisType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "processing_legal_basis_assessments", uniqueConstraints = @UniqueConstraint(
        name = "uk_legal_basis_operation_purpose_version",
        columnNames = {"processing_operation_id", "purpose_key", "assessment_version"}))
public class ProcessingLegalBasisAssessmentJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "processing_operation_id", nullable = false)
    Long processingOperationId;
    @Column(name = "purpose_key", nullable = false, length = 500)
    String purposeKey;
    @Lob @Column(nullable = false, columnDefinition = "text") String purpose;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50) LegalBasisType legalBasis;
    @Lob @Column(columnDefinition = "text") String justification;
    @Lob @Column(columnDefinition = "text") String personalDataCategories;
    @Lob @Column(columnDefinition = "text") String necessityAssessment;
    @Lob @Column(columnDefinition = "text") String legalReference;
    @Lob @Column(columnDefinition = "text") String legalObligationDescription;
    @Lob @Column(columnDefinition = "text") String contractualContext;
    @Lob @Column(columnDefinition = "text") String consentCollectionMechanism;
    @Lob @Column(columnDefinition = "text") String consentEvidenceMechanism;
    @Lob @Column(columnDefinition = "text") String consentWithdrawalMechanism;
    @Lob @Column(columnDefinition = "text") String legitimateInterest;
    @Lob @Column(columnDefinition = "text") String legitimateExpectation;
    @Lob @Column(columnDefinition = "text") String rightsImpactAssessment;
    @Lob @Column(columnDefinition = "text") String safeguards;
    @Lob @Column(columnDefinition = "text") String balancingConclusion;
    @Column(nullable = false) boolean sensitiveData;
    @Enumerated(EnumType.STRING) @Column(length = 60) SensitiveDataLegalBasisType sensitiveDataLegalBasis;
    @Lob @Column(columnDefinition = "text") String sensitiveDataIndispensability;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) LegalBasisAssessmentStatus status;
    @Column(name = "assessment_version", nullable = false) int assessmentVersion;
    Long previousVersionId;
    Long reviewedByUserId;
    LocalDateTime submittedAt;
    LocalDateTime reviewedAt;
    @Lob @Column(columnDefinition = "text") String rejectionReason;
    @Column(nullable = false, updatable = false) LocalDateTime createdAt;
    @Column(nullable = false) LocalDateTime updatedAt;
    protected ProcessingLegalBasisAssessmentJpaEntity() {}
}
