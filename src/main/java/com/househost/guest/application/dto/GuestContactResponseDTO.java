package com.househost.guest.application.dto;

public class GuestContactResponseDTO {

    private final Long guestId;
    private final String email;
    private final String phone;

    public GuestContactResponseDTO(Long guestId, String email, String phone) {
        this.guestId = guestId;
        this.email = email;
        this.phone = phone;
    }

    public Long getGuestId() {
        return guestId;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
