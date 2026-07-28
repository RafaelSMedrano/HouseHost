package com.househost.booking.checking.domain.model;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.guest.domain.model.Guest;
import com.househost.room.domain.model.Room;

import java.time.LocalDateTime;

public class CheckIn {
    private Long id;
    private Booking booking;
    private Guest guest;
    private Room room;
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
    private CheckInStatus status;
    private LocalDateTime createdAt;

    public CheckIn() {
    }

    public CheckIn(Booking booking, Guest guest, Room room, Integer adults,
                   Integer children, Integer pets, boolean documentVerified, boolean paymentVerified,
                   boolean registrationFormSigned, boolean rulesAccepted, boolean keysDelivered,
                   String vehiclePlate, String vehicleModel, String performedBy, String notes,
                   CheckInStatus status) {
        updateCheckIn(booking, guest, room, adults, children, pets, documentVerified, paymentVerified,
                registrationFormSigned, rulesAccepted, keysDelivered, vehiclePlate, vehicleModel,
                performedBy, notes, status);
    }

    public void updateCheckIn(Booking booking, Guest guest, Room room,
                              Integer adults, Integer children,
                              Integer pets, boolean documentVerified, boolean paymentVerified,
                              boolean registrationFormSigned, boolean rulesAccepted,
                              boolean keysDelivered, String vehiclePlate, String vehicleModel,
                              String performedBy, String notes, CheckInStatus status) {
        this.booking = booking;
        this.guest = guest;
        this.room = room;
        this.adults = adults;
        this.children = children;
        this.pets = pets;
        this.documentVerified = documentVerified;
        this.paymentVerified = paymentVerified;
        this.registrationFormSigned = registrationFormSigned;
        this.rulesAccepted = rulesAccepted;
        this.keysDelivered = keysDelivered;
        this.vehiclePlate = vehiclePlate;
        this.vehicleModel = vehicleModel;
        this.performedBy = performedBy;
        this.notes = notes;
        this.status = status == null ? CheckInStatus.COMPLETED : status;
    }

    public void restorePersistenceState(Long id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Booking getBooking() { return booking; }
    public Guest getGuest() { return guest; }
    public Room getRoom() { return room; }
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
    public CheckInStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
