package com.househost.guest.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Guest {

    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private String documentNumber;

    private String city;

    private String state;

    private String address;

    private LocalDate birthDate;

    private String gender;

    private GuestType guestType = GuestType.REGULAR;

    private GuestStatus status = GuestStatus.IN_BOOKING;

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

    private String notes;

    private List<String> preferences = new ArrayList<>();

    private List<Long> financialTransactionIds = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

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

    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

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

    public void changeFinancialStatus(GuestFinancialStatus financialStatus) {
        this.financialStatus = financialStatus == null
                ? GuestFinancialStatus.PAYMENT_SETTLED
                : financialStatus;
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

    public List<Long> getFinancialTransactionIds() {
        return List.copyOf(financialTransactionIds);
    }

    public void addFinancialTransactionId(Long financialTransactionId) {
        if (financialTransactionId != null && !financialTransactionIds.contains(financialTransactionId)) {
            financialTransactionIds.add(financialTransactionId);
        }
    }

    public void removeFinancialTransactionId(Long financialTransactionId) {
        financialTransactionIds.remove(financialTransactionId);
    }

    public void restorePersistenceState(
            Long id,
            GuestFinancialStatus financialStatus,
            List<Long> financialTransactionIds,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.financialStatus = financialStatus == null
                ? GuestFinancialStatus.PAYMENT_SETTLED
                : financialStatus;
        this.financialTransactionIds.clear();
        if (financialTransactionIds != null) {
            this.financialTransactionIds.addAll(financialTransactionIds);
        }
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}
