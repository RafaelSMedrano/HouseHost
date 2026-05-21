package com.househost.stay.dto;

import com.househost.booking.model.Booking;
import com.househost.stay.model.Stay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class StayResponseDTO {

    private Long id;
    private Long bookingId;
    private Long guestId;
    private String guestName;
    private Long roomId;
    private String roomNumber;
    private LocalDate checkInDate;
    private LocalDate expectedCheckOutDate;
    private LocalDate actualCheckOutDate;
    private String vehiclePlate;
    private String vehicleModel;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StayResponseDTO(Stay stay) {
        Booking booking = stay.getBooking();

        this.id = stay.getId();
        this.bookingId = booking == null ? null : booking.getId();
        this.guestId = stay.getGuest().getId();
        this.guestName = stay.getGuest().getFullName();
        this.roomId = stay.getRoom().getId();
        this.roomNumber = stay.getRoom().getRoomNumber();
        this.checkInDate = stay.getCheckInDate();
        this.expectedCheckOutDate = stay.getExpectedCheckOutDate();
        this.actualCheckOutDate = stay.getActualCheckOutDate();
        this.vehiclePlate = stay.getVehiclePlate();
        this.vehicleModel = stay.getVehicleModel();
        this.status = stay.getStatus().name();
        this.totalAmount = stay.getTotalAmount();
        this.createdAt = stay.getCreatedAt();
        this.updatedAt = stay.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getGuestId() {
        return guestId;
    }

    public String getGuestName() {
        return guestName;
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
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

    public String getStatus() {
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
