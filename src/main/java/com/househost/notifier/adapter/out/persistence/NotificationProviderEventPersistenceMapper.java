package com.househost.notifier.adapter.out.persistence;

import com.househost.notifier.adapter.out.persistence.entity.NotificationIntentJpaEntity;
import com.househost.notifier.adapter.out.persistence.entity.NotificationProviderEventJpaEntity;
import com.househost.notifier.domain.model.NotificationProviderEvent;

final class NotificationProviderEventPersistenceMapper {

    private NotificationProviderEventPersistenceMapper() {
    }

    static NotificationProviderEventJpaEntity toEntity(
            NotificationProviderEvent notificationProviderEvent,
            NotificationIntentJpaEntity notificationIntentJpaEntity
    ) {
        NotificationProviderEventJpaEntity notificationProviderEventJpaEntity =
                new NotificationProviderEventJpaEntity();
        notificationProviderEventJpaEntity.setId(notificationProviderEvent.getId());
        notificationProviderEventJpaEntity.setNotificationIntentJpaEntity(
                notificationIntentJpaEntity
        );
        notificationProviderEventJpaEntity.setTransportEventId(
                notificationProviderEvent.getTransportEventId()
        );
        notificationProviderEventJpaEntity.setProviderEventId(
                notificationProviderEvent.getProviderEventId()
        );
        notificationProviderEventJpaEntity.setProviderMessageId(
                notificationProviderEvent.getProviderMessageId()
        );
        notificationProviderEventJpaEntity.setEventType(
                notificationProviderEvent.getEventType()
        );
        notificationProviderEventJpaEntity.setBounceType(
                notificationProviderEvent.getBounceType()
        );
        notificationProviderEventJpaEntity.setBounceSubType(
                notificationProviderEvent.getBounceSubType()
        );
        notificationProviderEventJpaEntity.setProviderStatusCode(
                notificationProviderEvent.getProviderStatusCode()
        );
        notificationProviderEventJpaEntity.setFailureCategory(
                notificationProviderEvent.getFailureCategory()
        );
        notificationProviderEventJpaEntity.setOccurredAt(
                notificationProviderEvent.getOccurredAt()
        );
        notificationProviderEventJpaEntity.setReceivedAt(
                notificationProviderEvent.getReceivedAt()
        );
        notificationProviderEventJpaEntity.setProcessedAt(
                notificationProviderEvent.getProcessedAt()
        );
        notificationProviderEventJpaEntity.setRawEventStorageKey(
                notificationProviderEvent.getRawEventStorageKey()
        );
        return notificationProviderEventJpaEntity;
    }

    static NotificationProviderEvent toDomain(
            NotificationProviderEventJpaEntity notificationProviderEventJpaEntity
    ) {
        return new NotificationProviderEvent(
                notificationProviderEventJpaEntity.getId(),
                notificationProviderEventJpaEntity.getNotificationIntentJpaEntity().getId(),
                notificationProviderEventJpaEntity.getTransportEventId(),
                notificationProviderEventJpaEntity.getProviderEventId(),
                notificationProviderEventJpaEntity.getProviderMessageId(),
                notificationProviderEventJpaEntity.getEventType(),
                notificationProviderEventJpaEntity.getBounceType(),
                notificationProviderEventJpaEntity.getBounceSubType(),
                notificationProviderEventJpaEntity.getProviderStatusCode(),
                notificationProviderEventJpaEntity.getFailureCategory(),
                notificationProviderEventJpaEntity.getOccurredAt(),
                notificationProviderEventJpaEntity.getReceivedAt(),
                notificationProviderEventJpaEntity.getProcessedAt(),
                notificationProviderEventJpaEntity.getRawEventStorageKey()
        );
    }
}
