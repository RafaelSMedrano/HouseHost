package com.househost.supplier.application.dto;

import com.househost.supplier.domain.model.*;
import java.time.LocalDate;

public class SupplierRelationshipRequestDTO {
    public Long id;
    public String serviceName;
    public String description;
    public String purpose;
    public String personalDataCategories;
    public String dataSubjectCategories;
    public String processingActions;
    public SupplierDataRole role;
    public String roleAssessment;
    public String storageLocations;
    public Boolean internationalTransfer;
    public String transferMechanism;
    public String retentionCriteria;
    public String deletionOrReturnProcedure;
    public String securityMeasures;
    public String incidentNotificationChannel;
    public String incidentNotificationExpectation;
    public String subOperatorInformation;
    public SupplierContractStatus contractStatus;
    public String contractReference;
    public LocalDate contractStartDate;
    public LocalDate contractEndDate;
    public String responsibilitySummary;
    public SupplierRiskLevel riskLevel;
    public SupplierGovernanceStatus governanceStatus;
    public String assessmentNotes;
    public LocalDate nextReviewDate;
    public LocalDate endedAt;
    public SupplierDataDispositionStatus dataDispositionStatus;
    public String dataDispositionNotes;
}
