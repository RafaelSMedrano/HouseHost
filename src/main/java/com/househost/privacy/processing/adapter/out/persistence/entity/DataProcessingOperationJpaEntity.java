package com.househost.privacy.processing.adapter.out.persistence.entity;

import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_processing_operations")
public class DataProcessingOperationJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, length = 80)
    String operationCode;

    @Column(nullable = false, unique = true, length = 180)
    String operationName;

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    String description;

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    String purpose;

    @Column(nullable = false, length = 80)
    String legalBasis;

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    String dataSubjectCategories;

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    String personalDataCategories;

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    String dataSource;

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    String processingActions;

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    String internalAccessRoles;

    @Lob
    @Column(columnDefinition = "text")
    String externalRecipients;

    @Column(nullable = false)
    Boolean internationalTransfer;

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    String retentionPeriod;

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    String deletionMethod;

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    String securityMeasures;

    @Column(nullable = false, length = 120)
    String responsibleArea;

    @Column(nullable = false, length = 120)
    String systemName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    DataProcessingOperationStatus status;

    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;

    LocalDateTime reviewedAt;

    Long reviewedByUserId;

    protected DataProcessingOperationJpaEntity() {
    }
}
