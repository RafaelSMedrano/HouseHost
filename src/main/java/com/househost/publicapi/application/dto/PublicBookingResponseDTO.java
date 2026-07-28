package com.househost.publicapi.application.dto;

import com.househost.booking.booking.domain.model.BookingStatus;

import java.math.BigDecimal;

public record PublicBookingResponseDTO(
        String bookingCode,
        Long bookingId,
        BookingStatus status,
        BigDecimal total,
        String message
) {
}
