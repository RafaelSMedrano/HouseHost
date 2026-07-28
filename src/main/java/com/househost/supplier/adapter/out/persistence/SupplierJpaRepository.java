package com.househost.supplier.adapter.out.persistence;

import com.househost.supplier.adapter.out.persistence.entity.SupplierJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierJpaRepository extends JpaRepository<SupplierJpaEntity, Long> {
    @EntityGraph(attributePaths = "relationshipList")
    Optional<SupplierJpaEntity> findOneById(Long id);
    @EntityGraph(attributePaths = "relationshipList")
    List<SupplierJpaEntity> findAllByOrderByOfficialNameAsc();
    boolean existsByNormalizedOfficialNameAndIdNot(String name, Long id);
    boolean existsByNormalizedOfficialName(String name);
    boolean existsByRegistrationIdentifierAndIdNot(String identifier, Long id);
    boolean existsByRegistrationIdentifier(String identifier);
}
