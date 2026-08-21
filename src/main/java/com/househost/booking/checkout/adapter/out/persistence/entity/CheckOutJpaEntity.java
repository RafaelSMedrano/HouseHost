package com.househost.booking.checkout.adapter.out.persistence.entity;

import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.booking.checkout.domain.model.CheckOutStatus;
import com.househost.booking.booking.adapter.out.persistence.entity.BookingJpaEntity;
import com.househost.guest.adapter.out.persistence.entity.GuestJpaEntity;
import com.househost.room.adapter.out.persistence.entity.RoomJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "CheckOut")
@Table(name = "check_outs")
public class CheckOutJpaEntity extends CheckOut {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(optional = true)
    @JoinColumn(name = "booking_id", unique = true, nullable = true)
    BookingJpaEntity booking;

    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    GuestJpaEntity guest;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    RoomJpaEntity room;

    @Column(nullable = false)
    LocalDateTime actualCheckOutAt;

    boolean roomInspected;
    boolean keysReturned;
    boolean consumablesChecked;
    boolean pendingAmountPaid;
    BigDecimal extraCharges;
    BigDecimal pendingAmount;
    String performedBy;
    @Column(length = 1000)
    String notes;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    CheckOutStatus status;

    @Column(nullable = false)
    boolean guestHistoryApplied;

    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;

    protected CheckOutJpaEntity() {
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        actualCheckOutAt = actualCheckOutAt == null ? now : actualCheckOutAt;
        createdAt = createdAt == null ? now : createdAt;
        updatedAt = now;
        status = status == null ? CheckOutStatus.COMPLETED : status;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
