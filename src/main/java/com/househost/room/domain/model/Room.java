package com.househost.room.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Room {

    private Long id;

    private String roomNumber;

    private RoomType type;

    private Integer capacity;

    private BigDecimal dailyRate;

    private RoomStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Room() {
    }

    public Room(String roomNumber, RoomType type, Integer capacity, BigDecimal dailyRate, RoomStatus status) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.capacity = capacity;
        this.dailyRate = dailyRate;
        this.status = status;
    }

    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateProfile(String roomNumber, RoomType type, Integer capacity, BigDecimal dailyRate, RoomStatus status) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.capacity = capacity;
        this.dailyRate = dailyRate;
        this.status = status;
    }

    public void changeStatus(RoomStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public RoomType getType() {
        return type;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void restorePersistenceState(Long id, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
