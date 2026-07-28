package com.househost.booking.booking.application.dto;

import com.househost.booking.booking.domain.model.BookingOrigin;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingRequestDTO {

    public Long guestId;
    public Long roomId;
    public LocalDate checkInDate;
    public LocalDate checkOutDate;
    public BookingStatus status;
    public BookingOrigin origin;
    public Integer adults;
    public Integer children;
    public Integer pets;
    public FinancialTransactionMethod paymentMethod;
    public String installments;
    public BigDecimal dailyRate;
    public BigDecimal discount;
    public BigDecimal paidAmount;
    public LocalDate paymentDate;
    public boolean paymentCompleted;
    public String specialRequests;
    public String internalNotes;
}
