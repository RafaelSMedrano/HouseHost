package com.househost.auth.dto;

public class LoginResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String photoUrl;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(Long id, String username, String email, String phone, String role, String photoUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.photoUrl = photoUrl;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
