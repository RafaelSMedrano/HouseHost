package com.househost.booking.booking.adapter.out.persistence.entity;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingOrigin;
import com.househost.booking.booking.domain.model.BookingPaymentStatus;
import com.househost.booking.booking.domain.model.BookingStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "Booking")
@Table(name = "bookings")
public class BookingJpaEntity extends Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    GuestJpaEntity guest;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    RoomJpaEntity room;

    @Column(nullable = false)
    LocalDate checkInDate;

    @Column(nullable = false)
    LocalDate checkOutDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    BookingStatus status;

    @Column(nullable = false)
    BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    BookingOrigin origin = BookingOrigin.DIRETO_TELEFONE;

    Integer adults;
    Integer children;
    Integer pets;
    String paymentMethod;
    String installments;
    BigDecimal dailyRate;
    BigDecimal discount;
    BigDecimal paidAmount;
    LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    BookingPaymentStatus paymentStatus = BookingPaymentStatus.WAITING;

    @Column(length = 1000)
    String specialRequests;

    @Column(length = 1000)
    String internalNotes;

    @Column(length = 120)
    String privacyPolicyVersion;

    @Column(length = 71)
    String privacyPolicyContentHash;

    @Column(length = 120)
    String termsVersion;

    LocalDateTime privacyAcceptedAt;

    @Column(nullable = false)
    Boolean marketingOptIn = false;

    LocalDateTime marketingOptInAt;

    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;

    protected BookingJpaEntity() {
    }

    public static BookingJpaEntity reference(Long id) {
        BookingJpaEntity entity = new BookingJpaEntity();
        entity.id = id;
        return entity;
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
