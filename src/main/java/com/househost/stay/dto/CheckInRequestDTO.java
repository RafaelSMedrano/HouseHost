package com.househost.stay.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CheckInRequestDTO {

    public Long stayId;
    public Long bookingId;
    public Long guestId;
    public Long roomId;
    public LocalDateTime expectedArrivalAt;
    public LocalDateTime actualCheckInAt;
    public LocalDate checkInDate;
    public LocalDate expectedCheckOutDate;
    public Integer adults;
    public Integer children;
    public Integer pets;
    public boolean documentVerified;
    public boolean paymentVerified;
    public boolean registrationFormSigned;
    public boolean rulesAccepted;
    public boolean keysDelivered;
    public String vehiclePlate;
    public String vehicleModel;
    public String performedBy;
    public String notes;
    public String status;
}
