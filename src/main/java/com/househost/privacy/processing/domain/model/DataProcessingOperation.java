package com.househost.privacy.processing.domain.model;

import java.time.LocalDateTime;

public class DataProcessingOperation {
    private Long id;

    private String operationCode;

    private String operationName;

    private String description;

    private String purpose;

    private String legalBasis;

    private String dataSubjectCategories;

    private String personalDataCategories;

    private String dataSource;

    private String processingActions;

    private String internalAccessRoles;

    private String externalRecipients;

    private Boolean internationalTransfer;

    private String retentionPeriod;

    private String deletionMethod;

    private String securityMeasures;

    private String responsibleArea;

    private String systemName;

    private DataProcessingOperationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime reviewedAt;

    private Long reviewedByUserId;

    public void restorePersistenceState(Long id, DataProcessingOperationStatus status, LocalDateTime createdAt,
            LocalDateTime updatedAt, LocalDateTime reviewedAt, Long reviewedByUserId) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.reviewedAt = reviewedAt;
        this.reviewedByUserId = reviewedByUserId;
    }

    public DataProcessingOperation(
            String operationCode,
            String operationName,
            String description,
            String purpose,
            String legalBasis,
            String dataSubjectCategories,
            String personalDataCategories,
            String dataSource,
            String processingActions,
            String internalAccessRoles,
            String externalRecipients,
            Boolean internationalTransfer,
            String retentionPeriod,
            String deletionMethod,
            String securityMeasures,
            String responsibleArea,
            String systemName
    ) {
        this.operationCode = operationCode;
        updateDetails(
                operationName,
                description,
                purpose,
                legalBasis,
                dataSubjectCategories,
                personalDataCategories,
                dataSource,
                processingActions,
                internalAccessRoles,
                externalRecipients,
                internationalTransfer,
                retentionPeriod,
                deletionMethod,
                securityMeasures,
                responsibleArea,
                systemName
        );
        this.status = DataProcessingOperationStatus.ACTIVE;
    }

    public DataProcessingOperation(
            String operationName,
            String description,
            String purpose,
            String legalBasis,
            String dataSubjectCategories,
            String personalDataCategories,
            String dataSource,
            String processingActions,
            String internalAccessRoles,
            String externalRecipients,
            Boolean internationalTransfer,
            String retentionPeriod,
            String deletionMethod,
            String securityMeasures,
            String responsibleArea,
            String systemName
    ) {
        this(
                null,
                operationName,
                description,
                purpose,
                legalBasis,
                dataSubjectCategories,
                personalDataCategories,
                dataSource,
                processingActions,
                internalAccessRoles,
                externalRecipients,
                internationalTransfer,
                retentionPeriod,
                deletionMethod,
                securityMeasures,
                responsibleArea,
                systemName
        );
    }

    public void updateDetails(
            String operationName,
            String description,
            String purpose,
            String legalBasis,
            String dataSubjectCategories,
            String personalDataCategories,
            String dataSource,
            String processingActions,
            String internalAccessRoles,
            String externalRecipients,
            Boolean internationalTransfer,
            String retentionPeriod,
            String deletionMethod,
            String securityMeasures,
            String responsibleArea,
            String systemName
    ) {
        this.operationName = operationName;
        this.description = description;
        this.purpose = purpose;
        this.legalBasis = legalBasis;
        this.dataSubjectCategories = dataSubjectCategories;
        this.personalDataCategories = personalDataCategories;
        this.dataSource = dataSource;
        this.processingActions = processingActions;
        this.internalAccessRoles = internalAccessRoles;
        this.externalRecipients = externalRecipients;
        this.internationalTransfer = internationalTransfer;
        this.retentionPeriod = retentionPeriod;
        this.deletionMethod = deletionMethod;
        this.securityMeasures = securityMeasures;
        this.responsibleArea = responsibleArea;
        this.systemName = systemName;
    }

    public void changeStatus(DataProcessingOperationStatus status) {
        this.status = status;
    }

    public void markReviewed(Long userId) {
        this.reviewedAt = LocalDateTime.now();
        this.reviewedByUserId = userId;
    }

    public void prepareForCreation() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = DataProcessingOperationStatus.ACTIVE;
        }
        if (internationalTransfer == null) {
            internationalTransfer = false;
        }
    }

    public void markUpdated() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getOperationCode() {
        return operationCode;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getDescription() {
        return description;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getLegalBasis() {
        return legalBasis;
    }

    public String getDataSubjectCategories() {
        return dataSubjectCategories;
    }

    public String getPersonalDataCategories() {
        return personalDataCategories;
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getProcessingActions() {
        return processingActions;
    }

    public String getInternalAccessRoles() {
        return internalAccessRoles;
    }

    public String getExternalRecipients() {
        return externalRecipients;
    }

    public Boolean getInternationalTransfer() {
        return internationalTransfer;
    }

    public String getRetentionPeriod() {
        return retentionPeriod;
    }

    public String getDeletionMethod() {
        return deletionMethod;
    }

    public String getSecurityMeasures() {
        return securityMeasures;
    }

    public String getResponsibleArea() {
        return responsibleArea;
    }

    public String getSystemName() {
        return systemName;
    }

    public DataProcessingOperationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public Long getReviewedByUserId() {
        return reviewedByUserId;
    }
}
