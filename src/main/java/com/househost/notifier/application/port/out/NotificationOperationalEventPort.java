package com.househost.notifier.application.port.out;

import com.househost.notifier.domain.model.NotificationFailureCategory;
import com.househost.notifier.domain.model.NotificationEventType;
import com.househost.notifier.domain.model.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

public interface NotificationOperationalEventPort {

    void recordBatchClaimed(int claimedCount, Instant claimedAt);

    void recordOutcome(
            UUID notificationIntentId,
            String sourceSystem,
            String notificationType,
            NotificationStatus notificationStatus,
            int attemptCount,
            NotificationFailureCategory notificationFailureCategory,
            Instant nextAttemptAt
    );

    void recordClaimResultIgnored(UUID notificationIntentId, int attemptCount);

    void recordDispatchInterrupted(UUID notificationIntentId, int attemptCount);

    void recordRequeued(UUID notificationIntentId, String sourceSystem, String notificationType);

    default void recordFeedbackProcessed(
            UUID notificationIntentId,
            NotificationEventType notificationEventType,
            NotificationStatus notificationStatus,
            boolean stateChanged
    ) {
    }

    default void recordFeedbackUnmatched(NotificationEventType notificationEventType) {
    }
}
