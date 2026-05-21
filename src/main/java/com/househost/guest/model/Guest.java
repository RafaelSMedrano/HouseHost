package com.househost.guest.model;

import com.househost.booking.model.Booking;
import com.househost.finance.model.FinancialTransaction;
import com.househost.finance.model.FinancialTransactionStatus;
import com.househost.stay.model.Stay;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "guests")
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true)
    private String email;

    private String phone;

    @Column(unique = true)
    private String documentNumber;

    private String city;

    private String state;

    private String address;

    private LocalDate birthDate;

    private String gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuestType guestType = GuestType.REGULAR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuestStatus status = GuestStatus.IN_BOOKING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuestFinancialStatus financialStatus = GuestFinancialStatus.PAYMENT_SETTLED;

    private boolean travelsWithPets;

    private String petType;

    private boolean needsAccessibility;

    private String favoriteRoom;

    private Integer stayCount;

    private BigDecimal totalSpent;

    private LocalDate lastStayDate;

    private Integer rating;

    private String originChannel;

    private String referredBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "guest_preferences", joinColumns = @JoinColumn(name = "guest_id"))
    @Column(name = "preference")
    private List<String> preferences = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "guest")
    private List<FinancialTransaction> financialTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "guest")
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "guest")
    private List<Stay> stays = new ArrayList<>();

    public Guest() {
    }

    public Guest(String fullName, String email, String phone, String documentNumber) {
        this(fullName, email, phone, documentNumber, null, null);
    }

    public Guest(String fullName, String email, String phone, String documentNumber, String city, LocalDate birthDate) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.documentNumber = documentNumber;
        this.city = city;
        this.birthDate = birthDate;
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

    public void updateProfile(String fullName, String email, String phone, String documentNumber) {
        updateProfile(fullName, email, phone, documentNumber, city, birthDate);
    }

    public void updateProfile(String fullName, String email, String phone, String documentNumber, String city, LocalDate birthDate) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.documentNumber = documentNumber;
        this.city = city;
        this.birthDate = birthDate;
    }

    public void updateProfile(
            String fullName,
            String email,
            String phone,
            String documentNumber,
            String city,
            String state,
            String address,
            LocalDate birthDate,
            String gender,
            GuestType guestType,
            GuestStatus status,
            boolean travelsWithPets,
            String petType,
            boolean needsAccessibility,
            String favoriteRoom,
            Integer stayCount,
            BigDecimal totalSpent,
            LocalDate lastStayDate,
            Integer rating,
            String originChannel,
            String referredBy,
            String notes,
            List<String> preferences
    ) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.documentNumber = documentNumber;
        this.city = city;
        this.state = state;
        this.address = address;
        this.birthDate = birthDate;
        this.gender = gender;
        this.guestType = guestType == null ? GuestType.REGULAR : guestType;
        this.status = status == null ? GuestStatus.IN_BOOKING : status;
        this.travelsWithPets = travelsWithPets;
        this.petType = petType;
        this.needsAccessibility = needsAccessibility;
        this.favoriteRoom = favoriteRoom;
        this.stayCount = stayCount;
        this.totalSpent = totalSpent;
        this.lastStayDate = lastStayDate;
        this.rating = rating;
        this.originChannel = originChannel;
        this.referredBy = referredBy;
        this.notes = notes;
        this.preferences.clear();
        if (preferences != null) {
            this.preferences.addAll(preferences);
        }
    }

    public void changeStatus(GuestStatus status) {
        this.status = status == null ? GuestStatus.IN_BOOKING : status;
    }

    public void addFinancialTransaction(FinancialTransaction financialTransaction) {
        if (financialTransaction == null || financialTransactions.contains(financialTransaction)) {
            return;
        }

        financialTransactions.add(financialTransaction);
        refreshFinancialStatus();
    }

    public void removeFinancialTransaction(FinancialTransaction financialTransaction) {
        financialTransactions.remove(financialTransaction);
        refreshFinancialStatus();
    }

    public void refreshFinancialStatus() {
        if (hasOverdueFinancialTransaction()) {
            financialStatus = GuestFinancialStatus.DEBTOR;
            return;
        }

        FinancialTransaction latestTransaction = findLatestFinancialTransaction();
        if (latestTransaction == null) {
            financialStatus = GuestFinancialStatus.PAYMENT_SETTLED;
            return;
        }

        if (latestTransaction.getStatus() == FinancialTransactionStatus.SETTLED
                || latestTransaction.getStatus() == FinancialTransactionStatus.PAID) {
            financialStatus = GuestFinancialStatus.PAYMENT_SETTLED;
            return;
        }

        if (latestTransaction.getStatus() == FinancialTransactionStatus.WAITING
                || latestTransaction.getStatus() == FinancialTransactionStatus.ON_TIME
                || latestTransaction.getStatus() == FinancialTransactionStatus.PARTIALLY_REALIZED) {
            financialStatus = GuestFinancialStatus.WAITING_PAYMENT;
            return;
        }

        financialStatus = GuestFinancialStatus.DEBTOR;
    }

    private boolean hasOverdueFinancialTransaction() {
        if (financialTransactions == null || financialTransactions.isEmpty()) {
            return false;
        }

        LocalDate today = LocalDate.now();
        return financialTransactions.stream()
                .filter(transaction -> transaction.getStatus() != FinancialTransactionStatus.SETTLED)
                .filter(transaction -> transaction.getTransactionDate() != null)
                .anyMatch(transaction -> transaction.getTransactionDate().isBefore(today));
    }

    private FinancialTransaction findLatestFinancialTransaction() {
        if (financialTransactions == null || financialTransactions.isEmpty()) {
            return null;
        }

        return financialTransactions.stream()
                .max(Comparator
                        .comparing(FinancialTransaction::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(FinancialTransaction::getId, Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElse(null);
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getAddress() {
        return address;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getGender() {
        return gender;
    }

    public String getGuestType() {
        return guestType == null ? GuestType.REGULAR.getApiValue() : guestType.getApiValue();
    }

    public GuestType getGuestTypeEnum() {
        return guestType;
    }

    public GuestStatus getStatus() {
        return status;
    }

    public GuestFinancialStatus getFinancialStatus() {
        refreshFinancialStatus();
        return financialStatus;
    }

    public boolean isTravelsWithPets() {
        return travelsWithPets;
    }

    public String getPetType() {
        return petType;
    }

    public boolean isNeedsAccessibility() {
        return needsAccessibility;
    }

    public String getFavoriteRoom() {
        return favoriteRoom;
    }

    public Integer getStayCount() {
        return stayCount;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public LocalDate getLastStayDate() {
        return lastStayDate;
    }

    public Integer getRating() {
        return rating;
    }

    public String getOriginChannel() {
        return originChannel;
    }

    public String getReferredBy() {
        return referredBy;
    }

    public String getNotes() {
        return notes;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<FinancialTransaction> getFinancialTransactions() {
        return financialTransactions;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public List<Stay> getStays() {
        return stays;
    }
}
