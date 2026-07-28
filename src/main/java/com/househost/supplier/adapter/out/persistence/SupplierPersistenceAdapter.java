package com.househost.supplier.adapter.out.persistence;

import com.househost.supplier.adapter.out.persistence.entity.SupplierPersistenceMapper;
import com.househost.supplier.application.port.out.SupplierPersistencePort;
import com.househost.supplier.domain.model.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SupplierPersistenceAdapter implements SupplierPersistencePort {
    private final SupplierJpaRepository repository;

    public SupplierPersistenceAdapter(SupplierJpaRepository repository) { this.repository = repository; }

    public Supplier save(Supplier supplier) {
        supplier.prepareForSave(LocalDateTime.now());
        return SupplierPersistenceMapper.toDomain(repository.save(SupplierPersistenceMapper.toEntity(supplier)));
    }

    public Optional<Supplier> findById(Long id) {
        return repository.findOneById(id).map(SupplierPersistenceMapper::toDomain);
    }

    public List<Supplier> findAll(String name, SupplierDataRole role, SupplierRiskLevel risk,
            SupplierGovernanceStatus governanceStatus, SupplierStatus status) {
        String normalizedName = name == null ? null : name.trim().toLowerCase(Locale.ROOT);
        return repository.findAllByOrderByOfficialNameAsc().stream()
                .map(SupplierPersistenceMapper::toDomain)
                .filter(supplier -> normalizedName == null || normalizedName.isBlank()
                        || supplier.getNormalizedOfficialName().contains(normalizedName)
                        || supplier.getTradeName() != null && supplier.getTradeName().toLowerCase(Locale.ROOT).contains(normalizedName))
                .filter(supplier -> status == null || supplier.getStatus() == status)
                .filter(supplier -> role == null || supplier.getRelationshipList().stream().anyMatch(item -> item.getRole() == role))
                .filter(supplier -> risk == null || supplier.getRelationshipList().stream().anyMatch(item -> item.getRiskLevel() == risk))
                .filter(supplier -> governanceStatus == null || supplier.getRelationshipList().stream()
                        .anyMatch(item -> item.getGovernanceStatus() == governanceStatus))
                .toList();
    }

    public boolean existsByNormalizedOfficialName(String name, Long excludedId) {
        return excludedId == null ? repository.existsByNormalizedOfficialName(name)
                : repository.existsByNormalizedOfficialNameAndIdNot(name, excludedId);
    }

    public boolean existsByRegistrationIdentifier(String identifier, Long excludedId) {
        return excludedId == null ? repository.existsByRegistrationIdentifier(identifier)
                : repository.existsByRegistrationIdentifierAndIdNot(identifier, excludedId);
    }
}
