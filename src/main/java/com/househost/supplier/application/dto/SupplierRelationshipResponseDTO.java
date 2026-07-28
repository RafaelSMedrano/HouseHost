package com.househost.supplier.application.dto;

import com.househost.supplier.domain.model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SupplierRelationshipResponseDTO {
    public final Long id;
    public final String serviceName;
    public final String description;
    public final String purpose;
    public final String personalDataCategories;
    public final String dataSubjectCategories;
    public final String processingActions;
    public final SupplierDataRole role;
    public final String roleAssessment;
    public final String storageLocations;
    public final boolean internationalTransfer;
    public final String transferMechanism;
    public final String retentionCriteria;
    public final String deletionOrReturnProcedure;
    public final String securityMeasures;
    public final String incidentNotificationChannel;
    public final String incidentNotificationExpectation;
    public final String subOperatorInformation;
    public final SupplierContractStatus contractStatus;
    public final String contractReference;
    public final LocalDate contractStartDate;
    public final LocalDate contractEndDate;
    public final String responsibilitySummary;
    public final SupplierRiskLevel riskLevel;
    public final SupplierGovernanceStatus governanceStatus;
    public final String assessmentNotes;
    public final LocalDateTime reviewedAt;
    public final Long reviewedByUserId;
    public final LocalDate nextReviewDate;
    public final LocalDate endedAt;
    public final SupplierDataDispositionStatus dataDispositionStatus;
    public final String dataDispositionNotes;

    public SupplierRelationshipResponseDTO(SupplierDataProcessingRelationship relationship) {
        id = relationship.getId();
        serviceName = relationship.getServiceName();
        description = relationship.getDescription();
        purpose = relationship.getPurpose();
        personalDataCategories = relationship.getPersonalDataCategories();
        dataSubjectCategories = relationship.getDataSubjectCategories();
        processingActions = relationship.getProcessingActions();
        role = relationship.getRole();
        roleAssessment = relationship.getRoleAssessment();
        storageLocations = relationship.getStorageLocations();
        internationalTransfer = relationship.isInternationalTransfer();
        transferMechanism = relationship.getTransferMechanism();
        retentionCriteria = relationship.getRetentionCriteria();
        deletionOrReturnProcedure = relationship.getDeletionOrReturnProcedure();
        securityMeasures = relationship.getSecurityMeasures();
        incidentNotificationChannel = relationship.getIncidentNotificationChannel();
        incidentNotificationExpectation = relationship.getIncidentNotificationExpectation();
        subOperatorInformation = relationship.getSubOperatorInformation();
        contractStatus = relationship.getContractStatus();
        contractReference = relationship.getContractReference();
        contractStartDate = relationship.getContractStartDate();
        contractEndDate = relationship.getContractEndDate();
        responsibilitySummary = relationship.getResponsibilitySummary();
        riskLevel = relationship.getRiskLevel();
        governanceStatus = relationship.getGovernanceStatus();
        assessmentNotes = relationship.getAssessmentNotes();
        reviewedAt = relationship.getReviewedAt();
        reviewedByUserId = relationship.getReviewedByUserId();
        nextReviewDate = relationship.getNextReviewDate();
        endedAt = relationship.getEndedAt();
        dataDispositionStatus = relationship.getDataDispositionStatus();
        dataDispositionNotes = relationship.getDataDispositionNotes();
    }
}
