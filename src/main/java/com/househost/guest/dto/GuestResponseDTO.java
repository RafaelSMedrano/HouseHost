package com.househost.guest.dto;

import com.househost.guest.model.Guest;

import java.time.LocalDateTime;

public class GuestResponseDTO {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String documentNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GuestResponseDTO(Guest guest) {
        this.id = guest.getId();
        this.fullName = guest.getFullName();
        this.email = guest.getEmail();
        this.phone = guest.getPhone();
        this.documentNumber = guest.getDocumentNumber();
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
