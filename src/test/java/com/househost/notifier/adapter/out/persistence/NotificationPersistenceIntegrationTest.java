package com.househost.notifier.adapter.out.persistence;

import com.househost.notifier.application.records.NotificationClaimRecord;
import com.househost.notifier.domain.model.NotificationChannel;
import com.househost.notifier.domain.model.NotificationEventType;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import com.househost.notifier.domain.model.NotificationIntent;
import com.househost.notifier.domain.model.NotificationProviderEvent;
import com.househost.notifier.domain.model.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:notifier;MODE=MySQL;DB_CLOSE_DELAY=-1"
})
@Import({
        NotificationIntentPersistenceAdapter.class,
        NotificationProviderEventPersistenceAdapter.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class NotificationPersistenceIntegrationTest {

    private static final Instant CREATED_AT = Instant.parse("2026-08-21T12:00:00Z");

    @Autowired
    private NotificationIntentPersistenceAdapter notificationIntentPersistenceAdapter;

    @Autowired
    private NotificationProviderEventPersistenceAdapter
            notificationProviderEventPersistenceAdapter;

    @Autowired
    private NotificationIntentJpaRepository notificationIntentJpaRepository;

    @Autowired
    private NotificationProviderEventJpaRepository notificationProviderEventJpaRepository;

    @BeforeEach
    void clearPersistence() {
        notificationProviderEventJpaRepository.deleteAll();
        notificationIntentJpaRepository.deleteAll();
    }

    @Test
    void persistsCompleteIntentMappingAndEnforcesRequestIdempotency() {
        NotificationIntent originalNotificationIntent = createIntent(
                UUID.randomUUID(),
                "request-1"
        );
        NotificationIntent duplicateNotificationIntent = createIntent(
                UUID.randomUUID(),
                "request-1"
        );

        NotificationIntent persistedNotificationIntent =
                notificationIntentPersistenceAdapter.createIfAbsent(
                        originalNotificationIntent
                );
        NotificationIntent idempotentNotificationIntent =
                notificationIntentPersistenceAdapter.createIfAbsent(
                        duplicateNotificationIntent
                );

        assertEquals(persistedNotificationIntent.getId(), idempotentNotificationIntent.getId());
        assertEquals(1, notificationIntentJpaRepository.count());
        assertEquals("reservation-42", persistedNotificationIntent.getCorrelationKey());
        assertEquals(NotificationChannel.EMAIL, persistedNotificationIntent.getChannel());
        assertEquals(NotificationStatus.PENDING, persistedNotificationIntent.getStatus());
        assertEquals(0L, persistedNotificationIntent.getVersion());
    }

    @Test
    void createsOneIntentWhenConcurrentRequestsUseTheSameIdempotencyKey() throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            Future<NotificationIntent> firstNotificationIntentFuture = executorService.submit(
                    () -> createAfter(startLatch, UUID.randomUUID(), "request-race")
            );
            Future<NotificationIntent> secondNotificationIntentFuture = executorService.submit(
                    () -> createAfter(startLatch, UUID.randomUUID(), "request-race")
            );
            startLatch.countDown();

            assertEquals(
                    firstNotificationIntentFuture.get().getId(),
                    secondNotificationIntentFuture.get().getId()
            );
            assertEquals(1, notificationIntentJpaRepository.count());
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void claimsOnceAcrossConcurrentWorkersAndRecoversExpiredLease() throws Exception {
        notificationIntentPersistenceAdapter.createIfAbsent(
                createIntent(UUID.randomUUID(), "request-concurrent")
        );
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            Future<List<NotificationClaimRecord>> firstClaimRecordListFuture =
                    executorService.submit(() -> claimAfter(startLatch, CREATED_AT));
            Future<List<NotificationClaimRecord>> secondClaimRecordListFuture =
                    executorService.submit(() -> claimAfter(startLatch, CREATED_AT));
            startLatch.countDown();

            int claimedIntentCount = firstClaimRecordListFuture.get().size()
                    + secondClaimRecordListFuture.get().size();
            assertEquals(1, claimedIntentCount);
        } finally {
            executorService.shutdownNow();
        }

        List<NotificationClaimRecord> recoveredNotificationClaimRecordList =
                notificationIntentPersistenceAdapter
                        .claimEligibleNotificationClaimRecordList(
                                CREATED_AT.plusSeconds(31),
                                CREATED_AT.plusSeconds(61),
                                10
                        );

        assertEquals(1, recoveredNotificationClaimRecordList.size());
        assertEquals(2, recoveredNotificationClaimRecordList.getFirst().attemptCount());
    }

    @Test
    void appendsProviderEventOnceAndStoresOnlyNormalizedFields() {
        NotificationIntent acceptedNotificationIntent = acceptedIntent("request-event");
        NotificationProviderEvent firstNotificationProviderEvent = providerEvent(
                acceptedNotificationIntent.getId(),
                UUID.randomUUID(),
                "sns-message-1"
        );
        NotificationProviderEvent duplicateNotificationProviderEvent = providerEvent(
                acceptedNotificationIntent.getId(),
                UUID.randomUUID(),
                "sns-message-1"
        );

        NotificationProviderEvent persistedNotificationProviderEvent =
                notificationProviderEventPersistenceAdapter.appendIfAbsent(
                        firstNotificationProviderEvent
                );
        NotificationProviderEvent idempotentNotificationProviderEvent =
                notificationProviderEventPersistenceAdapter.appendIfAbsent(
                        duplicateNotificationProviderEvent
                );

        assertEquals(
                persistedNotificationProviderEvent.getId(),
                idempotentNotificationProviderEvent.getId()
        );
        assertEquals(1, notificationProviderEventJpaRepository.count());
        assertEquals("Permanent", persistedNotificationProviderEvent.getBounceType());
        assertEquals("s3://feedback/events/1.json",
                persistedNotificationProviderEvent.getRawEventStorageKey());
    }

    @Test
    void selectsExpiredRetentionAndAnonymizesPersonalContent() {
        NotificationIntent persistedNotificationIntent =
                notificationIntentPersistenceAdapter.createIfAbsent(
                        createIntent(UUID.randomUUID(), "request-retention")
                );

        List<NotificationIntent> expiredNotificationIntentList =
                notificationIntentPersistenceAdapter
                        .findRetentionExpiredNotificationIntentList(
                                CREATED_AT.plus(31, ChronoUnit.DAYS),
                                10
                        );
        int anonymizedIntentCount = notificationIntentPersistenceAdapter
                .anonymizeContentByNotificationIntentIdList(
                        List.of(persistedNotificationIntent.getId()),
                        CREATED_AT.plus(31, ChronoUnit.DAYS)
                );
        NotificationIntent anonymizedNotificationIntent =
                notificationIntentPersistenceAdapter
                        .findBySourceSystemAndIdempotencyKeyOptional(
                                "HOUSEHOST",
                                "request-retention"
                        )
                        .orElseThrow();

        assertEquals(1, expiredNotificationIntentList.size());
        assertEquals(1, anonymizedIntentCount);
        assertTrue(anonymizedNotificationIntent.isContentAnonymized());
        assertNull(anonymizedNotificationIntent.getRecipient());
        assertNull(anonymizedNotificationIntent.getCorrelationKey());
        assertTrue(notificationIntentPersistenceAdapter
                .findRetentionExpiredNotificationIntentList(
                        CREATED_AT.plus(32, ChronoUnit.DAYS),
                        10
                ).isEmpty());
        assertTrue(notificationIntentPersistenceAdapter
                .claimEligibleNotificationClaimRecordList(
                        CREATED_AT.plus(32, ChronoUnit.DAYS),
                        CREATED_AT.plus(32, ChronoUnit.DAYS).plusSeconds(30),
                        10
                ).isEmpty());
    }

    @Test
    void correlatesAcceptedIntentByUniqueProviderMessageId() {
        NotificationIntent acceptedNotificationIntent = acceptedIntent("request-correlation");

        NotificationIntent correlatedNotificationIntent =
                notificationIntentPersistenceAdapter
                        .findByProviderMessageIdOptional("ses-message-request-correlation")
                        .orElseThrow();

        assertEquals(acceptedNotificationIntent.getId(), correlatedNotificationIntent.getId());
        assertFalse(correlatedNotificationIntent.isContentAnonymized());
    }

    private List<NotificationClaimRecord> claimAfter(
            CountDownLatch startLatch,
            Instant claimedAt
    ) throws InterruptedException {
        startLatch.await();
        return notificationIntentPersistenceAdapter.claimEligibleNotificationClaimRecordList(
                claimedAt,
                claimedAt.plusSeconds(30),
                1
        );
    }

    private NotificationIntent createAfter(
            CountDownLatch startLatch,
            UUID notificationIntentId,
            String idempotencyKey
    ) throws InterruptedException {
        startLatch.await();
        return notificationIntentPersistenceAdapter.createIfAbsent(
                createIntent(notificationIntentId, idempotencyKey)
        );
    }

    private NotificationIntent acceptedIntent(String idempotencyKey) {
        NotificationIntent persistedNotificationIntent =
                notificationIntentPersistenceAdapter.createIfAbsent(
                        createIntent(UUID.randomUUID(), idempotencyKey)
                );
        List<NotificationClaimRecord> notificationClaimRecordList =
                notificationIntentPersistenceAdapter
                        .claimEligibleNotificationClaimRecordList(
                                CREATED_AT,
                                CREATED_AT.plusSeconds(30),
                                1
                        );
        NotificationIntent claimedNotificationIntent =
                notificationIntentPersistenceAdapter
                        .findBySourceSystemAndIdempotencyKeyOptional(
                                persistedNotificationIntent.getSourceSystem(),
                                idempotencyKey
                        )
                        .orElseThrow();
        claimedNotificationIntent.markAccepted(
                "ses-message-" + idempotencyKey,
                CREATED_AT.plusSeconds(1)
        );
        assertEquals(1, notificationClaimRecordList.size());
        return notificationIntentPersistenceAdapter.save(claimedNotificationIntent);
    }

    private NotificationIntent createIntent(UUID id, String idempotencyKey) {
        return NotificationIntent.create(
                id,
                "HOUSEHOST",
                "booking-request-42",
                idempotencyKey,
                "reservation-42",
                "PUBLIC_BOOKING_REQUEST_RECEIVED",
                NotificationChannel.EMAIL,
                "HOUSEHOST_TRANSACTIONAL",
                "guest@example.com",
                "Pedido recebido",
                "Recebemos seu pedido.",
                "<p>Recebemos seu pedido.</p>",
                CREATED_AT,
                CREATED_AT.plus(30, ChronoUnit.DAYS)
        );
    }

    private NotificationProviderEvent providerEvent(
            UUID notificationIntentId,
            UUID eventId,
            String transportEventId
    ) {
        return new NotificationProviderEvent(
                eventId,
                notificationIntentId,
                transportEventId,
                "ses-feedback-1",
                "ses-message-request-event",
                NotificationEventType.BOUNCE,
                "Permanent",
                "General",
                "5.1.1",
                NotificationFailureCategory.PERMANENT_BOUNCE,
                CREATED_AT.plusSeconds(2),
                CREATED_AT.plusSeconds(3),
                CREATED_AT.plusSeconds(4),
                "s3://feedback/events/1.json"
        );
    }
}
