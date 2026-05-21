package com.househost.stay.model;

import com.househost.booking.model.Booking;
import com.househost.guest.model.Guest;
import com.househost.room.model.Room;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stays")
public class Stay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "booking_id", unique = true)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate expectedCheckOutDate;

    private LocalDate actualCheckOutDate;

    private String vehiclePlate;

    private String vehicleModel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StayStatus status;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Stay() {
    }

    public Stay(Booking booking, Guest guest, Room room, LocalDate checkInDate, LocalDate expectedCheckOutDate, LocalDate actualCheckOutDate, StayStatus status, BigDecimal totalAmount) {
        this(booking, guest, room, checkInDate, expectedCheckOutDate, actualCheckOutDate, status, totalAmount, null, null);
    }

    public Stay(Booking booking, Guest guest, Room room, LocalDate checkInDate, LocalDate expectedCheckOutDate, LocalDate actualCheckOutDate, StayStatus status, BigDecimal totalAmount, String vehiclePlate, String vehicleModel) {
        this.booking = booking;
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.expectedCheckOutDate = expectedCheckOutDate;
        this.actualCheckOutDate = actualCheckOutDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.vehiclePlate = vehiclePlate;
        this.vehicleModel = vehicleModel;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateStay(Booking booking, Guest guest, Room room, LocalDate checkInDate, LocalDate expectedCheckOutDate, LocalDate actualCheckOutDate, StayStatus status, BigDecimal totalAmount) {
        updateStay(booking, guest, room, checkInDate, expectedCheckOutDate, actualCheckOutDate, status, totalAmount, vehiclePlate, vehicleModel);
    }

    public void updateStay(Booking booking, Guest guest, Room room, LocalDate checkInDate, LocalDate expectedCheckOutDate, LocalDate actualCheckOutDate, StayStatus status, BigDecimal totalAmount, String vehiclePlate, String vehicleModel) {
        this.booking = booking;
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.expectedCheckOutDate = expectedCheckOutDate;
        this.actualCheckOutDate = actualCheckOutDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.vehiclePlate = vehiclePlate;
        this.vehicleModel = vehicleModel;
    }

    public Long getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getExpectedCheckOutDate() {
        return expectedCheckOutDate;
    }

    public LocalDate getActualCheckOutDate() {
        return actualCheckOutDate;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public StayStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
