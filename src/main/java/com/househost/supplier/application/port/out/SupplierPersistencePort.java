package com.househost.supplier.application.port.out;

import com.househost.supplier.domain.model.*;
import java.util.List;
import java.util.Optional;

public interface SupplierPersistencePort {
    Supplier save(Supplier supplier);
    Optional<Supplier> findById(Long id);
    List<Supplier> findAll(String name, SupplierDataRole role, SupplierRiskLevel risk,
            SupplierGovernanceStatus governanceStatus, SupplierStatus status);
    boolean existsByNormalizedOfficialName(String normalizedOfficialName, Long excludedId);
    boolean existsByRegistrationIdentifier(String registrationIdentifier, Long excludedId);
}
