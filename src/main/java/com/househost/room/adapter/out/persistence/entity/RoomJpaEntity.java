package com.househost.room.adapter.out.persistence.entity;

import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import com.househost.room.domain.model.RoomType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "Room")
@Table(name = "rooms")
public class RoomJpaEntity extends Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    RoomType type;

    @Column(nullable = false)
    Integer capacity;

    @Column(nullable = false)
    BigDecimal dailyRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    RoomStatus status;

    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;

    protected RoomJpaEntity() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = createdAt == null ? now : createdAt;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
