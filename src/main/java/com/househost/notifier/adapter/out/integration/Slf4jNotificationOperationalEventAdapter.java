package com.househost.notifier.adapter.out.integration;

import com.househost.notifier.application.port.out.NotificationOperationalEventPort;
import com.househost.notifier.domain.model.NotificationEventType;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import com.househost.notifier.domain.model.NotificationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class Slf4jNotificationOperationalEventAdapter
        implements NotificationOperationalEventPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            Slf4jNotificationOperationalEventAdapter.class
    );

    @Override
    public void recordBatchClaimed(int claimedCount, Instant claimedAt) {
        if (claimedCount == 0) {
            return;
        }
        LOGGER.info(
                "event=notifier.dispatch.batch_claimed claimedCount={} claimedAt={}",
                claimedCount,
                claimedAt
        );
    }

    @Override
    public void recordOutcome(
            UUID notificationIntentId,
            String sourceSystem,
            String notificationType,
            NotificationStatus notificationStatus,
            int attemptCount,
            NotificationFailureCategory notificationFailureCategory,
            Instant nextAttemptAt
    ) {
        LOGGER.info(
                "event=notifier.dispatch.outcome intentId={} sourceSystem={} "
                        + "notificationType={} status={} attemptCount={} "
                        + "failureCategory={} nextAttemptAt={}",
                notificationIntentId,
                sourceSystem,
                notificationType,
                notificationStatus,
                attemptCount,
                notificationFailureCategory,
                nextAttemptAt
        );
    }

    @Override
    public void recordClaimResultIgnored(UUID notificationIntentId, int attemptCount) {
        LOGGER.warn(
                "event=notifier.dispatch.claim_result_ignored intentId={} attemptCount={}",
                notificationIntentId,
                attemptCount
        );
    }

    @Override
    public void recordDispatchInterrupted(UUID notificationIntentId, int attemptCount) {
        LOGGER.error(
                "event=notifier.dispatch.interrupted intentId={} attemptCount={}",
                notificationIntentId,
                attemptCount
        );
    }

    @Override
    public void recordRequeued(
            UUID notificationIntentId,
            String sourceSystem,
            String notificationType
    ) {
        LOGGER.info(
                "event=notifier.dispatch.requeued intentId={} sourceSystem={} "
                        + "notificationType={}",
                notificationIntentId,
                sourceSystem,
                notificationType
        );
    }

    @Override
    public void recordFeedbackProcessed(
            UUID notificationIntentId,
            NotificationEventType notificationEventType,
            NotificationStatus notificationStatus,
            boolean stateChanged
    ) {
        LOGGER.info(
                "event=notifier.feedback.processed intentId={} eventType={} "
                        + "status={} stateChanged={}",
                notificationIntentId,
                notificationEventType,
                notificationStatus,
                stateChanged
        );
    }

    @Override
    public void recordFeedbackUnmatched(NotificationEventType notificationEventType) {
        LOGGER.warn(
                "event=notifier.feedback.unmatched eventType={}",
                notificationEventType
        );
    }
}
