package com.househost.guest.application.dto;

import com.househost.guest.domain.model.GuestStatus;
import com.househost.guest.domain.model.GuestType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    public GuestStatus status;
    public Boolean travelsWithPets;
    public String petType;
    public Boolean needsAccessibility;
    public String favoriteRoom;
    public Integer stayCount;
    public BigDecimal totalSpent;
    public LocalDate lastStayDate;
    public Integer rating;
    public String originChannel;
    public String referredBy;
    public String notes;
    public List<String> preferences;
}
