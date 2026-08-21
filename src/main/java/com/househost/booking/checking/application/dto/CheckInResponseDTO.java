package com.househost.booking.checking.application.dto;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.checking.domain.model.CheckIn;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanReplacementOutcomeDTO;

import java.time.LocalDateTime;

public class CheckInResponseDTO {

    private Long id;
    private Long bookingId;
    private Long guestId;
    private String guestName;
    private Long roomId;
    private String roomNumber;
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
    private FinancialTransactionPlanReplacementOutcomeDTO paymentMaterialization;

    public CheckInResponseDTO(CheckIn checkIn) {
        this(checkIn, null);
    }

    public CheckInResponseDTO(
            CheckIn checkIn,
            FinancialTransactionPlanReplacementOutcomeDTO paymentMaterialization
    ) {
        Booking booking = checkIn.getBooking();
        this.id = checkIn.getId();
        this.bookingId = booking == null ? null : booking.getId();
        this.guestId = checkIn.getGuest().getId();
        this.guestName = checkIn.getGuest().getFullName();
        this.roomId = checkIn.getRoom().getId();
        this.roomNumber = checkIn.getRoom().getRoomNumber();
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
        this.paymentMaterialization = paymentMaterialization;
    }

    public Long getId() {
        return id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getGuestId() {
        return guestId;
    }

    public String getGuestName() {
        return guestName;
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public Integer getAdults() {
        return adults;
    }

    public Integer getChildren() {
        return children;
    }

    public Integer getPets() {
        return pets;
    }

    public boolean isDocumentVerified() {
        return documentVerified;
    }

    public boolean isPaymentVerified() {
        return paymentVerified;
    }

    public boolean isRegistrationFormSigned() {
        return registrationFormSigned;
    }

    public boolean isRulesAccepted() {
        return rulesAccepted;
    }

    public boolean isKeysDelivered() {
        return keysDelivered;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public String getNotes() {
        return notes;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public FinancialTransactionPlanReplacementOutcomeDTO getPaymentMaterialization() {
        return paymentMaterialization;
    }
}
