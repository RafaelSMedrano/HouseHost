package com.househost.notifier.adapter.out.persistence.entity;

import com.househost.notifier.domain.model.NotificationChannel;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import com.househost.notifier.domain.model.NotificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "notification_intents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_intent_source_idempotency",
                        columnNames = {"source_system", "idempotency_key"}
                ),
                @UniqueConstraint(
                        name = "uk_notification_intent_provider_message",
                        columnNames = "provider_message_id"
                )
        }
)
public class NotificationIntentJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, nullable = false)
    private UUID id;

    @Column(name = "source_system", length = 100, nullable = false)
    private String sourceSystem;

    @Column(name = "external_event_id", length = 160, nullable = false)
    private String externalEventId;

    @Column(name = "idempotency_key", length = 200, nullable = false)
    private String idempotencyKey;

    @Column(name = "correlation_key", length = 200)
    private String correlationKey;

    @Column(name = "notification_type", length = 100, nullable = false)
    private String notificationType;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private NotificationChannel channel;

    @Column(name = "delivery_profile_key", length = 100, nullable = false)
    private String deliveryProfileKey;

    @Column(length = 320)
    private String recipient;

    @Column(length = 255)
    private String subject;

    @Column(name = "text_body", columnDefinition = "longtext")
    private String textBody;

    @Column(name = "html_body", columnDefinition = "longtext")
    private String htmlBody;

    @Enumerated(EnumType.STRING)
    @Column(length = 40, nullable = false)
    private NotificationStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "lease_until")
    private Instant leaseUntil;

    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_error_category", length = 40)
    private NotificationFailureCategory lastErrorCategory;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "retention_until", nullable = false)
    private Instant retentionUntil;

    @Version
    @Column(nullable = false)
    private Long version;

    public NotificationIntentJpaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getExternalEventId() {
        return externalEventId;
    }

    public void setExternalEventId(String externalEventId) {
        this.externalEventId = externalEventId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public String getDeliveryProfileKey() {
        return deliveryProfileKey;
    }

    public void setDeliveryProfileKey(String deliveryProfileKey) {
        this.deliveryProfileKey = deliveryProfileKey;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTextBody() {
        return textBody;
    }

    public void setTextBody(String textBody) {
        this.textBody = textBody;
    }

    public String getHtmlBody() {
        return htmlBody;
    }

    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public void setNextAttemptAt(Instant nextAttemptAt) {
        this.nextAttemptAt = nextAttemptAt;
    }

    public Instant getLeaseUntil() {
        return leaseUntil;
    }

    public void setLeaseUntil(Instant leaseUntil) {
        this.leaseUntil = leaseUntil;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public void setProviderMessageId(String providerMessageId) {
        this.providerMessageId = providerMessageId;
    }

    public NotificationFailureCategory getLastErrorCategory() {
        return lastErrorCategory;
    }

    public void setLastErrorCategory(NotificationFailureCategory lastErrorCategory) {
        this.lastErrorCategory = lastErrorCategory;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Instant failedAt) {
        this.failedAt = failedAt;
    }

    public Instant getRetentionUntil() {
        return retentionUntil;
    }

    public void setRetentionUntil(Instant retentionUntil) {
        this.retentionUntil = retentionUntil;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
