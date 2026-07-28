package com.househost.booking.checkout.application.dto;

import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.booking.checkout.domain.model.CheckOutStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CheckOutResponseDTO {
    private Long id;
    private Long bookingId;
    private Long guestId;
    private String guestName;
    private Long roomId;
    private String roomNumber;
    private LocalDateTime actualCheckOutAt;
    private boolean roomInspected;
    private boolean keysReturned;
    private boolean consumablesChecked;
    private boolean pendingAmountPaid;
    private BigDecimal extraCharges;
    private BigDecimal pendingAmount;
    private String performedBy;
    private String notes;
    private CheckOutStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CheckOutResponseDTO(CheckOut checkOut) {
        this.id = checkOut.getId();
        this.bookingId = checkOut.getBooking().getId();
        this.guestId = checkOut.getGuest().getId();
        this.guestName = checkOut.getGuest().getFullName();
        this.roomId = checkOut.getRoom().getId();
        this.roomNumber = checkOut.getRoom().getRoomNumber();
        this.actualCheckOutAt = checkOut.getActualCheckOutAt();
        this.roomInspected = checkOut.isRoomInspected();
        this.keysReturned = checkOut.isKeysReturned();
        this.consumablesChecked = checkOut.isConsumablesChecked();
        this.pendingAmountPaid = checkOut.isPendingAmountPaid();
        this.extraCharges = checkOut.getExtraCharges();
        this.pendingAmount = checkOut.getPendingAmount();
        this.performedBy = checkOut.getPerformedBy();
        this.notes = checkOut.getNotes();
        this.status = checkOut.getStatus();
        this.createdAt = checkOut.getCreatedAt();
        this.updatedAt = checkOut.getUpdatedAt();
    }

    public Long getId() { return id; }
    public Long getBookingId() { return bookingId; }
    public Long getGuestId() { return guestId; }
    public String getGuestName() { return guestName; }
    public Long getRoomId() { return roomId; }
    public String getRoomNumber() { return roomNumber; }
    public LocalDateTime getActualCheckOutAt() { return actualCheckOutAt; }
    public boolean isRoomInspected() { return roomInspected; }
    public boolean isKeysReturned() { return keysReturned; }
    public boolean isConsumablesChecked() { return consumablesChecked; }
    public boolean isPendingAmountPaid() { return pendingAmountPaid; }
    public BigDecimal getExtraCharges() { return extraCharges; }
    public BigDecimal getPendingAmount() { return pendingAmount; }
    public String getPerformedBy() { return performedBy; }
    public String getNotes() { return notes; }
    public CheckOutStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
