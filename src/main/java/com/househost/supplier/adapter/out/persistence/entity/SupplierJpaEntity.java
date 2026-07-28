package com.househost.supplier.adapter.out.persistence.entity;

import com.househost.supplier.domain.model.SupplierStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "suppliers")
public class SupplierJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(nullable = false, length = 180) String officialName;
    @Column(nullable = false, unique = true, length = 180) String normalizedOfficialName;
    @Column(length = 180) String tradeName;
    @Column(unique = true, length = 80) String registrationIdentifier;
    @Column(length = 300) String website;
    @Column(nullable = false, length = 120) String countryOfEstablishment;
    @Column(length = 300) String businessContact;
    @Column(length = 300) String privacyContact;
    @Column(length = 300) String incidentContact;
    Long internalOwnerUserId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) SupplierStatus status;
    @Column(nullable = false, updatable = false) LocalDateTime createdAt;
    @Column(nullable = false) LocalDateTime updatedAt;
    @Version Long version;
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("id asc")
    List<SupplierRelationshipJpaEntity> relationshipList = new ArrayList<>();

    protected SupplierJpaEntity() {}
}
