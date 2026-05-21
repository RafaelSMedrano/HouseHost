package com.househost.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingRequestDTO {

    public Long guestId;
    public Long roomId;
    public LocalDate checkInDate;
    public LocalDate checkOutDate;
    public String status;
    public String origin;
    public Integer adults;
    public Integer children;
    public Integer pets;
    public String paymentMethod;
    public String installments;
    public BigDecimal dailyRate;
    public BigDecimal discount;
    public BigDecimal paidAmount;
    public LocalDate paymentDate;
    public boolean paymentCompleted;
    public String specialRequests;
    public String internalNotes;
}
