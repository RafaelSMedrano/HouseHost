package com.househost.guest.application.dto;

import com.househost.guest.domain.model.Guest;

public record GuestLookupResponseDTO(Long id, String fullName, String documentNumber) {

    public GuestLookupResponseDTO(Guest guest) {
        this(guest.getId(), guest.getFullName(), guest.getDocumentNumber());
    }
}
