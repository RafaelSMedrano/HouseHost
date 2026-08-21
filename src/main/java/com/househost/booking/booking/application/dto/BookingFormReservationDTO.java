package com.househost.booking.booking.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingFormReservationDTO {

    public Long roomId;
    public String roomCode;
    public LocalDate checkInDate;
    public LocalDate checkOutDate;
    public Integer adults;
    public Integer children;
    public Integer pets;
    public BigDecimal dailyRate;
    public BigDecimal discount;
}
