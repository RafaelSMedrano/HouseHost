package com.househost.publicapi.adapter.out.integration;

import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.notifier.application.port.in.NotificationRequestUseCase;
import com.househost.notifier.application.records.NotificationRequestRecord;
import com.househost.publicapi.application.records.PublicBookingNotificationRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class NotifierPublicBookingAdapterTest {

    private final NotificationRequestUseCase notificationRequestUseCase =
            mock(NotificationRequestUseCase.class);
    private final PublicBookingNotificationProperties publicBookingNotificationProperties =
            new PublicBookingNotificationProperties();

    @BeforeEach
    void configureProperties() {
        publicBookingNotificationProperties.setEnabled(true);
        publicBookingNotificationProperties.setManagementRecipient(
                "GERENCIA@EXAMPLE.COM"
        );
        publicBookingNotificationProperties.validate();
    }

    @Test
    void createsTwoIndependentMinimizedRequestsForOneBusinessEvent() {
        NotifierPublicBookingAdapter notifierPublicBookingAdapter =
                new NotifierPublicBookingAdapter(
                        notificationRequestUseCase,
                        publicBookingNotificationProperties
                );

        notifierPublicBookingAdapter.requestNotifications(notificationRecord());

        ArgumentCaptor<NotificationRequestRecord> notificationRequestRecordCaptor =
                ArgumentCaptor.forClass(NotificationRequestRecord.class);
        verify(notificationRequestUseCase, org.mockito.Mockito.times(2))
                .requestNotification(notificationRequestRecordCaptor.capture());
        List<NotificationRequestRecord> notificationRequestRecordList =
                notificationRequestRecordCaptor.getAllValues();
        NotificationRequestRecord guestNotificationRequestRecord =
                notificationRequestRecordList.get(0);
        NotificationRequestRecord managementNotificationRequestRecord =
                notificationRequestRecordList.get(1);

        assertEquals(
                guestNotificationRequestRecord.externalEventId(),
                managementNotificationRequestRecord.externalEventId()
        );
        assertNotEquals(
                guestNotificationRequestRecord.idempotencyKey(),
                managementNotificationRequestRecord.idempotencyKey()
        );
        assertEquals("GUEST_REQUEST_RECEIVED", guestNotificationRequestRecord.notificationType());
        assertEquals("MANAGEMENT_NEW_REQUEST", managementNotificationRequestRecord.notificationType());
        assertEquals(
                "maria@example.com",
                guestNotificationRequestRecord.emailMessageRecord().recipient()
        );
        assertEquals(
                "gerencia@example.com",
                managementNotificationRequestRecord.emailMessageRecord().recipient()
        );
        assertTrue(guestNotificationRequestRecord.emailMessageRecord().textBody()
                .contains("ainda nao esta confirmada"));
        assertTrue(guestNotificationRequestRecord.emailMessageRecord().textBody()
                .contains("forma de pagamento"));
        assertTrue(guestNotificationRequestRecord.emailMessageRecord().textBody()
                .contains("PUBLIC_BOOKING_REQUEST:42"));
        assertTrue(managementNotificationRequestRecord.emailMessageRecord().textBody()
                .contains("WhatsApp: +5512999999999"));
        assertFalse(managementNotificationRequestRecord.emailMessageRecord().textBody()
                .contains("observacao confidencial"));
    }

    @Test
    void disabledIntegrationDoesNotRequestDeliveryAndDoesNotFailBusinessFlow() {
        publicBookingNotificationProperties.setEnabled(false);
        publicBookingNotificationProperties.validate();
        NotifierPublicBookingAdapter notifierPublicBookingAdapter =
                new NotifierPublicBookingAdapter(
                        notificationRequestUseCase,
                        publicBookingNotificationProperties
                );

        notifierPublicBookingAdapter.requestNotifications(notificationRecord());

        verify(notificationRequestUseCase, never()).requestNotification(
                org.mockito.ArgumentMatchers.any()
        );
    }

    private PublicBookingNotificationRecord notificationRecord() {
        return new PublicBookingNotificationRecord(
                "PUBLIC_BOOKING_REQUEST:42",
                42L,
                "CL-42",
                LocalDateTime.of(2026, 8, 21, 12, 30),
                "Lavandas & Araucarias",
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 13),
                2,
                1,
                1,
                new BigDecimal("1050.00"),
                "BRL",
                BookingStatus.UNCONFIRMED,
                "Maria",
                "Silva",
                "maria@example.com",
                "+5512999999999"
        );
    }
}
