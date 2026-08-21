package com.househost.notifier.application.service;

import com.househost.notifier.application.port.out.NotificationFeedbackTransactionPort;
import com.househost.notifier.application.port.out.NotificationIntentPersistencePort;
import com.househost.notifier.application.port.out.NotificationOperationalEventPort;
import com.househost.notifier.application.port.out.NotificationProviderEventPersistencePort;
import com.househost.notifier.application.records.NotificationFeedbackRecord;
import com.househost.notifier.domain.model.NotificationChannel;
import com.househost.notifier.domain.model.NotificationEventType;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import com.househost.notifier.domain.model.NotificationIntent;
import com.househost.notifier.domain.model.NotificationProviderEvent;
import com.househost.notifier.domain.model.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationFeedbackServiceTest {

    private static final Instant CREATED_AT = Instant.parse("2026-08-21T12:00:00Z");
    private static final Instant RECEIVED_AT = CREATED_AT.plusSeconds(3);

    private NotificationIntentPersistencePort notificationIntentPersistencePort;
    private NotificationProviderEventPersistencePort
            notificationProviderEventPersistencePort;
    private NotificationFeedbackTransactionPort notificationFeedbackTransactionPort;
    private NotificationOperationalEventPort notificationOperationalEventPort;
    private NotificationFeedbackService notificationFeedbackService;

    @BeforeEach
    void setUp() {
        notificationIntentPersistencePort = mock(NotificationIntentPersistencePort.class);
        notificationProviderEventPersistencePort = mock(
                NotificationProviderEventPersistencePort.class
        );
        notificationFeedbackTransactionPort = mock(NotificationFeedbackTransactionPort.class);
        notificationOperationalEventPort = mock(NotificationOperationalEventPort.class);
        doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(notificationFeedbackTransactionPort).execute(any(Runnable.class));
        notificationFeedbackService = new NotificationFeedbackService(
                notificationIntentPersistencePort,
                notificationProviderEventPersistencePort,
                notificationFeedbackTransactionPort,
                notificationOperationalEventPort,
                Clock.fixed(RECEIVED_AT.plusSeconds(1), ZoneOffset.UTC)
        );
    }

    @Test
    void persistsDeliveryAndTransitionsAcceptedIntent() {
        NotificationIntent notificationIntent = acceptedIntent();
        when(notificationIntentPersistencePort.findByProviderMessageIdOptional(
                "ses-message-1"
        )).thenReturn(Optional.of(notificationIntent));
        when(notificationProviderEventPersistencePort.appendIfAbsent(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationFeedbackService.processFeedback(feedbackRecord(
                NotificationEventType.DELIVERY,
                null,
                CREATED_AT.plusSeconds(2)
        ));

        assertEquals(NotificationStatus.DELIVERED, notificationIntent.getStatus());
        verify(notificationIntentPersistencePort).save(notificationIntent);
        verify(notificationOperationalEventPort).recordFeedbackProcessed(
                notificationIntent.getId(),
                NotificationEventType.DELIVERY,
                NotificationStatus.DELIVERED,
                true
        );
    }

    @Test
    void permanentBounceTransitionsAcceptedIntentToBounced() {
        NotificationIntent notificationIntent = acceptedIntent();
        when(notificationIntentPersistencePort.findByProviderMessageIdOptional(any()))
                .thenReturn(Optional.of(notificationIntent));
        when(notificationProviderEventPersistencePort.appendIfAbsent(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationFeedbackService.processFeedback(feedbackRecord(
                NotificationEventType.BOUNCE,
                NotificationFailureCategory.PERMANENT_BOUNCE,
                CREATED_AT.plusSeconds(2)
        ));

        assertEquals(NotificationStatus.BOUNCED, notificationIntent.getStatus());
        assertEquals(
                NotificationFailureCategory.PERMANENT_BOUNCE,
                notificationIntent.getLastErrorCategory()
        );
    }

    @Test
    void complaintOverridesPreviouslyDeliveredState() {
        NotificationIntent notificationIntent = acceptedIntent();
        notificationIntent.markDelivered(CREATED_AT.plusSeconds(2));
        when(notificationIntentPersistencePort.findByProviderMessageIdOptional(any()))
                .thenReturn(Optional.of(notificationIntent));
        when(notificationProviderEventPersistencePort.appendIfAbsent(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationFeedbackService.processFeedback(feedbackRecord(
                NotificationEventType.COMPLAINT,
                NotificationFailureCategory.COMPLAINT,
                CREATED_AT.plusSeconds(3)
        ));

        assertEquals(NotificationStatus.COMPLAINT, notificationIntent.getStatus());
        assertEquals(
                NotificationFailureCategory.COMPLAINT,
                notificationIntent.getLastErrorCategory()
        );
    }

    @Test
    void duplicateTransportEventProducesNoSecondTransition() {
        NotificationIntent notificationIntent = acceptedIntent();
        NotificationProviderEvent existingNotificationProviderEvent = providerEvent(
                UUID.randomUUID(),
                notificationIntent.getId()
        );
        when(notificationIntentPersistencePort.findByProviderMessageIdOptional(any()))
                .thenReturn(Optional.of(notificationIntent));
        when(notificationProviderEventPersistencePort.appendIfAbsent(any()))
                .thenReturn(existingNotificationProviderEvent);

        notificationFeedbackService.processFeedback(feedbackRecord(
                NotificationEventType.DELIVERY,
                null,
                CREATED_AT.plusSeconds(2)
        ));

        assertEquals(NotificationStatus.ACCEPTED, notificationIntent.getStatus());
        verify(notificationIntentPersistencePort, never()).save(any());
        verify(notificationOperationalEventPort, never()).recordFeedbackProcessed(
                any(),
                any(),
                any(),
                anyBoolean()
        );
    }

    @Test
    void recordsOutOfOrderEventWithoutRegressingCurrentState() {
        NotificationIntent notificationIntent = acceptedIntent();
        notificationIntent.markDelivered(CREATED_AT.plusSeconds(4));
        when(notificationIntentPersistencePort.findByProviderMessageIdOptional(any()))
                .thenReturn(Optional.of(notificationIntent));
        when(notificationProviderEventPersistencePort.appendIfAbsent(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationFeedbackService.processFeedback(feedbackRecord(
                NotificationEventType.BOUNCE,
                NotificationFailureCategory.PERMANENT_BOUNCE,
                CREATED_AT.plusSeconds(2)
        ));

        assertEquals(NotificationStatus.DELIVERED, notificationIntent.getStatus());
        verify(notificationIntentPersistencePort, never()).save(any());
        verify(notificationOperationalEventPort).recordFeedbackProcessed(
                notificationIntent.getId(),
                NotificationEventType.BOUNCE,
                NotificationStatus.DELIVERED,
                false
        );
    }

    @Test
    void ignoresUnknownProviderMessageWithoutPersistingConsumerData() {
        when(notificationIntentPersistencePort.findByProviderMessageIdOptional(any()))
                .thenReturn(Optional.empty());

        notificationFeedbackService.processFeedback(feedbackRecord(
                NotificationEventType.COMPLAINT,
                NotificationFailureCategory.COMPLAINT,
                CREATED_AT.plusSeconds(2)
        ));

        verify(notificationProviderEventPersistencePort, never()).appendIfAbsent(any());
        verify(notificationOperationalEventPort).recordFeedbackUnmatched(
                NotificationEventType.COMPLAINT
        );
    }

    @Test
    void persistsOnlyNormalizedFeedbackFields() {
        NotificationIntent notificationIntent = acceptedIntent();
        when(notificationIntentPersistencePort.findByProviderMessageIdOptional(any()))
                .thenReturn(Optional.of(notificationIntent));
        when(notificationProviderEventPersistencePort.appendIfAbsent(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<NotificationProviderEvent> notificationProviderEventCaptor =
                ArgumentCaptor.forClass(NotificationProviderEvent.class);

        notificationFeedbackService.processFeedback(feedbackRecord(
                NotificationEventType.BOUNCE,
                NotificationFailureCategory.PERMANENT_BOUNCE,
                CREATED_AT.plusSeconds(2)
        ));

        verify(notificationProviderEventPersistencePort).appendIfAbsent(
                notificationProviderEventCaptor.capture()
        );
        NotificationProviderEvent notificationProviderEvent =
                notificationProviderEventCaptor.getValue();
        assertEquals("sns-message-1", notificationProviderEvent.getTransportEventId());
        assertEquals("5.1.1", notificationProviderEvent.getProviderStatusCode());
        assertNull(notificationProviderEvent.getRawEventStorageKey());
    }

    private NotificationIntent acceptedIntent() {
        NotificationIntent notificationIntent = NotificationIntent.create(
                UUID.randomUUID(),
                "HOUSEHOST",
                "event-1",
                "event-1:guest",
                "support-reference-1",
                "GUEST_REQUEST_RECEIVED",
                NotificationChannel.EMAIL,
                "HOUSEHOST_TRANSACTIONAL",
                "guest@example.com",
                "Pedido recebido",
                "Recebemos seu pedido.",
                "<p>Recebemos seu pedido.</p>",
                CREATED_AT,
                CREATED_AT.plus(30, ChronoUnit.DAYS)
        );
        notificationIntent.claim(CREATED_AT, CREATED_AT.plusSeconds(30));
        notificationIntent.markAccepted("ses-message-1", CREATED_AT.plusSeconds(1));
        return notificationIntent;
    }

    private NotificationFeedbackRecord feedbackRecord(
            NotificationEventType notificationEventType,
            NotificationFailureCategory notificationFailureCategory,
            Instant occurredAt
    ) {
        return new NotificationFeedbackRecord(
                "sns-message-1",
                null,
                "ses-message-1",
                notificationEventType,
                notificationEventType == NotificationEventType.BOUNCE
                        ? "Permanent"
                        : null,
                notificationEventType == NotificationEventType.BOUNCE
                        ? "General"
                        : null,
                notificationEventType == NotificationEventType.BOUNCE
                        ? "5.1.1"
                        : null,
                notificationFailureCategory,
                occurredAt,
                RECEIVED_AT,
                null
        );
    }

    private NotificationProviderEvent providerEvent(
            UUID notificationProviderEventId,
            UUID notificationIntentId
    ) {
        return new NotificationProviderEvent(
                notificationProviderEventId,
                notificationIntentId,
                "sns-message-1",
                null,
                "ses-message-1",
                NotificationEventType.DELIVERY,
                null,
                null,
                null,
                null,
                CREATED_AT.plusSeconds(2),
                RECEIVED_AT,
                RECEIVED_AT.plusSeconds(1),
                null
        );
    }
}
