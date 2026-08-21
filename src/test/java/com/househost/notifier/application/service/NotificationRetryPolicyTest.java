package com.househost.notifier.application.service;

import com.househost.notifier.application.records.NotificationRetryDecisionRecord;
import com.househost.notifier.domain.exception.NotificationDomainException;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import com.househost.notifier.domain.model.NotificationStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationRetryPolicyTest {

    private static final Instant FAILURE_RECORDED_AT = Instant.parse(
            "2026-08-21T12:00:00Z"
    );

    @Test
    void appliesBoundedExponentialBackoffWithDeterministicJitter() {
        NotificationRetryPolicy minimumJitterNotificationRetryPolicy = policy(
                0.0,
                Duration.ofMinutes(10)
        );
        NotificationRetryPolicy neutralJitterNotificationRetryPolicy = policy(
                0.5,
                Duration.ofMinutes(10)
        );
        NotificationRetryPolicy maximumJitterNotificationRetryPolicy = policy(
                0.999_999,
                Duration.ofSeconds(90)
        );

        NotificationRetryDecisionRecord firstNotificationRetryDecisionRecord =
                minimumJitterNotificationRetryPolicy.decide(
                        1,
                        FAILURE_RECORDED_AT,
                        NotificationFailureCategory.NETWORK
                );
        NotificationRetryDecisionRecord secondNotificationRetryDecisionRecord =
                neutralJitterNotificationRetryPolicy.decide(
                        2,
                        FAILURE_RECORDED_AT,
                        NotificationFailureCategory.NETWORK
                );
        NotificationRetryDecisionRecord cappedNotificationRetryDecisionRecord =
                maximumJitterNotificationRetryPolicy.decide(
                        4,
                        FAILURE_RECORDED_AT,
                        NotificationFailureCategory.NETWORK
                );

        assertEquals(
                FAILURE_RECORDED_AT.plusSeconds(24),
                firstNotificationRetryDecisionRecord.nextAttemptAt()
        );
        assertEquals(
                FAILURE_RECORDED_AT.plusSeconds(60),
                secondNotificationRetryDecisionRecord.nextAttemptAt()
        );
        assertEquals(
                FAILURE_RECORDED_AT.plusSeconds(90),
                cappedNotificationRetryDecisionRecord.nextAttemptAt()
        );
    }

    @Test
    void exhaustsAtConfiguredAttemptLimitWithoutCalculatingJitter() {
        NotificationRetryPolicy notificationRetryPolicy = new NotificationRetryPolicy(
                3,
                Duration.ofSeconds(30),
                Duration.ofMinutes(10),
                0.20,
                () -> 2.0
        );

        NotificationRetryDecisionRecord notificationRetryDecisionRecord =
                notificationRetryPolicy.decide(
                        3,
                        FAILURE_RECORDED_AT,
                        NotificationFailureCategory.PROVIDER_UNAVAILABLE
                );

        assertEquals(
                NotificationStatus.EXHAUSTED,
                notificationRetryDecisionRecord.targetStatus()
        );
        assertNull(notificationRetryDecisionRecord.nextAttemptAt());
    }

    @Test
    void rejectsInvalidConfigurationAndRandomValues() {
        assertThrows(
                NotificationDomainException.class,
                () -> new NotificationRetryPolicy(
                        0,
                        Duration.ofSeconds(30),
                        Duration.ofMinutes(1),
                        0.20,
                        () -> 0.5
                )
        );
        NotificationRetryPolicy notificationRetryPolicy = policy(
                1.0,
                Duration.ofMinutes(10)
        );
        assertThrows(
                NotificationDomainException.class,
                () -> notificationRetryPolicy.decide(
                        1,
                        FAILURE_RECORDED_AT,
                        NotificationFailureCategory.NETWORK
                )
        );
    }

    private NotificationRetryPolicy policy(
            double randomValue,
            Duration maximumDelay
    ) {
        return new NotificationRetryPolicy(
                5,
                Duration.ofSeconds(30),
                maximumDelay,
                0.20,
                () -> randomValue
        );
    }
}
