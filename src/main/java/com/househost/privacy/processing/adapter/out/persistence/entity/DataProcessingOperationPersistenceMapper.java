package com.househost.privacy.processing.adapter.out.persistence.entity;

import com.househost.privacy.processing.domain.model.DataProcessingOperation;

public final class DataProcessingOperationPersistenceMapper {
    private DataProcessingOperationPersistenceMapper() {
    }

    public static DataProcessingOperation toDomain(
            DataProcessingOperationJpaEntity operationJpaEntity
    ) {
        DataProcessingOperation operation = new DataProcessingOperation(
                operationJpaEntity.operationCode,
                operationJpaEntity.operationName,
                operationJpaEntity.description,
                operationJpaEntity.purpose,
                operationJpaEntity.legalBasis,
                operationJpaEntity.dataSubjectCategories,
                operationJpaEntity.personalDataCategories,
                operationJpaEntity.dataSource,
                operationJpaEntity.processingActions,
                operationJpaEntity.internalAccessRoles,
                operationJpaEntity.externalRecipients,
                operationJpaEntity.internationalTransfer,
                operationJpaEntity.retentionPeriod,
                operationJpaEntity.deletionMethod,
                operationJpaEntity.securityMeasures,
                operationJpaEntity.responsibleArea,
                operationJpaEntity.systemName
        );
        operation.restorePersistenceState(
                operationJpaEntity.id,
                operationJpaEntity.status,
                operationJpaEntity.createdAt,
                operationJpaEntity.updatedAt,
                operationJpaEntity.reviewedAt,
                operationJpaEntity.reviewedByUserId
        );
        return operation;
    }

    public static DataProcessingOperationJpaEntity toEntity(DataProcessingOperation operation) {
        DataProcessingOperationJpaEntity operationJpaEntity = new DataProcessingOperationJpaEntity();
        operationJpaEntity.id = operation.getId();
        operationJpaEntity.operationCode = operation.getOperationCode();
        operationJpaEntity.operationName = operation.getOperationName();
        operationJpaEntity.description = operation.getDescription();
        operationJpaEntity.purpose = operation.getPurpose();
        operationJpaEntity.legalBasis = operation.getLegalBasis();
        operationJpaEntity.dataSubjectCategories = operation.getDataSubjectCategories();
        operationJpaEntity.personalDataCategories = operation.getPersonalDataCategories();
        operationJpaEntity.dataSource = operation.getDataSource();
        operationJpaEntity.processingActions = operation.getProcessingActions();
        operationJpaEntity.internalAccessRoles = operation.getInternalAccessRoles();
        operationJpaEntity.externalRecipients = operation.getExternalRecipients();
        operationJpaEntity.internationalTransfer = operation.getInternationalTransfer();
        operationJpaEntity.retentionPeriod = operation.getRetentionPeriod();
        operationJpaEntity.deletionMethod = operation.getDeletionMethod();
        operationJpaEntity.securityMeasures = operation.getSecurityMeasures();
        operationJpaEntity.responsibleArea = operation.getResponsibleArea();
        operationJpaEntity.systemName = operation.getSystemName();
        operationJpaEntity.status = operation.getStatus();
        operationJpaEntity.createdAt = operation.getCreatedAt();
        operationJpaEntity.updatedAt = operation.getUpdatedAt();
        operationJpaEntity.reviewedAt = operation.getReviewedAt();
        operationJpaEntity.reviewedByUserId = operation.getReviewedByUserId();
        return operationJpaEntity;
    }
}
