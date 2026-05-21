package com.househost.booking.model;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingOrigin origin = BookingOrigin.DIRETO_TELEFONE;

    private Integer adults;

    private Integer children;

    private Integer pets;

    private String paymentMethod;

    private String installments;

    private BigDecimal dailyRate;

    private BigDecimal discount;

    private BigDecimal paidAmount;

    private LocalDate paymentDate;

    @Column(length = 1000)
    private String specialRequests;

    @Column(length = 1000)
    private String internalNotes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Booking() {
    }

    public Booking(Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate, BookingStatus status, BigDecimal totalAmount) {
        this(guest, room, checkInDate, checkOutDate, status, totalAmount, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public Booking(Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate, BookingStatus status, BigDecimal totalAmount, BookingOrigin origin, Integer adults, Integer children, Integer pets, String paymentMethod, String installments, BigDecimal dailyRate, BigDecimal discount, BigDecimal paidAmount, LocalDate paymentDate, String specialRequests, String internalNotes) {
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.origin = origin == null ? BookingOrigin.DIRETO_TELEFONE : origin;
        this.adults = adults;
        this.children = children;
        this.pets = pets;
        this.paymentMethod = paymentMethod;
        this.installments = installments;
        this.dailyRate = dailyRate;
        this.discount = discount;
        this.paidAmount = paidAmount;
        this.paymentDate = paymentDate;
        this.specialRequests = specialRequests;
        this.internalNotes = internalNotes;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateBooking(Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate, BookingStatus status, BigDecimal totalAmount) {
        updateBooking(guest, room, checkInDate, checkOutDate, status, totalAmount, origin, adults, children, pets, paymentMethod, installments, dailyRate, discount, paidAmount, paymentDate, specialRequests, internalNotes);
    }

    public void updateBooking(Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate, BookingStatus status, BigDecimal totalAmount, BookingOrigin origin, Integer adults, Integer children, Integer pets, String paymentMethod, String installments, BigDecimal dailyRate, BigDecimal discount, BigDecimal paidAmount, LocalDate paymentDate, String specialRequests, String internalNotes) {
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.origin = origin == null ? BookingOrigin.DIRETO_TELEFONE : origin;
        this.adults = adults;
        this.children = children;
        this.pets = pets;
        this.paymentMethod = paymentMethod;
        this.installments = installments;
        this.dailyRate = dailyRate;
        this.discount = discount;
        this.paidAmount = paidAmount;
        this.paymentDate = paymentDate;
        this.specialRequests = specialRequests;
        this.internalNotes = internalNotes;
    }

    public void changeStatus(BookingStatus status) {
        this.status = status;
    }

    public void registerSettledPayment(BigDecimal amount, LocalDate paymentDate) {
        BigDecimal normalizedAmount = amount == null ? BigDecimal.ZERO : amount.max(BigDecimal.ZERO);
        BigDecimal currentPaidAmount = paidAmount == null ? BigDecimal.ZERO : paidAmount.max(BigDecimal.ZERO);
        BigDecimal newPaidAmount = currentPaidAmount.add(normalizedAmount);

        if (totalAmount != null && newPaidAmount.compareTo(totalAmount) > 0) {
            newPaidAmount = totalAmount;
        }

        this.paidAmount = newPaidAmount;
        this.paymentDate = paymentDate == null ? LocalDate.now() : paymentDate;
    }

    public BookingPaymentStatus getPaymentStatus() {
        BigDecimal total = totalAmount == null ? BigDecimal.ZERO : totalAmount.max(BigDecimal.ZERO);
        BigDecimal paid = paidAmount == null ? BigDecimal.ZERO : paidAmount.max(BigDecimal.ZERO);

        if (total.compareTo(BigDecimal.ZERO) > 0 && paid.compareTo(total) >= 0) {
            return BookingPaymentStatus.PAID;
        }

        if (paid.compareTo(BigDecimal.ZERO) > 0) {
            return BookingPaymentStatus.PARTIAL;
        }

        return BookingPaymentStatus.WAITING;
    }

    public Long getId() {
        return id;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BookingOrigin getOrigin() {
        return origin;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getInstallments() {
        return installments;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
