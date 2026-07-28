package com.househost.supplier.domain.model;

import com.househost.supplier.domain.exception.SupplierException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Supplier {

    private Long id;
    private String officialName;
    private String normalizedOfficialName;
    private String tradeName;
    private String registrationIdentifier;
    private String website;
    private String countryOfEstablishment;
    private String businessContact;
    private String privacyContact;
    private String incidentContact;
    private Long internalOwnerUserId;
    private SupplierStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    private List<SupplierDataProcessingRelationship> relationshipList;

    public Supplier(Long id, String officialName, String normalizedOfficialName,
            String tradeName, String registrationIdentifier, String website,
            String countryOfEstablishment, String businessContact,
            String privacyContact, String incidentContact, Long internalOwnerUserId,
            SupplierStatus status,
            List<SupplierDataProcessingRelationship> relationshipList) {
        this.id = id;
        updateDetails(officialName, normalizedOfficialName, tradeName,
                registrationIdentifier, website, countryOfEstablishment,
                businessContact, privacyContact, incidentContact,
                internalOwnerUserId, status, relationshipList);
    }

    public void updateDetails(String officialName, String normalizedOfficialName,
            String tradeName, String registrationIdentifier, String website,
            String countryOfEstablishment, String businessContact,
            String privacyContact, String incidentContact, Long internalOwnerUserId,
            SupplierStatus status,
            List<SupplierDataProcessingRelationship> relationshipList) {
        if (officialName == null || officialName.isBlank()) {
            throw new SupplierException("O nome oficial do fornecedor e obrigatorio.");
        }
        if (relationshipList == null || relationshipList.isEmpty()) {
            throw new SupplierException("O fornecedor deve possuir ao menos uma relacao de servico.");
        }
        relationshipList.forEach(SupplierDataProcessingRelationship::validateConsistency);
        this.officialName = officialName;
        this.normalizedOfficialName = normalizedOfficialName;
        this.tradeName = tradeName;
        this.registrationIdentifier = registrationIdentifier;
        this.website = website;
        this.countryOfEstablishment = countryOfEstablishment;
        this.businessContact = businessContact;
        this.privacyContact = privacyContact;
        this.incidentContact = incidentContact;
        this.internalOwnerUserId = internalOwnerUserId;
        this.status = status == null ? SupplierStatus.ACTIVE : status;
        this.relationshipList = new ArrayList<>(relationshipList);
    }

    public void changeStatus(SupplierStatus status) {
        if (status == null) throw new SupplierException("O status do fornecedor e obrigatorio.");
        this.status = status;
    }

    public SupplierDataProcessingRelationship relationship(Long relationshipId) {
        return relationshipList.stream()
                .filter(relationship -> relationship.getId().equals(relationshipId))
                .findFirst()
                .orElseThrow(() -> new SupplierException("Relacao do fornecedor nao encontrada."));
    }

    public void restorePersistenceState(LocalDateTime createdAt, LocalDateTime updatedAt, Long version) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public void prepareForSave(LocalDateTime now) {
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        relationshipList.forEach(relationship -> relationship.prepareForSave(now));
    }

    public Long getId() { return id; }
    public String getOfficialName() { return officialName; }
    public String getNormalizedOfficialName() { return normalizedOfficialName; }
    public String getTradeName() { return tradeName; }
    public String getRegistrationIdentifier() { return registrationIdentifier; }
    public String getWebsite() { return website; }
    public String getCountryOfEstablishment() { return countryOfEstablishment; }
    public String getBusinessContact() { return businessContact; }
    public String getPrivacyContact() { return privacyContact; }
    public String getIncidentContact() { return incidentContact; }
    public Long getInternalOwnerUserId() { return internalOwnerUserId; }
    public SupplierStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
    public List<SupplierDataProcessingRelationship> getRelationshipList() { return List.copyOf(relationshipList); }
}
