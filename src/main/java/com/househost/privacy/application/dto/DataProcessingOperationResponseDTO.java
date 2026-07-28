package com.househost.privacy.application.dto;

import com.househost.privacy.legalbasis.application.dto.DataProcessingOperationLegalBasisSummaryDTO;
import com.househost.privacy.legalbasis.domain.model.LegalBasisReadiness;
import com.househost.privacy.processing.application.dto.ProcessingOperationResponseDTO;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import java.time.LocalDateTime;
import java.util.List;

public class DataProcessingOperationResponseDTO {

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
    private final LegalBasisReadiness legalBasisReadiness;
    private final List<DataProcessingOperationLegalBasisSummaryDTO> legalBasisAssessmentList;

    public DataProcessingOperationResponseDTO(ProcessingOperationResponseDTO processingOperationResponseDTO) {
        this(processingOperationResponseDTO, LegalBasisReadiness.NOT_ASSESSED, List.of());
    }

    public DataProcessingOperationResponseDTO(
            ProcessingOperationResponseDTO processingOperationResponseDTO,
            LegalBasisReadiness legalBasisReadiness,
            List<DataProcessingOperationLegalBasisSummaryDTO> legalBasisAssessmentList
    ) {
        this.id = processingOperationResponseDTO.getId();
        this.operationCode = processingOperationResponseDTO.getOperationCode();
        this.operationName = processingOperationResponseDTO.getOperationName();
        this.description = processingOperationResponseDTO.getDescription();
        this.purpose = processingOperationResponseDTO.getPurpose();
        this.legalBasis = processingOperationResponseDTO.getLegalBasis();
        this.dataSubjectCategories = processingOperationResponseDTO.getDataSubjectCategories();
        this.personalDataCategories = processingOperationResponseDTO.getPersonalDataCategories();
        this.dataSource = processingOperationResponseDTO.getDataSource();
        this.processingActions = processingOperationResponseDTO.getProcessingActions();
        this.internalAccessRoles = processingOperationResponseDTO.getInternalAccessRoles();
        this.externalRecipients = processingOperationResponseDTO.getExternalRecipients();
        this.internationalTransfer = processingOperationResponseDTO.getInternationalTransfer();
        this.retentionPeriod = processingOperationResponseDTO.getRetentionPeriod();
        this.deletionMethod = processingOperationResponseDTO.getDeletionMethod();
        this.securityMeasures = processingOperationResponseDTO.getSecurityMeasures();
        this.responsibleArea = processingOperationResponseDTO.getResponsibleArea();
        this.systemName = processingOperationResponseDTO.getSystemName();
        this.status = processingOperationResponseDTO.getStatus();
        this.createdAt = processingOperationResponseDTO.getCreatedAt();
        this.updatedAt = processingOperationResponseDTO.getUpdatedAt();
        this.reviewedAt = processingOperationResponseDTO.getReviewedAt();
        this.reviewedByUserId = processingOperationResponseDTO.getReviewedByUserId();
        this.legalBasisReadiness = legalBasisReadiness;
        this.legalBasisAssessmentList = List.copyOf(legalBasisAssessmentList);
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

    public LegalBasisReadiness getLegalBasisReadiness() {
        return legalBasisReadiness;
    }

    public List<DataProcessingOperationLegalBasisSummaryDTO> getLegalBasisAssessmentList() {
        return legalBasisAssessmentList;
    }
}
