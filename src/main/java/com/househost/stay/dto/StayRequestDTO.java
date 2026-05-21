package com.househost.stay.dto;

import java.time.LocalDate;

public class StayRequestDTO {

    public Long bookingId;
    public Long guestId;
    public Long roomId;
    public LocalDate checkInDate;
    public LocalDate expectedCheckOutDate;
    public LocalDate actualCheckOutDate;
    public String vehiclePlate;
    public String vehicleModel;
    public String status;
}
