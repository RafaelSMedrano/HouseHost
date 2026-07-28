package com.househost.booking.booking.domain.model;

import com.househost.guest.domain.model.Guest;
import com.househost.room.domain.model.Room;
import com.househost.shared.exception.BookingException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Booking {

    private Long id;

    private Guest guest;

    private Room room;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private BookingStatus status;

    private BigDecimal totalAmount;

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

    private BookingPaymentStatus paymentStatus = BookingPaymentStatus.WAITING;

    private String specialRequests;

    private String internalNotes;

    private String privacyPolicyVersion;

    private String privacyPolicyContentHash;

    private String termsVersion;

    private LocalDateTime privacyAcceptedAt;

    private Boolean marketingOptIn = false;

    private LocalDateTime marketingOptInAt;

    private LocalDateTime createdAt;

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
        this.paymentStatus = BookingPaymentStatus.WAITING;
        this.specialRequests = specialRequests;
        this.internalNotes = internalNotes;
    }

    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

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

    public void changePaymentStatus(BookingPaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus == null
                ? BookingPaymentStatus.WAITING
                : paymentStatus;

        if (this.paymentStatus == BookingPaymentStatus.PAID) {
            this.paidAmount = totalAmount;
            this.paymentDate = LocalDate.now();
        }
    }

    public void registerPrivacyAcceptance(
            String privacyPolicyVersion,
            String privacyPolicyContentHash,
            String termsVersion
    ) {
        if (privacyAcceptedAt != null) {
            throw new BookingException("O aceite de privacidade da reserva nao pode ser substituido.");
        }
        if (privacyPolicyVersion == null || privacyPolicyVersion.isBlank()) {
            throw new BookingException("A versao da politica aceita e obrigatoria.");
        }
        if (privacyPolicyContentHash == null
                || !privacyPolicyContentHash.matches("sha256:[0-9a-f]{64}")) {
            throw new BookingException("O hash da politica aceita e invalido.");
        }
        LocalDateTime now = LocalDateTime.now();
        this.privacyPolicyVersion = privacyPolicyVersion;
        this.privacyPolicyContentHash = privacyPolicyContentHash;
        this.termsVersion = termsVersion;
        this.privacyAcceptedAt = now;
        this.marketingOptIn = false;
        this.marketingOptInAt = null;
    }

    public BookingPaymentStatus getPaymentStatus() {
        return paymentStatus == null ? BookingPaymentStatus.WAITING : paymentStatus;
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

    public String getPrivacyPolicyVersion() {
        return privacyPolicyVersion;
    }

    public String getPrivacyPolicyContentHash() {
        return privacyPolicyContentHash;
    }

    public String getTermsVersion() {
        return termsVersion;
    }

    public LocalDateTime getPrivacyAcceptedAt() {
        return privacyAcceptedAt;
    }

    public Boolean getMarketingOptIn() {
        return marketingOptIn;
    }

    public LocalDateTime getMarketingOptInAt() {
        return marketingOptInAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void restorePersistenceState(
            Long id,
            BookingPaymentStatus paymentStatus,
            String privacyPolicyVersion,
            String privacyPolicyContentHash,
            String termsVersion,
            LocalDateTime privacyAcceptedAt,
            Boolean marketingOptIn,
            LocalDateTime marketingOptInAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.paymentStatus = paymentStatus == null ? BookingPaymentStatus.WAITING : paymentStatus;
        this.privacyPolicyVersion = privacyPolicyVersion;
        this.privacyPolicyContentHash = privacyPolicyContentHash;
        this.termsVersion = termsVersion;
        this.privacyAcceptedAt = privacyAcceptedAt;
        this.marketingOptIn = Boolean.TRUE.equals(marketingOptIn);
        this.marketingOptInAt = marketingOptInAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
