package com.househost.booking.booking.application.dto;

import com.househost.booking.booking.domain.model.BookingOrigin;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingFormCreateRequestDTO {

    public GuestData guest;
    public ReservationData reservation;
    public PaymentData payment;
    public BookingOrigin origin;
    public BookingStatus status;
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
        public FinancialTransactionMethod paymentMethod;
        public String installments;
        public BigDecimal dailyRate;
        public BigDecimal discount;
        public BigDecimal paidAmount;
        public LocalDate paymentDate;
        public boolean paymentCompleted;
    }
}
