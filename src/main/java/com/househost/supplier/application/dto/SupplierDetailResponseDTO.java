package com.househost.supplier.application.dto;

import com.househost.supplier.domain.model.Supplier;
import com.househost.supplier.domain.model.SupplierStatus;
import java.time.LocalDateTime;
import java.util.List;

public class SupplierDetailResponseDTO {
    public final Long id;
    public final String officialName;
    public final String tradeName;
    public final String registrationIdentifier;
    public final String website;
    public final String countryOfEstablishment;
    public final String businessContact;
    public final String privacyContact;
    public final String incidentContact;
    public final Long internalOwnerUserId;
    public final SupplierStatus status;
    public final LocalDateTime createdAt;
    public final LocalDateTime updatedAt;
    public final List<SupplierRelationshipResponseDTO> relationshipList;

    public SupplierDetailResponseDTO(Supplier supplier) {
        id = supplier.getId();
        officialName = supplier.getOfficialName();
        tradeName = supplier.getTradeName();
        registrationIdentifier = supplier.getRegistrationIdentifier();
        website = supplier.getWebsite();
        countryOfEstablishment = supplier.getCountryOfEstablishment();
        businessContact = supplier.getBusinessContact();
        privacyContact = supplier.getPrivacyContact();
        incidentContact = supplier.getIncidentContact();
        internalOwnerUserId = supplier.getInternalOwnerUserId();
        status = supplier.getStatus();
        createdAt = supplier.getCreatedAt();
        updatedAt = supplier.getUpdatedAt();
        relationshipList = supplier.getRelationshipList().stream()
                .map(SupplierRelationshipResponseDTO::new)
                .toList();
    }
}
