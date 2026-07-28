package com.househost.booking.checking.adapter.out.persistence.entity;

import com.househost.booking.booking.adapter.out.persistence.entity.BookingJpaEntity;
import com.househost.booking.checking.domain.model.CheckIn;
import com.househost.booking.checking.domain.model.CheckInStatus;
import com.househost.guest.adapter.out.persistence.entity.GuestJpaEntity;
import com.househost.room.adapter.out.persistence.entity.RoomJpaEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "CheckIn")
@Table(name = "check_ins")
public class CheckInJpaEntity extends CheckIn {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @OneToOne @JoinColumn(name = "booking_id", unique = true) BookingJpaEntity booking;
    @ManyToOne @JoinColumn(name = "guest_id", nullable = false) GuestJpaEntity guest;
    @ManyToOne @JoinColumn(name = "room_id", nullable = false) RoomJpaEntity room;
    Integer adults;
    Integer children;
    Integer pets;
    boolean documentVerified;
    boolean paymentVerified;
    boolean registrationFormSigned;
    boolean rulesAccepted;
    boolean keysDelivered;
    String vehiclePlate;
    String vehicleModel;
    String performedBy;
    @Column(length = 1000) String notes;
    @Enumerated(EnumType.STRING) @Column(nullable = false) CheckInStatus status;
    @Column(nullable = false, updatable = false) LocalDateTime createdAt;

    protected CheckInJpaEntity() {}

    @PrePersist void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = createdAt == null ? now : createdAt;
        status = status == null ? CheckInStatus.COMPLETED : status;
    }
    @Override public Long getId() { return id; }
}
