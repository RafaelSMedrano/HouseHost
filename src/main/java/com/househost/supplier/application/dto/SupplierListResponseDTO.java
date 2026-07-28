package com.househost.supplier.application.dto;

import com.househost.supplier.domain.model.*;
import java.time.LocalDate;
import java.util.List;

public class SupplierListResponseDTO {
    public final Long id;
    public final String officialName;
    public final String tradeName;
    public final String principalService;
    public final List<SupplierDataRole> roleList;
    public final SupplierRiskLevel highestRisk;
    public final SupplierGovernanceStatus governanceStatus;
    public final SupplierStatus status;
    public final LocalDate nextReviewDate;
    public final boolean overdueReview;

    public SupplierListResponseDTO(Supplier supplier, LocalDate today) {
        List<SupplierDataProcessingRelationship> relationshipList = supplier.getRelationshipList();
        id = supplier.getId();
        officialName = supplier.getOfficialName();
        tradeName = supplier.getTradeName();
        principalService = relationshipList.get(0).getServiceName();
        roleList = relationshipList.stream().map(SupplierDataProcessingRelationship::getRole).distinct().toList();
        highestRisk = relationshipList.stream().map(SupplierDataProcessingRelationship::getRiskLevel)
                .max(Enum::compareTo).orElse(SupplierRiskLevel.LOW);
        governanceStatus = relationshipList.stream().map(SupplierDataProcessingRelationship::getGovernanceStatus)
                .max(Enum::compareTo).orElse(SupplierGovernanceStatus.DRAFT);
        status = supplier.getStatus();
        nextReviewDate = relationshipList.stream().map(SupplierDataProcessingRelationship::getNextReviewDate)
                .filter(date -> date != null).min(LocalDate::compareTo).orElse(null);
        overdueReview = nextReviewDate != null && nextReviewDate.isBefore(today);
    }
}
