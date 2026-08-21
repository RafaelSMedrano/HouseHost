package com.househost.notifier.adapter.out.persistence.entity;

import com.househost.notifier.domain.model.NotificationEventType;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "notification_provider_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_provider_event_transport",
                        columnNames = "transport_event_id"
                ),
                @UniqueConstraint(
                        name = "uk_notification_provider_event_provider",
                        columnNames = "provider_event_id"
                )
        }
)
public class NotificationProviderEventJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_intent_id", nullable = false)
    private NotificationIntentJpaEntity notificationIntentJpaEntity;

    @Column(name = "transport_event_id", length = 255, nullable = false)
    private String transportEventId;

    @Column(name = "provider_event_id", length = 255)
    private String providerEventId;

    @Column(name = "provider_message_id", length = 255, nullable = false)
    private String providerMessageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 40, nullable = false)
    private NotificationEventType eventType;

    @Column(name = "bounce_type", length = 100)
    private String bounceType;

    @Column(name = "bounce_sub_type", length = 100)
    private String bounceSubType;

    @Column(name = "provider_status_code", length = 100)
    private String providerStatusCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_category", length = 40)
    private NotificationFailureCategory failureCategory;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "raw_event_storage_key", length = 512)
    private String rawEventStorageKey;

    public NotificationProviderEventJpaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public NotificationIntentJpaEntity getNotificationIntentJpaEntity() {
        return notificationIntentJpaEntity;
    }

    public void setNotificationIntentJpaEntity(
            NotificationIntentJpaEntity notificationIntentJpaEntity
    ) {
        this.notificationIntentJpaEntity = notificationIntentJpaEntity;
    }

    public String getTransportEventId() {
        return transportEventId;
    }

    public void setTransportEventId(String transportEventId) {
        this.transportEventId = transportEventId;
    }

    public String getProviderEventId() {
        return providerEventId;
    }

    public void setProviderEventId(String providerEventId) {
        this.providerEventId = providerEventId;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public void setProviderMessageId(String providerMessageId) {
        this.providerMessageId = providerMessageId;
    }

    public NotificationEventType getEventType() {
        return eventType;
    }

    public void setEventType(NotificationEventType eventType) {
        this.eventType = eventType;
    }

    public String getBounceType() {
        return bounceType;
    }

    public void setBounceType(String bounceType) {
        this.bounceType = bounceType;
    }

    public String getBounceSubType() {
        return bounceSubType;
    }

    public void setBounceSubType(String bounceSubType) {
        this.bounceSubType = bounceSubType;
    }

    public String getProviderStatusCode() {
        return providerStatusCode;
    }

    public void setProviderStatusCode(String providerStatusCode) {
        this.providerStatusCode = providerStatusCode;
    }

    public NotificationFailureCategory getFailureCategory() {
        return failureCategory;
    }

    public void setFailureCategory(NotificationFailureCategory failureCategory) {
        this.failureCategory = failureCategory;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public String getRawEventStorageKey() {
        return rawEventStorageKey;
    }

    public void setRawEventStorageKey(String rawEventStorageKey) {
        this.rawEventStorageKey = rawEventStorageKey;
    }
}
