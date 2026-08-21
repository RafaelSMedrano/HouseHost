package com.househost.notifier.adapter.out.persistence;

import com.househost.notifier.application.port.out.EmailDeliveryPort;
import com.househost.notifier.application.port.out.NotificationOperationalEventPort;
import com.househost.notifier.application.records.EmailDeliveryResultRecord;
import com.househost.notifier.application.records.EmailMessageRecord;
import com.househost.notifier.application.service.NotificationDispatchService;
import com.househost.notifier.application.service.NotificationRetryPolicy;
import com.househost.notifier.domain.model.NotificationChannel;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import com.househost.notifier.domain.model.NotificationIntent;
import com.househost.notifier.domain.model.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(NotificationIntentPersistenceAdapter.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class NotificationDispatchPersistenceIntegrationTest {

    private static final Instant CREATED_AT = Instant.parse("2026-08-21T12:00:00Z");

    @Autowired
    private NotificationIntentPersistenceAdapter notificationIntentPersistenceAdapter;

    @Autowired
    private NotificationIntentJpaRepository notificationIntentJpaRepository;

    @BeforeEach
    void clearPersistence() {
        notificationIntentJpaRepository.deleteAll();
    }

    @Test
    void retriesAfterRestartAndCallsProviderOutsideTransaction() {
        NotificationIntent persistedNotificationIntent =
                notificationIntentPersistenceAdapter.createIfAbsent(
                        createIntent("restart-retry")
                );
        RecordingEmailDeliveryPort recordingEmailDeliveryPort =
                new RecordingEmailDeliveryPort();
        recordingEmailDeliveryPort.addResult(
                EmailDeliveryResultRecord.retryableFailure(
                        NotificationFailureCategory.PROVIDER_UNAVAILABLE
                )
        );
        NotificationDispatchService firstNotificationDispatchService = dispatchService(
                recordingEmailDeliveryPort,
                CREATED_AT,
                3
        );

        firstNotificationDispatchService.dispatchDueNotifications();

        NotificationIntent retryableNotificationIntent = findIntent(
                persistedNotificationIntent.getId()
        );
        assertEquals(
                NotificationStatus.RETRYABLE_FAILURE,
                retryableNotificationIntent.getStatus()
        );
        assertEquals(CREATED_AT.plusSeconds(30), retryableNotificationIntent.getNextAttemptAt());

        recordingEmailDeliveryPort.addResult(
                EmailDeliveryResultRecord.accepted("provider-message-restart")
        );
        NotificationDispatchService restartedNotificationDispatchService = dispatchService(
                recordingEmailDeliveryPort,
                CREATED_AT.plusSeconds(30),
                3
        );

        restartedNotificationDispatchService.dispatchDueNotifications();

        NotificationIntent acceptedNotificationIntent = findIntent(
                persistedNotificationIntent.getId()
        );
        assertEquals(NotificationStatus.ACCEPTED, acceptedNotificationIntent.getStatus());
        assertEquals(2, acceptedNotificationIntent.getAttemptCount());
        assertTrue(recordingEmailDeliveryPort.wasAlwaysCalledOutsideTransaction());
    }

    @Test
    void exhaustsRetryLimitAndAllowsControlledReprocessing() {
        NotificationIntent persistedNotificationIntent =
                notificationIntentPersistenceAdapter.createIfAbsent(
                        createIntent("exhausted-reprocess")
                );
        RecordingEmailDeliveryPort recordingEmailDeliveryPort =
                new RecordingEmailDeliveryPort();
        recordingEmailDeliveryPort.addResult(
                EmailDeliveryResultRecord.retryableFailure(
                        NotificationFailureCategory.THROTTLED
                )
        );
        NotificationDispatchService notificationDispatchService = dispatchService(
                recordingEmailDeliveryPort,
                CREATED_AT,
                1
        );

        notificationDispatchService.dispatchDueNotifications();

        NotificationIntent exhaustedNotificationIntent = findIntent(
                persistedNotificationIntent.getId()
        );
        assertEquals(NotificationStatus.EXHAUSTED, exhaustedNotificationIntent.getStatus());
        assertNull(exhaustedNotificationIntent.getNextAttemptAt());

        notificationDispatchService.reprocessExhaustedNotification(
                persistedNotificationIntent.getId()
        );

        NotificationIntent requeuedNotificationIntent = findIntent(
                persistedNotificationIntent.getId()
        );
        assertEquals(NotificationStatus.PENDING, requeuedNotificationIntent.getStatus());
        assertEquals(0, requeuedNotificationIntent.getAttemptCount());
        assertEquals(CREATED_AT, requeuedNotificationIntent.getNextAttemptAt());
        assertFalse(requeuedNotificationIntent.getStatus().isTerminal());
    }

    @Test
    void mapsUnexpectedProviderExceptionToDurableUnknownRetry() {
        NotificationIntent persistedNotificationIntent =
                notificationIntentPersistenceAdapter.createIfAbsent(
                        createIntent("unexpected-provider-error")
                );
        RecordingEmailDeliveryPort recordingEmailDeliveryPort =
                new RecordingEmailDeliveryPort();
        recordingEmailDeliveryPort.failNextCall();
        NotificationDispatchService notificationDispatchService = dispatchService(
                recordingEmailDeliveryPort,
                CREATED_AT,
                3
        );

        notificationDispatchService.dispatchDueNotifications();

        NotificationIntent retryableNotificationIntent = findIntent(
                persistedNotificationIntent.getId()
        );
        assertEquals(
                NotificationStatus.RETRYABLE_FAILURE,
                retryableNotificationIntent.getStatus()
        );
        assertEquals(
                NotificationFailureCategory.UNKNOWN,
                retryableNotificationIntent.getLastErrorCategory()
        );
    }

    private NotificationDispatchService dispatchService(
            EmailDeliveryPort emailDeliveryPort,
            Instant currentInstant,
            int maximumAttempts
    ) {
        NotificationRetryPolicy notificationRetryPolicy = new NotificationRetryPolicy(
                maximumAttempts,
                Duration.ofSeconds(30),
                Duration.ofMinutes(10),
                0.20,
                () -> 0.5
        );
        return new NotificationDispatchService(
                notificationIntentPersistenceAdapter,
                emailDeliveryPort,
                new NoOpNotificationOperationalEventPort(),
                notificationRetryPolicy,
                Clock.fixed(currentInstant, ZoneOffset.UTC),
                Duration.ofMinutes(2),
                10
        );
    }

    private NotificationIntent findIntent(UUID notificationIntentId) {
        return notificationIntentPersistenceAdapter.findByIdOptional(notificationIntentId)
                .orElseThrow();
    }

    private NotificationIntent createIntent(String idempotencyKey) {
        return NotificationIntent.create(
                UUID.randomUUID(),
                "HOUSEHOST",
                "event-039",
                idempotencyKey,
                "support-reference",
                "GUEST_REQUEST_RECEIVED",
                NotificationChannel.EMAIL,
                "HOUSEHOST_TRANSACTIONAL",
                "guest@example.com",
                "Request received",
                "We received your request.",
                "<p>We received your request.</p>",
                CREATED_AT,
                CREATED_AT.plus(30, ChronoUnit.DAYS)
        );
    }

    private static final class RecordingEmailDeliveryPort implements EmailDeliveryPort {

        private final Queue<EmailDeliveryResultRecord> emailDeliveryResultRecordQueue =
                new ArrayDeque<>();
        private boolean alwaysCalledOutsideTransaction = true;
        private boolean failNextCall;

        @Override
        public EmailDeliveryResultRecord deliver(
                String sourceSystem,
                String deliveryProfileKey,
                EmailMessageRecord emailMessageRecord
        ) {
            alwaysCalledOutsideTransaction = alwaysCalledOutsideTransaction
                    && !TransactionSynchronizationManager.isActualTransactionActive();
            if (failNextCall) {
                failNextCall = false;
                throw new IllegalStateException("simulated provider failure");
            }
            return emailDeliveryResultRecordQueue.remove();
        }

        void addResult(EmailDeliveryResultRecord emailDeliveryResultRecord) {
            emailDeliveryResultRecordQueue.add(emailDeliveryResultRecord);
        }

        void failNextCall() {
            failNextCall = true;
        }

        boolean wasAlwaysCalledOutsideTransaction() {
            return alwaysCalledOutsideTransaction;
        }
    }

    private static final class NoOpNotificationOperationalEventPort
            implements NotificationOperationalEventPort {

        @Override
        public void recordBatchClaimed(int claimedCount, Instant claimedAt) {
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
        }

        @Override
        public void recordClaimResultIgnored(UUID notificationIntentId, int attemptCount) {
        }

        @Override
        public void recordDispatchInterrupted(UUID notificationIntentId, int attemptCount) {
        }

        @Override
        public void recordRequeued(
                UUID notificationIntentId,
                String sourceSystem,
                String notificationType
        ) {
        }
    }
}
