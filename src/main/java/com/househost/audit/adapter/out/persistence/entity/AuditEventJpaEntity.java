package com.househost.audit.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events")
public class AuditEventJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(nullable = false, length = 80) String eventType;
    @Column(nullable = false, length = 80) String entityType;
    Long entityId;
    @Column(name = "processing_operation_id") Long processingOperationId;
    @Column(nullable = false, length = 80) String actorType;
    Long actorId;
    @Column(length = 180) String actorLabel;
    @Column(nullable = false) LocalDateTime occurredAt;
    @Column(length = 80) String ipAddress;
    @Column(length = 500) String userAgent;
    @Lob @Column(columnDefinition = "longtext") String metadataJson;
    @Column(nullable = false, updatable = false) LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (occurredAt == null) occurredAt = LocalDateTime.now();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
