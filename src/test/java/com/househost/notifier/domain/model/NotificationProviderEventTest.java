package com.househost.notifier.domain.model;

import com.househost.notifier.domain.exception.NotificationDomainException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationProviderEventTest {

    @Test
    void representsMinimizedAppendOnlyProviderOutcome() {
        Instant receivedAt = Instant.parse("2026-08-20T12:01:00Z");
        NotificationProviderEvent notificationProviderEvent = new NotificationProviderEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "sns-message-1",
                "ses-event-1",
                "ses-message-1",
                NotificationEventType.BOUNCE,
                "Permanent",
                "General",
                "5.1.1",
                NotificationFailureCategory.PERMANENT_BOUNCE,
                receivedAt.minusSeconds(2),
                receivedAt,
                receivedAt.plusMillis(20),
                null
        );

        assertEquals(NotificationEventType.BOUNCE, notificationProviderEvent.getEventType());
        assertEquals("Permanent", notificationProviderEvent.getBounceType());
        assertTrue(Arrays.stream(NotificationProviderEvent.class.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .allMatch(field -> Modifier.isFinal(field.getModifiers())));
    }

    @Test
    void rejectsProcessingBeforeReceipt() {
        Instant receivedAt = Instant.parse("2026-08-20T12:01:00Z");

        assertThrows(
                NotificationDomainException.class,
                () -> new NotificationProviderEvent(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "sns-message-1",
                        null,
                        "ses-message-1",
                        NotificationEventType.DELIVERY,
                        null,
                        null,
                        "250",
                        null,
                        receivedAt.minusSeconds(2),
                        receivedAt,
                        receivedAt.minusMillis(1),
                        null
                )
        );
    }
}
