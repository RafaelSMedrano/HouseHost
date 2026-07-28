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
    private List<Long> bookingIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GuestRegisterResponseDTO(Guest guest, List<Long> bookingIds) {
        this(
                guest,
                guest.getEmail(),
                guest.getPhone(),
                guest.getDocumentNumber(),
                guest.getAddress(),
                guest.getBirthDate(),
                guest.getNotes(),
                bookingIds
        );
    }

    public GuestRegisterResponseDTO(
            Guest guest,
            String displayedEmail,
            String displayedPhone,
            List<Long> bookingIds
    ) {
        this(
                guest,
                displayedEmail,
                displayedPhone,
                guest.getDocumentNumber(),
                guest.getAddress(),
                guest.getBirthDate(),
                guest.getNotes(),
                bookingIds
        );
    }

    public GuestRegisterResponseDTO(
            Guest guest,
            String displayedEmail,
            String displayedPhone,
            String displayedDocumentNumber,
            String displayedAddress,
            LocalDate displayedBirthDate,
            String displayedNotes,
            List<Long> bookingIds
    ) {
        this.id = guest.getId();
        this.fullName = guest.getFullName();
        this.email = displayedEmail;
        this.phone = displayedPhone;
        this.documentNumber = displayedDocumentNumber;
        this.city = guest.getCity();
        this.state = guest.getState();
        this.address = displayedAddress;
        this.birthDate = displayedBirthDate;
        this.gender = guest.getGender();
        this.guestType = guest.getGuestTypeEnum();
        this.status = guest.getStatus();
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
        this.notes = displayedNotes;
        this.preferences = guest.getPreferences();
        this.bookingIds = bookingIds == null ? List.of() : List.copyOf(bookingIds);
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

    public List<Long> getBookingIds() {
        return bookingIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
