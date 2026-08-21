package com.househost.guest.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.househost.guest.domain.model.GuestType;

import java.time.LocalDate;

@JsonIgnoreProperties({
        "status",
        "travelsWithPets",
        "petType",
        "needsAccessibility",
        "favoriteRoom",
        "stayCount",
        "totalSpent",
        "lastStayDate",
        "preferences",
        "referredBy"
})
public class GuestRegisterRequestDTO {

    public String fullName;
    public String email;
    public String phone;
    public String documentNumber;
    public String city;
    public String state;
    public String address;
    public LocalDate birthDate;
    public String gender;
    public GuestType guestType;
    public String originChannel;
    public String notes;
    public String preferencesAndRestrictions;
    public String accessibilityNeeds;
}
