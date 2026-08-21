package com.househost.notifier.domain.model;

import com.househost.notifier.domain.exception.NotificationDomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationIntentTest {

    private static final Instant CREATED_AT = Instant.parse("2026-08-20T12:00:00Z");

    @Test
    void createsSelfContainedPendingIntentWithImmutableMessageSnapshot() {
        NotificationIntent notificationIntent = createIntent();

        assertEquals(NotificationStatus.PENDING, notificationIntent.getStatus());
        assertEquals(0, notificationIntent.getAttemptCount());
        assertEquals(CREATED_AT, notificationIntent.getNextAttemptAt());
        assertEquals("guest@example.com", notificationIntent.getRecipient());
        assertEquals("Reservation request received", notificationIntent.getSubject());
        assertTrue(notificationIntent.isEligibleForClaimAt(CREATED_AT));
        assertFalse(notificationIntent.getStatus().isTerminal());
    }

    @Test
    void transitionsFromClaimThroughProviderAcceptanceAndDelivery() {
        NotificationIntent notificationIntent = createIntent();
        Instant claimedAt = CREATED_AT.plusSeconds(1);
        Instant acceptedAt = claimedAt.plusSeconds(2);
        Instant deliveredAt = acceptedAt.plusSeconds(5);

        notificationIntent.claim(claimedAt, claimedAt.plusSeconds(30));
        notificationIntent.markAccepted("provider-message-1", acceptedAt);
        notificationIntent.markDelivered(deliveredAt);

        assertEquals(NotificationStatus.DELIVERED, notificationIntent.getStatus());
        assertEquals(1, notificationIntent.getAttemptCount());
        assertEquals("provider-message-1", notificationIntent.getProviderMessageId());
        assertEquals(acceptedAt, notificationIntent.getAcceptedAt());
        assertEquals(deliveredAt, notificationIntent.getDeliveredAt());
        assertNull(notificationIntent.getLeaseUntil());
        assertFalse(notificationIntent.isEligibleForClaimAt(deliveredAt.plusSeconds(30)));
    }

    @Test
    void retriesOnlyWhenPersistedRetryTimeBecomesDue() {
        NotificationIntent notificationIntent = createIntent();
        Instant firstClaimAt = CREATED_AT.plusSeconds(1);
        Instant failureAt = firstClaimAt.plusSeconds(1);
        Instant retryAt = failureAt.plusSeconds(30);

        notificationIntent.claim(firstClaimAt, firstClaimAt.plusSeconds(10));
        notificationIntent.markRetryableFailure(
                NotificationFailureCategory.NETWORK,
                retryAt,
                failureAt
        );

        assertFalse(notificationIntent.isEligibleForClaimAt(retryAt.minusMillis(1)));
        assertThrows(
                NotificationDomainException.class,
                () -> notificationIntent.claim(
                        retryAt.minusMillis(1),
                        retryAt.plusSeconds(10)
                )
        );

        notificationIntent.claim(retryAt, retryAt.plusSeconds(10));

        assertEquals(NotificationStatus.PROCESSING, notificationIntent.getStatus());
        assertEquals(2, notificationIntent.getAttemptCount());
    }

    @Test
    void recoversOnlyAnExpiredProcessingLease() {
        NotificationIntent notificationIntent = createIntent();
        Instant claimedAt = CREATED_AT.plusSeconds(1);
        Instant leaseUntil = claimedAt.plusSeconds(30);
        notificationIntent.claim(claimedAt, leaseUntil);

        assertFalse(notificationIntent.isEligibleForClaimAt(leaseUntil.minusMillis(1)));
        assertTrue(notificationIntent.isEligibleForClaimAt(leaseUntil));

        notificationIntent.claim(leaseUntil, leaseUntil.plusSeconds(30));

        assertEquals(2, notificationIntent.getAttemptCount());
    }

    @Test
    void preservesBounceAsTerminalState() {
        NotificationIntent notificationIntent = acceptedIntent();
        Instant bouncedAt = CREATED_AT.plusSeconds(10);

        notificationIntent.markBounced(
                NotificationFailureCategory.PERMANENT_BOUNCE,
                bouncedAt
        );

        assertEquals(NotificationStatus.BOUNCED, notificationIntent.getStatus());
        assertTrue(notificationIntent.getStatus().isTerminal());
        assertEquals(bouncedAt, notificationIntent.getFailedAt());
        assertThrows(
                NotificationDomainException.class,
                () -> notificationIntent.markDelivered(bouncedAt.plusSeconds(1))
        );
        assertThrows(
                NotificationDomainException.class,
                () -> notificationIntent.claim(
                        bouncedAt.plusSeconds(1),
                        bouncedAt.plusSeconds(30)
                )
        );
    }

    @Test
    void preservesExhaustedHandoffAsTerminalState() {
        NotificationIntent notificationIntent = createIntent();
        Instant claimedAt = CREATED_AT.plusSeconds(1);
        Instant exhaustedAt = claimedAt.plusSeconds(2);
        notificationIntent.claim(claimedAt, claimedAt.plusSeconds(30));

        notificationIntent.markExhausted(
                NotificationFailureCategory.INVALID_REQUEST,
                exhaustedAt
        );

        assertEquals(NotificationStatus.EXHAUSTED, notificationIntent.getStatus());
        assertTrue(notificationIntent.getStatus().isTerminal());
        assertEquals(exhaustedAt, notificationIntent.getFailedAt());
        assertThrows(
                NotificationDomainException.class,
                () -> notificationIntent.claim(
                        exhaustedAt.plusSeconds(1),
                        exhaustedAt.plusSeconds(30)
                )
        );
    }

    @Test
    void requeuesExhaustedIntentForControlledProcessing() {
        NotificationIntent notificationIntent = createIntent();
        Instant claimedAt = CREATED_AT.plusSeconds(1);
        Instant exhaustedAt = claimedAt.plusSeconds(2);
        Instant requeuedAt = exhaustedAt.plusSeconds(3);
        notificationIntent.claim(claimedAt, claimedAt.plusSeconds(30));
        notificationIntent.markExhausted(
                NotificationFailureCategory.INVALID_REQUEST,
                exhaustedAt
        );

        notificationIntent.requeueExhausted(requeuedAt);

        assertEquals(NotificationStatus.PENDING, notificationIntent.getStatus());
        assertEquals(0, notificationIntent.getAttemptCount());
        assertEquals(requeuedAt, notificationIntent.getNextAttemptAt());
        assertNull(notificationIntent.getFailedAt());
        assertNull(notificationIntent.getLastErrorCategory());
        assertTrue(notificationIntent.isEligibleForClaimAt(requeuedAt));
    }

    @Test
    void acceptsComplaintAfterDeliveryAndThenPreservesTerminalState() {
        NotificationIntent notificationIntent = acceptedIntent();
        Instant deliveredAt = CREATED_AT.plusSeconds(10);
        Instant complaintAt = deliveredAt.plusSeconds(5);
        notificationIntent.markDelivered(deliveredAt);

        notificationIntent.markComplaint(
                NotificationFailureCategory.COMPLAINT,
                complaintAt
        );

        assertEquals(NotificationStatus.COMPLAINT, notificationIntent.getStatus());
        assertEquals(complaintAt, notificationIntent.getFailedAt());
        assertThrows(
                NotificationDomainException.class,
                () -> notificationIntent.markBounced(
                        NotificationFailureCategory.PERMANENT_BOUNCE,
                        complaintAt.plusSeconds(1)
                )
        );
    }

    @Test
    void rejectsHeaderInjectionAndInvalidRestoredState() {
        assertThrows(
                NotificationDomainException.class,
                () -> NotificationIntent.create(
                        UUID.randomUUID(),
                        "HOUSEHOST",
                        "event-1",
                        "event-1:guest",
                        null,
                        "GUEST_REQUEST_RECEIVED",
                        NotificationChannel.EMAIL,
                        "HOUSEHOST_TRANSACTIONAL",
                        "guest@example.com",
                        "Unsafe\r\nBcc: attacker@example.com",
                        "Text",
                        "<p>Text</p>",
                        CREATED_AT,
                        CREATED_AT.plus(30, ChronoUnit.DAYS)
                )
        );
        assertThrows(
                NotificationDomainException.class,
                () -> NotificationIntent.restore(
                        UUID.randomUUID(),
                        "HOUSEHOST",
                        "event-1",
                        "event-1:guest",
                        null,
                        "GUEST_REQUEST_RECEIVED",
                        NotificationChannel.EMAIL,
                        "HOUSEHOST_TRANSACTIONAL",
                        "guest@example.com",
                        "Subject",
                        "Text",
                        "<p>Text</p>",
                        NotificationStatus.ACCEPTED,
                        1,
                        null,
                        null,
                        null,
                        null,
                        CREATED_AT,
                        CREATED_AT,
                        null,
                        null,
                        null,
                        CREATED_AT.plus(30, ChronoUnit.DAYS),
                        0L
                )
        );
    }

    private NotificationIntent acceptedIntent() {
        NotificationIntent notificationIntent = createIntent();
        notificationIntent.claim(
                CREATED_AT.plusSeconds(1),
                CREATED_AT.plusSeconds(30)
        );
        notificationIntent.markAccepted(
                "provider-message-1",
                CREATED_AT.plusSeconds(2)
        );
        return notificationIntent;
    }

    private NotificationIntent createIntent() {
        return NotificationIntent.create(
                UUID.fromString("8b826fb3-fe90-49f6-8c83-ebc12e99b92f"),
                "HOUSEHOST",
                "event-1",
                "event-1:guest",
                "support-reference-1",
                "GUEST_REQUEST_RECEIVED",
                NotificationChannel.EMAIL,
                "HOUSEHOST_TRANSACTIONAL",
                "guest@example.com",
                "Reservation request received",
                "We received your request.",
                "<p>We received your request.</p>",
                CREATED_AT,
                CREATED_AT.plus(30, ChronoUnit.DAYS)
        );
    }
}
