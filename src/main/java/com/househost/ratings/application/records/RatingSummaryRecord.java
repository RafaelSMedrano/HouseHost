package com.househost.ratings.application.records;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RatingSummaryRecord(
        Long bookingId,
        Long guestId,
        String guestName,
        LocalDate bookingCheckInDate,
        LocalDate bookingCheckOutDate,
        LocalDateTime evaluatedAt,
        Integer checkInProcedureScore,
        Integer checkOutProcedureScore,
        Integer accommodationCleanlinessScore,
        Integer teamCommunicationScore,
        Integer locationScore,
        Integer comfortScore,
        String observations
) {
}
