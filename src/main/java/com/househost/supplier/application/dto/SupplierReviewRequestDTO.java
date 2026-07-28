package com.househost.supplier.application.dto;

import com.househost.supplier.domain.model.SupplierGovernanceStatus;
import com.househost.supplier.domain.model.SupplierRiskLevel;
import java.time.LocalDate;

public class SupplierReviewRequestDTO {
    public SupplierGovernanceStatus governanceStatus;
    public SupplierRiskLevel riskLevel;
    public String assessmentNotes;
    public LocalDate nextReviewDate;
}
