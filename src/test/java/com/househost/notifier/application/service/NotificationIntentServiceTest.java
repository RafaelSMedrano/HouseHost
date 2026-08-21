package com.househost.notifier.application.service;

import com.househost.notifier.application.port.out.NotificationIntentPersistencePort;
import com.househost.notifier.application.records.EmailMessageRecord;
import com.househost.notifier.application.records.NotificationClaimRecord;
import com.househost.notifier.application.records.NotificationRequestRecord;
import com.househost.notifier.domain.model.NotificationChannel;
import com.househost.notifier.domain.model.NotificationIntent;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class NotificationIntentServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-20T12:00:00Z");
    private static final UUID GENERATED_ID = UUID.fromString(
            "8b826fb3-fe90-49f6-8c83-ebc12e99b92f"
    );

    @Test
    void createsSelfContainedIntentWithServerControlledRetention() {
        InMemoryNotificationIntentPersistencePort persistencePort =
                new InMemoryNotificationIntentPersistencePort();
        NotificationIntentService notificationIntentService = new NotificationIntentService(
                persistencePort,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofDays(30),
                () -> GENERATED_ID
        );

        UUID notificationIntentId = notificationIntentService.requestNotification(
                requestRecord()
        );

        NotificationIntent notificationIntent = persistencePort.notificationIntentList.getFirst();
        assertEquals(GENERATED_ID, notificationIntentId);
        assertEquals(NOW.plus(Duration.ofDays(30)), notificationIntent.getRetentionUntil());
        assertEquals("event-1", notificationIntent.getExternalEventId());
        assertEquals("guest@example.com", notificationIntent.getRecipient());
    }

    @Test
    void returnsExistingIntentForSourceScopedIdempotencyKey() {
        InMemoryNotificationIntentPersistencePort persistencePort =
                new InMemoryNotificationIntentPersistencePort();
        NotificationIntentService notificationIntentService = new NotificationIntentService(
                persistencePort,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofDays(30),
                () -> GENERATED_ID
        );

        UUID firstNotificationIntentId = notificationIntentService.requestNotification(
                requestRecord()
        );
        NotificationIntent firstNotificationIntent =
                persistencePort.notificationIntentList.getFirst();
        UUID repeatedNotificationIntentId = notificationIntentService.requestNotification(
                requestRecord()
        );

        assertEquals(firstNotificationIntentId, repeatedNotificationIntentId);
        assertEquals(1, persistencePort.notificationIntentList.size());
        assertSame(firstNotificationIntent, persistencePort.notificationIntentList.getFirst());
    }

    private NotificationRequestRecord requestRecord() {
        return new NotificationRequestRecord(
                "HOUSEHOST",
                "event-1",
                "event-1:guest",
                "support-reference-1",
                "GUEST_REQUEST_RECEIVED",
                NotificationChannel.EMAIL,
                "HOUSEHOST_TRANSACTIONAL",
                new EmailMessageRecord(
                        "guest@example.com",
                        "Reservation request received",
                        "We received your request.",
                        "<p>We received your request.</p>"
                )
        );
    }

    private static final class InMemoryNotificationIntentPersistencePort
            implements NotificationIntentPersistencePort {

        private final List<NotificationIntent> notificationIntentList = new ArrayList<>();

        @Override
        public NotificationIntent createIfAbsent(NotificationIntent notificationIntent) {
            Optional<NotificationIntent> existingNotificationIntentOptional =
                    findBySourceSystemAndIdempotencyKeyOptional(
                            notificationIntent.getSourceSystem(),
                            notificationIntent.getIdempotencyKey()
                    );
            if (existingNotificationIntentOptional.isPresent()) {
                return existingNotificationIntentOptional.orElseThrow();
            }
            notificationIntentList.add(notificationIntent);
            return notificationIntent;
        }

        @Override
        public NotificationIntent save(NotificationIntent notificationIntent) {
            return notificationIntent;
        }

        @Override
        public Optional<NotificationIntent> findByIdOptional(UUID notificationIntentId) {
            return notificationIntentList.stream()
                    .filter(notificationIntent -> notificationIntentId.equals(
                            notificationIntent.getId()
                    ))
                    .findFirst();
        }

        @Override
        public Optional<NotificationIntent> findBySourceSystemAndIdempotencyKeyOptional(
                String sourceSystem,
                String idempotencyKey
        ) {
            return notificationIntentList.stream()
                    .filter(notificationIntent -> sourceSystem.equals(
                            notificationIntent.getSourceSystem()
                    ))
                    .filter(notificationIntent -> idempotencyKey.equals(
                            notificationIntent.getIdempotencyKey()
                    ))
                    .findFirst();
        }

        @Override
        public Optional<NotificationIntent> findByProviderMessageIdOptional(
                String providerMessageId
        ) {
            return Optional.empty();
        }

        @Override
        public List<NotificationClaimRecord> claimEligibleNotificationClaimRecordList(
                Instant claimedAt,
                Instant leaseUntil,
                int batchSize
        ) {
            return List.of();
        }

        @Override
        public List<NotificationIntent> findRetentionExpiredNotificationIntentList(
                Instant referenceAt,
                int batchSize
        ) {
            return List.of();
        }

        @Override
        public int anonymizeContentByNotificationIntentIdList(
                List<UUID> notificationIntentIdList,
                Instant anonymizedAt
        ) {
            return 0;
        }
    }
}
