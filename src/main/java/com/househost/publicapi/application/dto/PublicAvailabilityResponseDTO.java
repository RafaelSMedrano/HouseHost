package com.househost.publicapi.application.dto;

import java.time.LocalDate;
import java.util.List;

public record PublicAvailabilityResponseDTO(
        boolean available,
        Long roomId,
        LocalDate checkIn,
        LocalDate checkOut,
        long nights,
        List<BlockedDateRangeDTO> blockedDates
) {
    public record BlockedDateRangeDTO(LocalDate checkIn, LocalDate checkOut) {
    }
}
