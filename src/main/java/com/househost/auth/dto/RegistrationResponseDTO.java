package com.househost.auth.dto;

public class RegistrationResponseDTO {

    public Long id;
    public String username;
    public String email;
    public String phone;
    public String role;
    public String photoUrl;

    public RegistrationResponseDTO() {
    }

    public RegistrationResponseDTO(Long id, String username, String email, String phone, String role, String photoUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.photoUrl = photoUrl;
    }
}
