package com.househost.booking.dto;

import java.time.LocalDate;

public class BookingRequestDTO {

    public Long guestId;
    public Long roomId;
    public LocalDate checkInDate;
    public LocalDate checkOutDate;
    public String status;
}
