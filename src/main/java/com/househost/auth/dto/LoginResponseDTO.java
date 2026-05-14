package com.househost.auth.dto;

public class LoginResponseDTO {

    private String username;
    private String role;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
