package com.househost.room.application.dto;

import com.househost.room.domain.model.Room;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RoomResponseDTO {

    private Long id;
    private String roomNumber;
    private String type;
    private Integer capacity;
    private BigDecimal dailyRate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RoomResponseDTO(Room room) {
        this.id = room.getId();
        this.roomNumber = room.getRoomNumber();
        this.type = room.getType().name();
        this.capacity = room.getCapacity();
        this.dailyRate = room.getDailyRate();
        this.status = room.getStatus().name();
        this.createdAt = room.getCreatedAt();
        this.updatedAt = room.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getType() {
        return type;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
