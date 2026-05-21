package com.househost.guest.dto;

import com.househost.guest.model.Guest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class GuestResponseDTO {

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
    private String guestType;
    private String status;
    private String financialStatus;
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
    private List<String> preferences;
    private List<Long> financialTransactionIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GuestResponseDTO(Guest guest) {
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
        this.guestType = guest.getGuestType();
        this.status = guest.getStatus().name();
        this.financialStatus = guest.getFinancialStatus().name();
        this.travelsWithPets = guest.isTravelsWithPets();
        this.petType = guest.getPetType();
        this.needsAccessibility = guest.isNeedsAccessibility();
        this.favoriteRoom = guest.getFavoriteRoom();
        this.stayCount = guest.getStayCount();
        this.totalSpent = guest.getTotalSpent();
        this.lastStayDate = guest.getLastStayDate();
        this.rating = guest.getRating();
        this.originChannel = guest.getOriginChannel();
        this.referredBy = guest.getReferredBy();
        this.notes = guest.getNotes();
        this.preferences = guest.getPreferences();
        this.financialTransactionIds = guest.getFinancialTransactions().stream().map(transaction -> transaction.getId()).toList();
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

    public String getGuestType() {
        return guestType;
    }

    public String getStatus() {
        return status;
    }

    public String getFinancialStatus() {
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
        return financialTransactionIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
