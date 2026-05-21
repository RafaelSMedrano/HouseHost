package com.househost.stay.model;

import com.househost.booking.model.Booking;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_ins")
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "stay_id", unique = true)
    private Stay stay;

    @OneToOne
    @JoinColumn(name = "booking_id", unique = true)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    private LocalDateTime expectedArrivalAt;

    @Column(nullable = false)
    private LocalDateTime actualCheckInAt;

    @Column(nullable = false)
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

    @Column(length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckInStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public CheckIn() {
    }

    public CheckIn(Stay stay, Booking booking, Guest guest, Room room, LocalDateTime expectedArrivalAt, LocalDateTime actualCheckInAt, LocalDate expectedCheckOutDate, Integer adults, Integer children, Integer pets, boolean documentVerified, boolean paymentVerified, boolean registrationFormSigned, boolean rulesAccepted, boolean keysDelivered, String vehiclePlate, String vehicleModel, String performedBy, String notes, CheckInStatus status) {
        this.stay = stay;
        this.booking = booking;
        this.guest = guest;
        this.room = room;
        this.expectedArrivalAt = expectedArrivalAt;
        this.actualCheckInAt = actualCheckInAt;
        this.expectedCheckOutDate = expectedCheckOutDate;
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
        this.status = status;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (actualCheckInAt == null) {
            actualCheckInAt = now;
        }
        if (status == null) {
            status = CheckInStatus.COMPLETED;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateCheckIn(Stay stay, Booking booking, Guest guest, Room room, LocalDateTime expectedArrivalAt, LocalDateTime actualCheckInAt, LocalDate expectedCheckOutDate, Integer adults, Integer children, Integer pets, boolean documentVerified, boolean paymentVerified, boolean registrationFormSigned, boolean rulesAccepted, boolean keysDelivered, String vehiclePlate, String vehicleModel, String performedBy, String notes, CheckInStatus status) {
        this.stay = stay;
        this.booking = booking;
        this.guest = guest;
        this.room = room;
        this.expectedArrivalAt = expectedArrivalAt;
        this.actualCheckInAt = actualCheckInAt;
        this.expectedCheckOutDate = expectedCheckOutDate;
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
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Stay getStay() {
        return stay;
    }

    public Booking getBooking() {
        return booking;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDateTime getExpectedArrivalAt() {
        return expectedArrivalAt;
    }

    public LocalDateTime getActualCheckInAt() {
        return actualCheckInAt;
    }

    public LocalDate getExpectedCheckOutDate() {
        return expectedCheckOutDate;
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

    public CheckInStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
