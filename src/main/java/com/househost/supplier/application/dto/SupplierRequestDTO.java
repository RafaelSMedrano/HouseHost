package com.househost.supplier.application.dto;

import com.househost.supplier.domain.model.SupplierStatus;
import java.util.List;

public class SupplierRequestDTO {
    public String officialName;
    public String tradeName;
    public String registrationIdentifier;
    public String website;
    public String countryOfEstablishment;
    public String businessContact;
    public String privacyContact;
    public String incidentContact;
    public Long internalOwnerUserId;
    public SupplierStatus status;
    public List<SupplierRelationshipRequestDTO> relationshipList;
}
