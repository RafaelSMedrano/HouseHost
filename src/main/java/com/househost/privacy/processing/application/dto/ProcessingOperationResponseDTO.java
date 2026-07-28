package com.househost.privacy.processing.application.dto;

import com.househost.privacy.processing.domain.model.DataProcessingOperation;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import java.time.LocalDateTime;

public class ProcessingOperationResponseDTO {
    private final Long id;
    private final String operationCode;
    private final String operationName;
    private final String description;
    private final String purpose;
    private final String legalBasis;
    private final String dataSubjectCategories;
    private final String personalDataCategories;
    private final String dataSource;
    private final String processingActions;
    private final String internalAccessRoles;
    private final String externalRecipients;
    private final Boolean internationalTransfer;
    private final String retentionPeriod;
    private final String deletionMethod;
    private final String securityMeasures;
    private final String responsibleArea;
    private final String systemName;
    private final DataProcessingOperationStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime reviewedAt;
    private final Long reviewedByUserId;

    public ProcessingOperationResponseDTO(DataProcessingOperation processingOperation) {
        this.id = processingOperation.getId();
        this.operationCode = processingOperation.getOperationCode();
        this.operationName = processingOperation.getOperationName();
        this.description = processingOperation.getDescription();
        this.purpose = processingOperation.getPurpose();
        this.legalBasis = processingOperation.getLegalBasis();
        this.dataSubjectCategories = processingOperation.getDataSubjectCategories();
        this.personalDataCategories = processingOperation.getPersonalDataCategories();
        this.dataSource = processingOperation.getDataSource();
        this.processingActions = processingOperation.getProcessingActions();
        this.internalAccessRoles = processingOperation.getInternalAccessRoles();
        this.externalRecipients = processingOperation.getExternalRecipients();
        this.internationalTransfer = processingOperation.getInternationalTransfer();
        this.retentionPeriod = processingOperation.getRetentionPeriod();
        this.deletionMethod = processingOperation.getDeletionMethod();
        this.securityMeasures = processingOperation.getSecurityMeasures();
        this.responsibleArea = processingOperation.getResponsibleArea();
        this.systemName = processingOperation.getSystemName();
        this.status = processingOperation.getStatus();
        this.createdAt = processingOperation.getCreatedAt();
        this.updatedAt = processingOperation.getUpdatedAt();
        this.reviewedAt = processingOperation.getReviewedAt();
        this.reviewedByUserId = processingOperation.getReviewedByUserId();
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
