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

    private GuestStatus status = GuestStatus.INACTIVE;

    private GuestFinancialStatus financialStatus = GuestFinancialStatus.PAYMENT_SETTLED;

    private Integer stayCount;

    private BigDecimal totalSpent;

    private LocalDate lastStayDate;

    private String originChannel;

    private String notes;

    private String preferencesAndRestrictions;

    private String accessibilityNeeds;

    private List<Long> financialTransactionIdList = new ArrayList<>();

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
            String originChannel,
            String notes,
            String preferencesAndRestrictions,
            String accessibilityNeeds
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
        this.originChannel = originChannel;
        this.notes = notes;
        this.preferencesAndRestrictions = preferencesAndRestrictions;
        this.accessibilityNeeds = accessibilityNeeds;
    }

    public void restoreOperationalState(
            GuestStatus status,
            Integer stayCount,
            BigDecimal totalSpent,
            LocalDate lastStayDate
    ) {
        this.status = status == null ? GuestStatus.INACTIVE : status;
        this.stayCount = stayCount;
        this.totalSpent = totalSpent;
        this.lastStayDate = lastStayDate;
    }

    public void setStatus(GuestStatus status) {
        this.status = status == null ? GuestStatus.INACTIVE : status;
    }

    public void applyCompletedStay(
            LocalDate completedStayDate,
            BigDecimal finalizedStayAmount
    ) {
        stayCount = stayCount == null ? 1 : stayCount + 1;
        BigDecimal currentTotalSpent = totalSpent == null ? BigDecimal.ZERO : totalSpent;
        BigDecimal amountToAdd = finalizedStayAmount == null
                ? BigDecimal.ZERO
                : finalizedStayAmount.max(BigDecimal.ZERO);
        totalSpent = currentTotalSpent.add(amountToAdd);
        lastStayDate = completedStayDate;
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

    public Integer getStayCount() {
        return stayCount;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public LocalDate getLastStayDate() {
        return lastStayDate;
    }

    public String getOriginChannel() {
        return originChannel;
    }

    public String getNotes() {
        return notes;
    }

    public String getPreferencesAndRestrictions() {
        return preferencesAndRestrictions;
    }

    public String getAccessibilityNeeds() {
        return accessibilityNeeds;
    }

    public List<Long> getFinancialTransactionIds() {
        return List.copyOf(financialTransactionIdList);
    }

    public void addFinancialTransactionId(Long financialTransactionId) {
        if (financialTransactionId != null
                && !financialTransactionIdList.contains(financialTransactionId)) {
            financialTransactionIdList.add(financialTransactionId);
        }
    }

    public void removeFinancialTransactionId(Long financialTransactionId) {
        financialTransactionIdList.remove(financialTransactionId);
    }

    public void restorePersistenceState(
            Long id,
            GuestFinancialStatus financialStatus,
            List<Long> financialTransactionIdList,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.financialStatus = financialStatus == null
                ? GuestFinancialStatus.PAYMENT_SETTLED
                : financialStatus;
        this.financialTransactionIdList.clear();
        if (financialTransactionIdList != null) {
            this.financialTransactionIdList.addAll(financialTransactionIdList);
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
