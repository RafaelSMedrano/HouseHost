package com.househost.notifier.application.service;

import com.househost.notifier.application.port.in.NotificationFeedbackUseCase;
import com.househost.notifier.application.port.out.NotificationFeedbackTransactionPort;
import com.househost.notifier.application.port.out.NotificationIntentPersistencePort;
import com.househost.notifier.application.port.out.NotificationOperationalEventPort;
import com.househost.notifier.application.port.out.NotificationProviderEventPersistencePort;
import com.househost.notifier.application.records.NotificationFeedbackRecord;
import com.househost.notifier.domain.model.NotificationEventType;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import com.househost.notifier.domain.model.NotificationIntent;
import com.househost.notifier.domain.model.NotificationProviderEvent;
import com.househost.notifier.domain.model.NotificationStatus;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class NotificationFeedbackService implements NotificationFeedbackUseCase {

    private final NotificationIntentPersistencePort notificationIntentPersistencePort;
    private final NotificationProviderEventPersistencePort
            notificationProviderEventPersistencePort;
    private final NotificationFeedbackTransactionPort notificationFeedbackTransactionPort;
    private final NotificationOperationalEventPort notificationOperationalEventPort;
    private final Clock clock;

    public NotificationFeedbackService(
            NotificationIntentPersistencePort notificationIntentPersistencePort,
            NotificationProviderEventPersistencePort
                    notificationProviderEventPersistencePort,
            NotificationFeedbackTransactionPort notificationFeedbackTransactionPort,
            NotificationOperationalEventPort notificationOperationalEventPort,
            Clock clock
    ) {
        this.notificationIntentPersistencePort = notificationIntentPersistencePort;
        this.notificationProviderEventPersistencePort =
                notificationProviderEventPersistencePort;
        this.notificationFeedbackTransactionPort = notificationFeedbackTransactionPort;
        this.notificationOperationalEventPort = notificationOperationalEventPort;
        this.clock = clock;
    }

    @Override
    public void processFeedback(NotificationFeedbackRecord notificationFeedbackRecord) {
        notificationFeedbackTransactionPort.execute(
                () -> processFeedbackAtomically(notificationFeedbackRecord)
        );
    }

    private void processFeedbackAtomically(
            NotificationFeedbackRecord notificationFeedbackRecord
    ) {
        Optional<NotificationIntent> notificationIntentOptional =
                notificationIntentPersistencePort.findByProviderMessageIdOptional(
                        notificationFeedbackRecord.providerMessageId()
                );
        if (notificationIntentOptional.isEmpty()) {
            notificationOperationalEventPort.recordFeedbackUnmatched(
                    notificationFeedbackRecord.eventType()
            );
            return;
        }

        NotificationIntent notificationIntent = notificationIntentOptional.orElseThrow();
        Instant processedAt = clock.instant();
        NotificationProviderEvent candidateNotificationProviderEvent =
                toProviderEvent(
                        notificationIntent.getId(),
                        notificationFeedbackRecord,
                        processedAt
                );
        NotificationProviderEvent persistedNotificationProviderEvent =
                notificationProviderEventPersistencePort.appendIfAbsent(
                        candidateNotificationProviderEvent
                );
        if (!persistedNotificationProviderEvent.getId()
                .equals(candidateNotificationProviderEvent.getId())) {
            return;
        }

        NotificationStatus previousNotificationStatus = notificationIntent.getStatus();
        applyStateTransition(notificationIntent, notificationFeedbackRecord);
        boolean stateChanged = notificationIntent.getStatus() != previousNotificationStatus;
        if (stateChanged) {
            notificationIntentPersistencePort.save(notificationIntent);
        }
        notificationOperationalEventPort.recordFeedbackProcessed(
                notificationIntent.getId(),
                notificationFeedbackRecord.eventType(),
                notificationIntent.getStatus(),
                stateChanged
        );
    }

    private NotificationProviderEvent toProviderEvent(
            UUID notificationIntentId,
            NotificationFeedbackRecord notificationFeedbackRecord,
            Instant processedAt
    ) {
        return new NotificationProviderEvent(
                UUID.randomUUID(),
                notificationIntentId,
                notificationFeedbackRecord.transportEventId(),
                notificationFeedbackRecord.providerEventId(),
                notificationFeedbackRecord.providerMessageId(),
                notificationFeedbackRecord.eventType(),
                notificationFeedbackRecord.bounceType(),
                notificationFeedbackRecord.bounceSubType(),
                notificationFeedbackRecord.providerStatusCode(),
                notificationFeedbackRecord.failureCategory(),
                notificationFeedbackRecord.occurredAt(),
                notificationFeedbackRecord.receivedAt(),
                processedAt,
                notificationFeedbackRecord.rawEventStorageKey()
        );
    }

    private void applyStateTransition(
            NotificationIntent notificationIntent,
            NotificationFeedbackRecord notificationFeedbackRecord
    ) {
        if (notificationFeedbackRecord.occurredAt()
                .isBefore(notificationIntent.getUpdatedAt())) {
            return;
        }
        NotificationEventType notificationEventType = notificationFeedbackRecord.eventType();
        if (notificationEventType == NotificationEventType.DELIVERY
                && notificationIntent.getStatus() == NotificationStatus.ACCEPTED) {
            notificationIntent.markDelivered(notificationFeedbackRecord.occurredAt());
            return;
        }
        if ((notificationEventType == NotificationEventType.BOUNCE
                || notificationEventType == NotificationEventType.REJECT
                || notificationEventType == NotificationEventType.RENDERING_FAILURE)
                && notificationIntent.getStatus() == NotificationStatus.ACCEPTED) {
            notificationIntent.markBounced(
                    failureCategoryOrUnknown(notificationFeedbackRecord),
                    notificationFeedbackRecord.occurredAt()
            );
            return;
        }
        if (notificationEventType == NotificationEventType.COMPLAINT
                && (notificationIntent.getStatus() == NotificationStatus.ACCEPTED
                || notificationIntent.getStatus() == NotificationStatus.DELIVERED)) {
            notificationIntent.markComplaint(
                    failureCategoryOrUnknown(notificationFeedbackRecord),
                    notificationFeedbackRecord.occurredAt()
            );
        }
    }

    private NotificationFailureCategory failureCategoryOrUnknown(
            NotificationFeedbackRecord notificationFeedbackRecord
    ) {
        return notificationFeedbackRecord.failureCategory() == null
                ? NotificationFailureCategory.UNKNOWN
                : notificationFeedbackRecord.failureCategory();
    }
}
