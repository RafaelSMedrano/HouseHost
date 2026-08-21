package com.househost.publicapi.application.records;

import com.househost.booking.booking.domain.model.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PublicBookingNotificationRecord(
        String externalEventId,
        Long bookingId,
        String bookingCode,
        LocalDateTime requestedAt,
        String roomIdentification,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer adults,
        Integer children,
        Integer pets,
        BigDecimal quotedTotal,
        String currency,
        BookingStatus status,
        String guestFirstName,
        String guestLastName,
        String guestEmail,
        String guestWhatsApp
) {
}
