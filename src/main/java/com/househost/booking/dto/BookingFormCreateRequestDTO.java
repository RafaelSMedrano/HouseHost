package com.househost.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingFormCreateRequestDTO {

    public GuestData guest;
    public ReservationData reservation;
    public PaymentData payment;
    public String origin;
    public String status;
    public String specialRequests;
    public String internalNotes;

    public static class GuestData {
        public String fullName;
        public String documentNumber;
        public String email;
        public String phone;
    }

    public static class ReservationData {
        public Long roomId;
        public String roomCode;
        public LocalDate checkInDate;
        public LocalDate checkOutDate;
        public Integer adults;
        public Integer children;
        public Integer pets;
    }

    public static class PaymentData {
        public String paymentMethod;
        public String installments;
        public BigDecimal dailyRate;
        public BigDecimal discount;
        public BigDecimal paidAmount;
        public LocalDate paymentDate;
        public boolean paymentCompleted;
    }
}
