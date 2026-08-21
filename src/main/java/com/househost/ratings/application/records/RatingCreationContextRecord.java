package com.househost.ratings.application.records;

import com.househost.booking.booking.domain.model.Booking;

import java.time.LocalDateTime;

public record RatingCreationContextRecord(
        Booking booking,
        LocalDateTime evaluatedAt,
        String normalizedObservations
) {
}
