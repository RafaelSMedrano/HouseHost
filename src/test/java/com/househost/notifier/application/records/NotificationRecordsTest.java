package com.househost.notifier.application.records;

import com.househost.notifier.domain.exception.NotificationDomainException;
import com.househost.notifier.domain.model.EmailDeliveryOutcome;
import com.househost.notifier.domain.model.NotificationChannel;
import com.househost.notifier.domain.model.NotificationEventType;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import com.househost.notifier.domain.model.NotificationStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationRecordsTest {

    @Test
    void normalizesBoundedNeutralRequestWithoutConsumerIdentifiers() {
        EmailMessageRecord emailMessageRecord = new EmailMessageRecord(
                " guest@example.com ",
                " Request received ",
                " Text body ",
                " <p>HTML body</p> "
        );

        NotificationRequestRecord notificationRequestRecord = new NotificationRequestRecord(
                "HOUSEHOST",
                " request-event-1 ",
                " request-event-1:guest ",
                " ",
                "GUEST_REQUEST_RECEIVED",
                NotificationChannel.EMAIL,
                "HOUSEHOST_TRANSACTIONAL",
                emailMessageRecord
        );

        assertEquals("guest@example.com", emailMessageRecord.recipient());
        assertEquals("Request received", emailMessageRecord.subject());
        assertEquals(" Text body ", emailMessageRecord.textBody());
        assertEquals(" <p>HTML body</p> ", emailMessageRecord.htmlBody());
        assertEquals("request-event-1", notificationRequestRecord.externalEventId());
        assertNull(notificationRequestRecord.correlationKey());
    }

    @Test
    void rejectsInvalidEmailAndHeaderInjection() {
        assertThrows(
                NotificationDomainException.class,
                () -> new EmailMessageRecord(
                        "not-an-email",
                        "Subject",
                        "Text",
                        "<p>Text</p>"
                )
        );
        assertThrows(
                NotificationDomainException.class,
                () -> new EmailMessageRecord(
                        "guest@example.com",
                        "Subject\nBcc: attacker@example.com",
                        "Text",
                        "<p>Text</p>"
                )
        );
    }

    @Test
    void deliveryResultMakesAcceptanceAndFailureMutuallyExclusive() {
        EmailDeliveryResultRecord acceptedEmailDeliveryResultRecord =
                EmailDeliveryResultRecord.accepted("provider-message-1");
        EmailDeliveryResultRecord failedEmailDeliveryResultRecord =
                EmailDeliveryResultRecord.retryableFailure(
                        NotificationFailureCategory.PROVIDER_UNAVAILABLE
                );

        assertEquals(
                EmailDeliveryOutcome.ACCEPTED,
                acceptedEmailDeliveryResultRecord.outcome()
        );
        assertEquals(
                EmailDeliveryOutcome.RETRYABLE_FAILURE,
                failedEmailDeliveryResultRecord.outcome()
        );
        assertThrows(
                NotificationDomainException.class,
                () -> new EmailDeliveryResultRecord(
                        EmailDeliveryOutcome.ACCEPTED,
                        null,
                        NotificationFailureCategory.UNKNOWN
                )
        );
    }

    @Test
    void feedbackCarriesOnlyNormalizedProviderEvidence() {
        Instant receivedAt = Instant.parse("2026-08-20T12:00:00Z");
        NotificationFeedbackRecord notificationFeedbackRecord =
                new NotificationFeedbackRecord(
                        "sns-message-1",
                        "ses-event-1",
                        "ses-message-1",
                        NotificationEventType.BOUNCE,
                        "Permanent",
                        "General",
                        "5.1.1",
                        NotificationFailureCategory.PERMANENT_BOUNCE,
                        receivedAt.minusSeconds(1),
                        receivedAt,
                        null
                );

        assertEquals("sns-message-1", notificationFeedbackRecord.transportEventId());
        assertEquals(NotificationEventType.BOUNCE, notificationFeedbackRecord.eventType());
    }

    @Test
    void retryDecisionAllowsOnlyRetryOrExhaustion() {
        Instant retryAt = Instant.parse("2026-08-20T12:01:00Z");
        NotificationRetryDecisionRecord retryNotificationRetryDecisionRecord =
                NotificationRetryDecisionRecord.retryAt(
                        retryAt,
                        NotificationFailureCategory.NETWORK
                );
        NotificationRetryDecisionRecord exhaustedNotificationRetryDecisionRecord =
                NotificationRetryDecisionRecord.exhausted(
                        NotificationFailureCategory.INVALID_REQUEST
                );

        assertEquals(
                NotificationStatus.RETRYABLE_FAILURE,
                retryNotificationRetryDecisionRecord.targetStatus()
        );
        assertEquals(
                NotificationStatus.EXHAUSTED,
                exhaustedNotificationRetryDecisionRecord.targetStatus()
        );
        assertThrows(
                NotificationDomainException.class,
                () -> new NotificationRetryDecisionRecord(
                        NotificationStatus.ACCEPTED,
                        null,
                        NotificationFailureCategory.UNKNOWN
                )
        );
    }
}
