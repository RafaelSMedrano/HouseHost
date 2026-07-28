package com.househost.booking.checkout.domain.model;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.guest.domain.model.Guest;
import com.househost.room.domain.model.Room;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CheckOut {
    private Long id;
    private Booking booking;
    private Guest guest;
    private Room room;
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

    public CheckOut() {
    }

    public CheckOut(Booking booking, Guest guest, Room room, LocalDateTime actualCheckOutAt,
                    boolean roomInspected, boolean keysReturned, boolean consumablesChecked,
                    boolean pendingAmountPaid, BigDecimal extraCharges, BigDecimal pendingAmount,
                    String performedBy, String notes, CheckOutStatus status) {
        updateCheckOut(booking, guest, room, actualCheckOutAt, roomInspected, keysReturned,
                consumablesChecked, pendingAmountPaid, extraCharges, pendingAmount,
                performedBy, notes, status);
    }

    public void updateCheckOut(Booking booking, Guest guest, Room room, LocalDateTime actualCheckOutAt,
                               boolean roomInspected, boolean keysReturned, boolean consumablesChecked,
                               boolean pendingAmountPaid, BigDecimal extraCharges, BigDecimal pendingAmount,
                               String performedBy, String notes, CheckOutStatus status) {
        this.booking = booking;
        this.guest = guest;
        this.room = room;
        this.actualCheckOutAt = actualCheckOutAt;
        this.roomInspected = roomInspected;
        this.keysReturned = keysReturned;
        this.consumablesChecked = consumablesChecked;
        this.pendingAmountPaid = pendingAmountPaid;
        this.extraCharges = extraCharges;
        this.pendingAmount = pendingAmount;
        this.performedBy = performedBy;
        this.notes = notes;
        this.status = status == null ? CheckOutStatus.COMPLETED : status;
    }

    public void restorePersistenceState(Long id, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Booking getBooking() { return booking; }
    public Guest getGuest() { return guest; }
    public Room getRoom() { return room; }
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
