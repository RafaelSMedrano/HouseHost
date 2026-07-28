package com.househost.audit.domain.model;

import java.time.LocalDateTime;

public class AuditEvent {
    private Long id;
    private final String eventType;
    private final String entityType;
    private final Long entityId;
    private final Long processingOperationId;
    private final String actorType;
    private final Long actorId;
    private final String actorLabel;
    private final LocalDateTime occurredAt;
    private final String ipAddress;
    private final String userAgent;
    private final String metadataJson;
    private LocalDateTime createdAt;

    public AuditEvent(String eventType, String entityType, Long entityId, Long processingOperationId,
                      String actorType, Long actorId, String actorLabel, LocalDateTime occurredAt,
                      String ipAddress, String userAgent, String metadataJson) {
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.processingOperationId = processingOperationId;
        this.actorType = actorType;
        this.actorId = actorId;
        this.actorLabel = actorLabel;
        this.occurredAt = occurredAt == null ? LocalDateTime.now() : occurredAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.metadataJson = metadataJson;
    }

    public void restorePersistenceState(Long id, LocalDateTime createdAt) { this.id = id; this.createdAt = createdAt; }
    public Long getId() { return id; }
    public String getEventType() { return eventType; }
    public String getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
    public Long getProcessingOperationId() { return processingOperationId; }
    public String getActorType() { return actorType; }
    public Long getActorId() { return actorId; }
    public String getActorLabel() { return actorLabel; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public String getMetadataJson() { return metadataJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
