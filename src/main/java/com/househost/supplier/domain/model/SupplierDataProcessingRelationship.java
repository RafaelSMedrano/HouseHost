package com.househost.supplier.domain.model;

import com.househost.supplier.domain.exception.SupplierException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SupplierDataProcessingRelationship {

    private Long id;
    private String serviceName;
    private String description;
    private String purpose;
    private String personalDataCategories;
    private String dataSubjectCategories;
    private String processingActions;
    private SupplierDataRole role;
    private String roleAssessment;
    private String storageLocations;
    private boolean internationalTransfer;
    private String transferMechanism;
    private String retentionCriteria;
    private String deletionOrReturnProcedure;
    private String securityMeasures;
    private String incidentNotificationChannel;
    private String incidentNotificationExpectation;
    private String subOperatorInformation;
    private SupplierContractStatus contractStatus;
    private String contractReference;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private String responsibilitySummary;
    private SupplierRiskLevel riskLevel;
    private SupplierGovernanceStatus governanceStatus;
    private String assessmentNotes;
    private LocalDateTime reviewedAt;
    private Long reviewedByUserId;
    private LocalDate nextReviewDate;
    private LocalDate endedAt;
    private SupplierDataDispositionStatus dataDispositionStatus;
    private String dataDispositionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    public SupplierDataProcessingRelationship(
            Long id,
            String serviceName,
            String description,
            String purpose,
            String personalDataCategories,
            String dataSubjectCategories,
            String processingActions,
            SupplierDataRole role,
            String roleAssessment,
            String storageLocations,
            boolean internationalTransfer,
            String transferMechanism,
            String retentionCriteria,
            String deletionOrReturnProcedure,
            String securityMeasures,
            String incidentNotificationChannel,
            String incidentNotificationExpectation,
            String subOperatorInformation,
            SupplierContractStatus contractStatus,
            String contractReference,
            LocalDate contractStartDate,
            LocalDate contractEndDate,
            String responsibilitySummary,
            SupplierRiskLevel riskLevel,
            SupplierGovernanceStatus governanceStatus,
            String assessmentNotes,
            LocalDate nextReviewDate,
            LocalDate endedAt,
            SupplierDataDispositionStatus dataDispositionStatus,
            String dataDispositionNotes
    ) {
        this(id, serviceName, description, purpose, personalDataCategories,
                dataSubjectCategories, processingActions, role, roleAssessment,
                storageLocations, internationalTransfer, transferMechanism,
                retentionCriteria, deletionOrReturnProcedure, securityMeasures,
                incidentNotificationChannel, incidentNotificationExpectation,
                subOperatorInformation, contractStatus, contractReference,
                contractStartDate, contractEndDate, responsibilitySummary,
                riskLevel, governanceStatus, assessmentNotes, nextReviewDate,
                endedAt, dataDispositionStatus, dataDispositionNotes, null, null);
    }

    public SupplierDataProcessingRelationship(
            Long id, String serviceName, String description, String purpose,
            String personalDataCategories, String dataSubjectCategories,
            String processingActions, SupplierDataRole role, String roleAssessment,
            String storageLocations, boolean internationalTransfer, String transferMechanism,
            String retentionCriteria, String deletionOrReturnProcedure, String securityMeasures,
            String incidentNotificationChannel, String incidentNotificationExpectation,
            String subOperatorInformation, SupplierContractStatus contractStatus,
            String contractReference, LocalDate contractStartDate, LocalDate contractEndDate,
            String responsibilitySummary, SupplierRiskLevel riskLevel,
            SupplierGovernanceStatus governanceStatus, String assessmentNotes,
            LocalDate nextReviewDate, LocalDate endedAt,
            SupplierDataDispositionStatus dataDispositionStatus, String dataDispositionNotes,
            LocalDateTime reviewedAt, Long reviewedByUserId
    ) {
        this.id = id;
        this.reviewedAt = reviewedAt;
        this.reviewedByUserId = reviewedByUserId;
        updateDetails(serviceName, description, purpose, personalDataCategories,
                dataSubjectCategories, processingActions, role, roleAssessment,
                storageLocations, internationalTransfer, transferMechanism,
                retentionCriteria, deletionOrReturnProcedure, securityMeasures,
                incidentNotificationChannel, incidentNotificationExpectation,
                subOperatorInformation, contractStatus, contractReference,
                contractStartDate, contractEndDate, responsibilitySummary,
                riskLevel, governanceStatus, assessmentNotes, nextReviewDate,
                endedAt, dataDispositionStatus, dataDispositionNotes);
    }

    public void updateDetails(
            String serviceName, String description, String purpose,
            String personalDataCategories, String dataSubjectCategories,
            String processingActions, SupplierDataRole role, String roleAssessment,
            String storageLocations, boolean internationalTransfer,
            String transferMechanism, String retentionCriteria,
            String deletionOrReturnProcedure, String securityMeasures,
            String incidentNotificationChannel, String incidentNotificationExpectation,
            String subOperatorInformation, SupplierContractStatus contractStatus,
            String contractReference, LocalDate contractStartDate,
            LocalDate contractEndDate, String responsibilitySummary,
            SupplierRiskLevel riskLevel, SupplierGovernanceStatus governanceStatus,
            String assessmentNotes, LocalDate nextReviewDate, LocalDate endedAt,
            SupplierDataDispositionStatus dataDispositionStatus,
            String dataDispositionNotes
    ) {
        this.serviceName = serviceName;
        this.description = description;
        this.purpose = purpose;
        this.personalDataCategories = personalDataCategories;
        this.dataSubjectCategories = dataSubjectCategories;
        this.processingActions = processingActions;
        this.role = role;
        this.roleAssessment = roleAssessment;
        this.storageLocations = storageLocations;
        this.internationalTransfer = internationalTransfer;
        this.transferMechanism = transferMechanism;
        this.retentionCriteria = retentionCriteria;
        this.deletionOrReturnProcedure = deletionOrReturnProcedure;
        this.securityMeasures = securityMeasures;
        this.incidentNotificationChannel = incidentNotificationChannel;
        this.incidentNotificationExpectation = incidentNotificationExpectation;
        this.subOperatorInformation = subOperatorInformation;
        this.contractStatus = contractStatus == null ? SupplierContractStatus.NOT_REVIEWED : contractStatus;
        this.contractReference = contractReference;
        this.contractStartDate = contractStartDate;
        this.contractEndDate = contractEndDate;
        this.responsibilitySummary = responsibilitySummary;
        this.riskLevel = riskLevel == null ? SupplierRiskLevel.MEDIUM : riskLevel;
        this.governanceStatus = governanceStatus == null ? SupplierGovernanceStatus.DRAFT : governanceStatus;
        this.assessmentNotes = assessmentNotes;
        this.nextReviewDate = nextReviewDate;
        this.endedAt = endedAt;
        this.dataDispositionStatus = dataDispositionStatus == null
                ? SupplierDataDispositionStatus.NOT_APPLICABLE : dataDispositionStatus;
        this.dataDispositionNotes = dataDispositionNotes;
        validateConsistency();
    }

    public void review(SupplierGovernanceStatus governanceStatus, SupplierRiskLevel riskLevel,
            String assessmentNotes, LocalDate nextReviewDate, Long reviewerId, LocalDateTime reviewedAt) {
        this.governanceStatus = governanceStatus;
        this.riskLevel = riskLevel;
        this.assessmentNotes = assessmentNotes;
        this.nextReviewDate = nextReviewDate;
        this.reviewedByUserId = reviewerId;
        this.reviewedAt = reviewedAt;
        validateConsistency();
    }

    public void validateConsistency() {
        require(serviceName, "O nome do servico do fornecedor e obrigatorio.");
        if (role == null) {
            throw new SupplierException("O papel LGPD da relacao e obrigatorio.");
        }
        if (role == SupplierDataRole.NO_PERSONAL_DATA) {
            if (hasText(personalDataCategories) || hasText(dataSubjectCategories) || hasText(processingActions)) {
                throw new SupplierException("Uma relacao sem dados pessoais nao pode informar dados ou acoes de tratamento.");
            }
        } else {
            require(purpose, "A finalidade do tratamento e obrigatoria.");
            require(roleAssessment, "A justificativa do papel LGPD e obrigatoria.");
            require(storageLocations, "A localizacao dos dados e obrigatoria.");
            require(retentionCriteria, "O criterio de retencao e obrigatorio.");
            require(deletionOrReturnProcedure, "O procedimento de eliminacao ou devolucao e obrigatorio.");
            require(securityMeasures, "As medidas de seguranca sao obrigatorias.");
            require(responsibilitySummary, "O resumo de responsabilidades e obrigatorio.");
        }
        if (internationalTransfer) {
            require(transferMechanism, "O mecanismo de transferencia internacional e obrigatorio.");
        }
        if (governanceStatus == SupplierGovernanceStatus.APPROVED) {
            if (reviewedByUserId == null || reviewedAt == null) {
                throw new SupplierException("A aprovacao exige revisor e data de revisao.");
            }
            if (contractStatus != SupplierContractStatus.ACTIVE
                    && contractStatus != SupplierContractStatus.NOT_APPLICABLE) {
                throw new SupplierException(
                        "A aprovacao exige contrato ativo ou formalmente nao aplicavel."
                );
            }
            if (contractStatus == SupplierContractStatus.NOT_APPLICABLE) {
                require(
                        assessmentNotes,
                        "A nao aplicabilidade do contrato exige justificativa na avaliacao."
                );
            }
        }
        if (governanceStatus == SupplierGovernanceStatus.INACTIVE) {
            if (endedAt == null || dataDispositionStatus == null
                    || dataDispositionStatus == SupplierDataDispositionStatus.NOT_APPLICABLE) {
                throw new SupplierException("A inativacao exige data de termino e destino dos dados.");
            }
        }
        if (dataDispositionStatus == SupplierDataDispositionStatus.RETAINED_WITH_JUSTIFICATION) {
            require(dataDispositionNotes, "A retencao de dados exige justificativa.");
        }
    }

    public void restorePersistenceState(LocalDateTime createdAt, LocalDateTime updatedAt,
            LocalDateTime reviewedAt, Long reviewedByUserId, Long version) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.reviewedAt = reviewedAt;
        this.reviewedByUserId = reviewedByUserId;
        this.version = version;
    }

    public void prepareForSave(LocalDateTime now) {
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    private void require(String value, String message) {
        if (!hasText(value)) throw new SupplierException(message);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public Long getId() { return id; }
    public String getServiceName() { return serviceName; }
    public String getDescription() { return description; }
    public String getPurpose() { return purpose; }
    public String getPersonalDataCategories() { return personalDataCategories; }
    public String getDataSubjectCategories() { return dataSubjectCategories; }
    public String getProcessingActions() { return processingActions; }
    public SupplierDataRole getRole() { return role; }
    public String getRoleAssessment() { return roleAssessment; }
    public String getStorageLocations() { return storageLocations; }
    public boolean isInternationalTransfer() { return internationalTransfer; }
    public String getTransferMechanism() { return transferMechanism; }
    public String getRetentionCriteria() { return retentionCriteria; }
    public String getDeletionOrReturnProcedure() { return deletionOrReturnProcedure; }
    public String getSecurityMeasures() { return securityMeasures; }
    public String getIncidentNotificationChannel() { return incidentNotificationChannel; }
    public String getIncidentNotificationExpectation() { return incidentNotificationExpectation; }
    public String getSubOperatorInformation() { return subOperatorInformation; }
    public SupplierContractStatus getContractStatus() { return contractStatus; }
    public String getContractReference() { return contractReference; }
    public LocalDate getContractStartDate() { return contractStartDate; }
    public LocalDate getContractEndDate() { return contractEndDate; }
    public String getResponsibilitySummary() { return responsibilitySummary; }
    public SupplierRiskLevel getRiskLevel() { return riskLevel; }
    public SupplierGovernanceStatus getGovernanceStatus() { return governanceStatus; }
    public String getAssessmentNotes() { return assessmentNotes; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public Long getReviewedByUserId() { return reviewedByUserId; }
    public LocalDate getNextReviewDate() { return nextReviewDate; }
    public LocalDate getEndedAt() { return endedAt; }
    public SupplierDataDispositionStatus getDataDispositionStatus() { return dataDispositionStatus; }
    public String getDataDispositionNotes() { return dataDispositionNotes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}
