package com.househost.supplier.adapter.out.persistence.entity;

import com.househost.supplier.domain.model.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_data_processing_relationships")
public class SupplierRelationshipJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "supplier_id") SupplierJpaEntity supplier;
    @Column(nullable = false, length = 180) String serviceName;
    @Lob String description;
    @Lob String purpose;
    @Lob String personalDataCategories;
    @Lob String dataSubjectCategories;
    @Lob String processingActions;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) SupplierDataRole role;
    @Lob String roleAssessment;
    @Lob String storageLocations;
    @Column(nullable = false) boolean internationalTransfer;
    @Lob String transferMechanism;
    @Lob String retentionCriteria;
    @Lob String deletionOrReturnProcedure;
    @Lob String securityMeasures;
    @Column(length = 300) String incidentNotificationChannel;
    @Lob String incidentNotificationExpectation;
    @Lob String subOperatorInformation;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) SupplierContractStatus contractStatus;
    @Column(length = 300) String contractReference;
    LocalDate contractStartDate;
    LocalDate contractEndDate;
    @Lob String responsibilitySummary;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) SupplierRiskLevel riskLevel;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) SupplierGovernanceStatus governanceStatus;
    @Lob String assessmentNotes;
    LocalDateTime reviewedAt;
    Long reviewedByUserId;
    LocalDate nextReviewDate;
    LocalDate endedAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) SupplierDataDispositionStatus dataDispositionStatus;
    @Lob String dataDispositionNotes;
    @Column(nullable = false, updatable = false) LocalDateTime createdAt;
    @Column(nullable = false) LocalDateTime updatedAt;
    @Version Long version;

    protected SupplierRelationshipJpaEntity() {}
}
