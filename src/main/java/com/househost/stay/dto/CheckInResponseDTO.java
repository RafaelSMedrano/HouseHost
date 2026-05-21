package com.househost.stay.dto;

import com.househost.booking.model.Booking;
import com.househost.stay.model.CheckIn;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CheckInResponseDTO {

    private Long id;
    private Long stayId;
    private Long bookingId;
    private Long guestId;
    private String guestName;
    private Long roomId;
    private String roomNumber;
    private LocalDateTime expectedArrivalAt;
    private LocalDateTime actualCheckInAt;
    private LocalDate expectedCheckOutDate;
    private Integer adults;
    private Integer children;
    private Integer pets;
    private boolean documentVerified;
    private boolean paymentVerified;
    private boolean registrationFormSigned;
    private boolean rulesAccepted;
    private boolean keysDelivered;
    private String vehiclePlate;
    private String vehicleModel;
    private String performedBy;
    private String notes;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CheckInResponseDTO(CheckIn checkIn) {
        Booking booking = checkIn.getBooking();
        this.id = checkIn.getId();
        this.stayId = checkIn.getStay() == null ? null : checkIn.getStay().getId();
        this.bookingId = booking == null ? null : booking.getId();
        this.guestId = checkIn.getGuest().getId();
        this.guestName = checkIn.getGuest().getFullName();
        this.roomId = checkIn.getRoom().getId();
        this.roomNumber = checkIn.getRoom().getRoomNumber();
        this.expectedArrivalAt = checkIn.getExpectedArrivalAt();
        this.actualCheckInAt = checkIn.getActualCheckInAt();
        this.expectedCheckOutDate = checkIn.getExpectedCheckOutDate();
        this.adults = checkIn.getAdults();
        this.children = checkIn.getChildren();
        this.pets = checkIn.getPets();
        this.documentVerified = checkIn.isDocumentVerified();
        this.paymentVerified = checkIn.isPaymentVerified();
        this.registrationFormSigned = checkIn.isRegistrationFormSigned();
        this.rulesAccepted = checkIn.isRulesAccepted();
        this.keysDelivered = checkIn.isKeysDelivered();
        this.vehiclePlate = checkIn.getVehiclePlate();
        this.vehicleModel = checkIn.getVehicleModel();
        this.performedBy = checkIn.getPerformedBy();
        this.notes = checkIn.getNotes();
        this.status = checkIn.getStatus().name();
        this.createdAt = checkIn.getCreatedAt();
        this.updatedAt = checkIn.getUpdatedAt();
    }

    public Long getId() { return id; }
    public Long getStayId() { return stayId; }
    public Long getBookingId() { return bookingId; }
    public Long getGuestId() { return guestId; }
    public String getGuestName() { return guestName; }
    public Long getRoomId() { return roomId; }
    public String getRoomNumber() { return roomNumber; }
    public LocalDateTime getExpectedArrivalAt() { return expectedArrivalAt; }
    public LocalDateTime getActualCheckInAt() { return actualCheckInAt; }
    public LocalDate getExpectedCheckOutDate() { return expectedCheckOutDate; }
    public Integer getAdults() { return adults; }
    public Integer getChildren() { return children; }
    public Integer getPets() { return pets; }
    public boolean isDocumentVerified() { return documentVerified; }
    public boolean isPaymentVerified() { return paymentVerified; }
    public boolean isRegistrationFormSigned() { return registrationFormSigned; }
    public boolean isRulesAccepted() { return rulesAccepted; }
    public boolean isKeysDelivered() { return keysDelivered; }
    public String getVehiclePlate() { return vehiclePlate; }
    public String getVehicleModel() { return vehicleModel; }
    public String getPerformedBy() { return performedBy; }
    public String getNotes() { return notes; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
