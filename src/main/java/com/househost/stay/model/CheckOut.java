package com.househost.stay.model;

import com.househost.guest.model.Guest;
import com.househost.room.model.Room;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_outs")
public class CheckOut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "stay_id", unique = true, nullable = false)
    private Stay stay;

    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDateTime actualCheckOutAt;

    private boolean roomInspected;

    private boolean keysReturned;

    private boolean consumablesChecked;

    private boolean pendingAmountPaid;

    private BigDecimal extraCharges;

    private BigDecimal pendingAmount;

    private String performedBy;

    @Column(length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckOutStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public CheckOut() {
    }

    public CheckOut(Stay stay, Guest guest, Room room, LocalDateTime actualCheckOutAt, boolean roomInspected, boolean keysReturned, boolean consumablesChecked, boolean pendingAmountPaid, BigDecimal extraCharges, BigDecimal pendingAmount, String performedBy, String notes, CheckOutStatus status) {
        this.stay = stay;
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
        this.status = status;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (actualCheckOutAt == null) {
            actualCheckOutAt = now;
        }
        if (status == null) {
            status = CheckOutStatus.COMPLETED;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateCheckOut(Stay stay, Guest guest, Room room, LocalDateTime actualCheckOutAt, boolean roomInspected, boolean keysReturned, boolean consumablesChecked, boolean pendingAmountPaid, BigDecimal extraCharges, BigDecimal pendingAmount, String performedBy, String notes, CheckOutStatus status) {
        this.stay = stay;
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
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Stay getStay() {
        return stay;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDateTime getActualCheckOutAt() {
        return actualCheckOutAt;
    }

    public boolean isRoomInspected() {
        return roomInspected;
    }

    public boolean isKeysReturned() {
        return keysReturned;
    }

    public boolean isConsumablesChecked() {
        return consumablesChecked;
    }

    public boolean isPendingAmountPaid() {
        return pendingAmountPaid;
    }

    public BigDecimal getExtraCharges() {
        return extraCharges;
    }

    public BigDecimal getPendingAmount() {
        return pendingAmount;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public String getNotes() {
        return notes;
    }

    public CheckOutStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
