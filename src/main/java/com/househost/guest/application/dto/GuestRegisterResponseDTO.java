package com.househost.guest.application.dto;

import com.househost.guest.domain.model.Guest;
import com.househost.guest.domain.model.GuestStatus;
import com.househost.guest.domain.model.GuestType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class GuestRegisterResponseDTO {

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
    private GuestType guestType;
    private GuestStatus status;
    private String financialStatus;
    private Integer stayCount;
    private BigDecimal totalSpent;
    private LocalDate lastStayDate;
    private String originChannel;
    private String notes;
    private String preferencesAndRestrictions;
    private String accessibilityNeeds;
    private List<Long> bookingIdList;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GuestRegisterResponseDTO(Guest guest, List<Long> bookingIdList) {
        this.id = guest.getId();
        this.fullName = guest.getFullName();
        this.email = guest.getEmail();
        this.phone = guest.getPhone();
        this.documentNumber = guest.getDocumentNumber();
        this.city = guest.getCity();
        this.state = guest.getState();
        this.address = guest.getAddress();
        this.birthDate = guest.getBirthDate();
        this.gender = guest.getGender();
        this.guestType = guest.getGuestTypeEnum();
        this.status = guest.getStatus();
        this.financialStatus = guest.getFinancialStatus().name();
        this.stayCount = guest.getStayCount();
        this.totalSpent = guest.getTotalSpent();
        this.lastStayDate = guest.getLastStayDate();
        this.originChannel = guest.getOriginChannel();
        this.notes = guest.getNotes();
        this.preferencesAndRestrictions = guest.getPreferencesAndRestrictions();
        this.accessibilityNeeds = guest.getAccessibilityNeeds();
        this.bookingIdList = bookingIdList == null ? List.of() : List.copyOf(bookingIdList);
        this.createdAt = guest.getCreatedAt();
        this.updatedAt = guest.getUpdatedAt();
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

    public GuestType getGuestType() {
        return guestType;
    }

    public GuestStatus getStatus() {
        return status;
    }

    public String getFinancialStatus() {
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

    public List<Long> getBookingIds() {
        return bookingIdList;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
