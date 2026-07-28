package com.househost.supplier.adapter.out.persistence.entity;

import com.househost.supplier.domain.model.*;
import java.util.List;

public final class SupplierPersistenceMapper {
    private SupplierPersistenceMapper() {}

    public static SupplierJpaEntity toEntity(Supplier supplier) {
        SupplierJpaEntity entity = new SupplierJpaEntity();
        entity.id = supplier.getId();
        entity.officialName = supplier.getOfficialName();
        entity.normalizedOfficialName = supplier.getNormalizedOfficialName();
        entity.tradeName = supplier.getTradeName();
        entity.registrationIdentifier = supplier.getRegistrationIdentifier();
        entity.website = supplier.getWebsite();
        entity.countryOfEstablishment = supplier.getCountryOfEstablishment();
        entity.businessContact = supplier.getBusinessContact();
        entity.privacyContact = supplier.getPrivacyContact();
        entity.incidentContact = supplier.getIncidentContact();
        entity.internalOwnerUserId = supplier.getInternalOwnerUserId();
        entity.status = supplier.getStatus();
        entity.createdAt = supplier.getCreatedAt();
        entity.updatedAt = supplier.getUpdatedAt();
        entity.version = supplier.getVersion();
        entity.relationshipList = new java.util.ArrayList<>(supplier.getRelationshipList().stream()
                .map(relationship -> toEntity(relationship, entity)).toList());
        return entity;
    }

    private static SupplierRelationshipJpaEntity toEntity(
            SupplierDataProcessingRelationship relationship,
            SupplierJpaEntity supplierEntity
    ) {
        SupplierRelationshipJpaEntity entity = new SupplierRelationshipJpaEntity();
        entity.id = relationship.getId(); entity.supplier = supplierEntity;
        entity.serviceName = relationship.getServiceName(); entity.description = relationship.getDescription();
        entity.purpose = relationship.getPurpose(); entity.personalDataCategories = relationship.getPersonalDataCategories();
        entity.dataSubjectCategories = relationship.getDataSubjectCategories(); entity.processingActions = relationship.getProcessingActions();
        entity.role = relationship.getRole(); entity.roleAssessment = relationship.getRoleAssessment();
        entity.storageLocations = relationship.getStorageLocations(); entity.internationalTransfer = relationship.isInternationalTransfer();
        entity.transferMechanism = relationship.getTransferMechanism(); entity.retentionCriteria = relationship.getRetentionCriteria();
        entity.deletionOrReturnProcedure = relationship.getDeletionOrReturnProcedure(); entity.securityMeasures = relationship.getSecurityMeasures();
        entity.incidentNotificationChannel = relationship.getIncidentNotificationChannel();
        entity.incidentNotificationExpectation = relationship.getIncidentNotificationExpectation();
        entity.subOperatorInformation = relationship.getSubOperatorInformation(); entity.contractStatus = relationship.getContractStatus();
        entity.contractReference = relationship.getContractReference(); entity.contractStartDate = relationship.getContractStartDate();
        entity.contractEndDate = relationship.getContractEndDate(); entity.responsibilitySummary = relationship.getResponsibilitySummary();
        entity.riskLevel = relationship.getRiskLevel(); entity.governanceStatus = relationship.getGovernanceStatus();
        entity.assessmentNotes = relationship.getAssessmentNotes(); entity.reviewedAt = relationship.getReviewedAt();
        entity.reviewedByUserId = relationship.getReviewedByUserId(); entity.nextReviewDate = relationship.getNextReviewDate();
        entity.endedAt = relationship.getEndedAt(); entity.dataDispositionStatus = relationship.getDataDispositionStatus();
        entity.dataDispositionNotes = relationship.getDataDispositionNotes(); entity.createdAt = relationship.getCreatedAt();
        entity.updatedAt = relationship.getUpdatedAt(); entity.version = relationship.getVersion();
        return entity;
    }

    public static Supplier toDomain(SupplierJpaEntity entity) {
        List<SupplierDataProcessingRelationship> relationshipList = entity.relationshipList.stream()
                .map(SupplierPersistenceMapper::toDomain).toList();
        Supplier supplier = new Supplier(entity.id, entity.officialName, entity.normalizedOfficialName,
                entity.tradeName, entity.registrationIdentifier, entity.website,
                entity.countryOfEstablishment, entity.businessContact, entity.privacyContact,
                entity.incidentContact, entity.internalOwnerUserId, entity.status, relationshipList);
        supplier.restorePersistenceState(entity.createdAt, entity.updatedAt, entity.version);
        return supplier;
    }

    private static SupplierDataProcessingRelationship toDomain(SupplierRelationshipJpaEntity entity) {
        SupplierDataProcessingRelationship relationship = new SupplierDataProcessingRelationship(
                entity.id, entity.serviceName, entity.description, entity.purpose,
                entity.personalDataCategories, entity.dataSubjectCategories, entity.processingActions,
                entity.role, entity.roleAssessment, entity.storageLocations,
                entity.internationalTransfer, entity.transferMechanism, entity.retentionCriteria,
                entity.deletionOrReturnProcedure, entity.securityMeasures,
                entity.incidentNotificationChannel, entity.incidentNotificationExpectation,
                entity.subOperatorInformation, entity.contractStatus, entity.contractReference,
                entity.contractStartDate, entity.contractEndDate, entity.responsibilitySummary,
                entity.riskLevel, entity.governanceStatus, entity.assessmentNotes,
                entity.nextReviewDate, entity.endedAt, entity.dataDispositionStatus,
                entity.dataDispositionNotes, entity.reviewedAt, entity.reviewedByUserId);
        relationship.restorePersistenceState(entity.createdAt, entity.updatedAt,
                entity.reviewedAt, entity.reviewedByUserId, entity.version);
        return relationship;
    }
}
