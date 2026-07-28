package com.househost.auth.application.dto;

import com.househost.auth.domain.model.UserRole;

public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private UserRole role;
    private String photoUrl;

    public UserResponseDTO() {
    }

    public UserResponseDTO(Long id, String username, String email, String phone, UserRole role, String photoUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.photoUrl = photoUrl;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public UserRole getRole() { return role; }
    public String getPhotoUrl() { return photoUrl; }
}
