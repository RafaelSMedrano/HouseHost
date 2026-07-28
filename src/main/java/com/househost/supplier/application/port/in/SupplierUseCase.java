package com.househost.supplier.application.port.in;

import com.househost.supplier.application.dto.*;
import com.househost.supplier.domain.model.*;
import java.util.List;

public interface SupplierUseCase {
    SupplierDetailResponseDTO create(SupplierRequestDTO request);
    List<SupplierListResponseDTO> findAll(String name, SupplierDataRole role,
            SupplierRiskLevel risk, SupplierGovernanceStatus governanceStatus,
            SupplierStatus status);
    SupplierDetailResponseDTO findById(Long id);
    SupplierDetailResponseDTO update(Long id, SupplierRequestDTO request);
    SupplierDetailResponseDTO changeStatus(Long id, SupplierStatusRequestDTO request);
    SupplierDetailResponseDTO reviewRelationship(Long id, Long relationshipId,
            SupplierReviewRequestDTO request, String reviewerEmail);
}
