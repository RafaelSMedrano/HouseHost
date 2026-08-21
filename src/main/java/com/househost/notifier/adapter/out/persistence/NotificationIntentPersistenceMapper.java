package com.househost.notifier.adapter.out.persistence;

import com.househost.notifier.adapter.out.persistence.entity.NotificationIntentJpaEntity;
import com.househost.notifier.application.records.EmailMessageRecord;
import com.househost.notifier.application.records.NotificationClaimRecord;
import com.househost.notifier.domain.model.NotificationIntent;

final class NotificationIntentPersistenceMapper {

    private NotificationIntentPersistenceMapper() {
    }

    static NotificationIntentJpaEntity toEntity(NotificationIntent notificationIntent) {
        NotificationIntentJpaEntity notificationIntentJpaEntity =
                new NotificationIntentJpaEntity();
        applyToEntity(notificationIntent, notificationIntentJpaEntity);
        notificationIntentJpaEntity.setVersion(notificationIntent.getVersion());
        return notificationIntentJpaEntity;
    }

    static void applyToEntity(
            NotificationIntent notificationIntent,
            NotificationIntentJpaEntity notificationIntentJpaEntity
    ) {
        notificationIntentJpaEntity.setId(notificationIntent.getId());
        notificationIntentJpaEntity.setSourceSystem(notificationIntent.getSourceSystem());
        notificationIntentJpaEntity.setExternalEventId(notificationIntent.getExternalEventId());
        notificationIntentJpaEntity.setIdempotencyKey(notificationIntent.getIdempotencyKey());
        notificationIntentJpaEntity.setCorrelationKey(notificationIntent.getCorrelationKey());
        notificationIntentJpaEntity.setNotificationType(notificationIntent.getNotificationType());
        notificationIntentJpaEntity.setChannel(notificationIntent.getChannel());
        notificationIntentJpaEntity.setDeliveryProfileKey(
                notificationIntent.getDeliveryProfileKey()
        );
        notificationIntentJpaEntity.setRecipient(notificationIntent.getRecipient());
        notificationIntentJpaEntity.setSubject(notificationIntent.getSubject());
        notificationIntentJpaEntity.setTextBody(notificationIntent.getTextBody());
        notificationIntentJpaEntity.setHtmlBody(notificationIntent.getHtmlBody());
        notificationIntentJpaEntity.setStatus(notificationIntent.getStatus());
        notificationIntentJpaEntity.setAttemptCount(notificationIntent.getAttemptCount());
        notificationIntentJpaEntity.setNextAttemptAt(notificationIntent.getNextAttemptAt());
        notificationIntentJpaEntity.setLeaseUntil(notificationIntent.getLeaseUntil());
        notificationIntentJpaEntity.setProviderMessageId(
                notificationIntent.getProviderMessageId()
        );
        notificationIntentJpaEntity.setLastErrorCategory(
                notificationIntent.getLastErrorCategory()
        );
        notificationIntentJpaEntity.setCreatedAt(notificationIntent.getCreatedAt());
        notificationIntentJpaEntity.setUpdatedAt(notificationIntent.getUpdatedAt());
        notificationIntentJpaEntity.setAcceptedAt(notificationIntent.getAcceptedAt());
        notificationIntentJpaEntity.setDeliveredAt(notificationIntent.getDeliveredAt());
        notificationIntentJpaEntity.setFailedAt(notificationIntent.getFailedAt());
        notificationIntentJpaEntity.setRetentionUntil(notificationIntent.getRetentionUntil());
    }

    static NotificationIntent toDomain(
            NotificationIntentJpaEntity notificationIntentJpaEntity
    ) {
        return NotificationIntent.restore(
                notificationIntentJpaEntity.getId(),
                notificationIntentJpaEntity.getSourceSystem(),
                notificationIntentJpaEntity.getExternalEventId(),
                notificationIntentJpaEntity.getIdempotencyKey(),
                notificationIntentJpaEntity.getCorrelationKey(),
                notificationIntentJpaEntity.getNotificationType(),
                notificationIntentJpaEntity.getChannel(),
                notificationIntentJpaEntity.getDeliveryProfileKey(),
                notificationIntentJpaEntity.getRecipient(),
                notificationIntentJpaEntity.getSubject(),
                notificationIntentJpaEntity.getTextBody(),
                notificationIntentJpaEntity.getHtmlBody(),
                notificationIntentJpaEntity.getStatus(),
                notificationIntentJpaEntity.getAttemptCount(),
                notificationIntentJpaEntity.getNextAttemptAt(),
                notificationIntentJpaEntity.getLeaseUntil(),
                notificationIntentJpaEntity.getProviderMessageId(),
                notificationIntentJpaEntity.getLastErrorCategory(),
                notificationIntentJpaEntity.getCreatedAt(),
                notificationIntentJpaEntity.getUpdatedAt(),
                notificationIntentJpaEntity.getAcceptedAt(),
                notificationIntentJpaEntity.getDeliveredAt(),
                notificationIntentJpaEntity.getFailedAt(),
                notificationIntentJpaEntity.getRetentionUntil(),
                notificationIntentJpaEntity.getVersion()
        );
    }

    static NotificationClaimRecord toClaimRecord(NotificationIntent notificationIntent) {
        return new NotificationClaimRecord(
                notificationIntent.getId(),
                notificationIntent.getSourceSystem(),
                notificationIntent.getNotificationType(),
                notificationIntent.getDeliveryProfileKey(),
                new EmailMessageRecord(
                        notificationIntent.getRecipient(),
                        notificationIntent.getSubject(),
                        notificationIntent.getTextBody(),
                        notificationIntent.getHtmlBody()
                ),
                notificationIntent.getAttemptCount(),
                notificationIntent.getLeaseUntil()
        );
    }
}
