package com.househost.audit.adapter.out.persistence.entity;

import com.househost.audit.domain.model.AuditEvent;

public final class AuditEventPersistenceMapper {
    private AuditEventPersistenceMapper() {}

    public static AuditEvent toDomain(AuditEventJpaEntity entity) {
        if (entity == null) return null;
        AuditEvent event = new AuditEvent(entity.eventType, entity.entityType, entity.entityId,
                entity.processingOperationId, entity.actorType, entity.actorId, entity.actorLabel,
                entity.occurredAt, entity.ipAddress, entity.userAgent, entity.metadataJson);
        event.restorePersistenceState(entity.id, entity.createdAt);
        return event;
    }

    public static AuditEventJpaEntity toEntity(AuditEvent event) {
        if (event == null) return null;
        AuditEventJpaEntity entity = new AuditEventJpaEntity();
        entity.id = event.getId();
        entity.eventType = event.getEventType();
        entity.entityType = event.getEntityType();
        entity.entityId = event.getEntityId();
        entity.processingOperationId = event.getProcessingOperationId();
        entity.actorType = event.getActorType();
        entity.actorId = event.getActorId();
        entity.actorLabel = event.getActorLabel();
        entity.occurredAt = event.getOccurredAt();
        entity.ipAddress = event.getIpAddress();
        entity.userAgent = event.getUserAgent();
        entity.metadataJson = event.getMetadataJson();
        entity.createdAt = event.getCreatedAt();
        return entity;
    }
}
