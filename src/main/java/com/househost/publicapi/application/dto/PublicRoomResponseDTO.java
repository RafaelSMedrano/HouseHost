package com.househost.publicapi.application.dto;

import com.househost.room.domain.model.RoomStatus;

import java.math.BigDecimal;
import java.util.List;

public record PublicRoomResponseDTO(
        Long id,
        String name,
        String roomNumber,
        Integer capacity,
        BigDecimal baseNightlyRate,
        RoomStatus status,
        List<String> amenities
) {
}
