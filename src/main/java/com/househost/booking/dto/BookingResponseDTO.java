package com.househost.booking.dto;

import com.househost.booking.model.Booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingResponseDTO {

    private Long id;
    private Long guestId;
    private String guestName;
    private Long roomId;
    private String roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String status;
    private BigDecimal totalAmount;
    private String origin;
    private Integer adults;
    private Integer children;
    private Integer pets;
    private String paymentMethod;
    private String installments;
    private BigDecimal dailyRate;
    private BigDecimal discount;
    private BigDecimal paidAmount;
    private LocalDate paymentDate;
    private String paymentStatus;
    private String paymentStatusLabel;
    private String specialRequests;
    private String internalNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BookingResponseDTO(Booking booking) {
        this.id = booking.getId();
        this.guestId = booking.getGuest().getId();
        this.guestName = booking.getGuest().getFullName();
        this.roomId = booking.getRoom().getId();
        this.roomNumber = booking.getRoom().getRoomNumber();
        this.checkInDate = booking.getCheckInDate();
        this.checkOutDate = booking.getCheckOutDate();
        this.status = booking.getStatus().name();
        this.totalAmount = booking.getTotalAmount();
        this.origin = booking.getOrigin().getLabel();
        this.adults = booking.getAdults();
        this.children = booking.getChildren();
        this.pets = booking.getPets();
        this.paymentMethod = booking.getPaymentMethod();
        this.installments = booking.getInstallments();
        this.dailyRate = booking.getDailyRate();
        this.discount = booking.getDiscount();
        this.paidAmount = booking.getPaidAmount();
        this.paymentDate = booking.getPaymentDate();
        this.paymentStatus = booking.getPaymentStatus().name();
        this.paymentStatusLabel = paymentStatusLabel(this.paymentStatus);
        this.specialRequests = booking.getSpecialRequests();
        this.internalNotes = booking.getInternalNotes();
        this.createdAt = booking.getCreatedAt();
        this.updatedAt = booking.getUpdatedAt();
    }

    public Long getId() {
        return id;
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

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getOrigin() {
        return origin;
    }

    public Integer getAdults() {
        return adults;
    }

    public Integer getChildren() {
        return children;
    }

    public Integer getPets() {
        return pets;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getInstallments() {
        return installments;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getPaymentStatusLabel() {
        return paymentStatusLabel;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private String paymentStatusLabel(String paymentStatus) {
        return switch (paymentStatus) {
            case "PAID" -> "Pago";
            case "PARTIAL" -> "Parcial";
            default -> "Em espera";
        };
    }
}
