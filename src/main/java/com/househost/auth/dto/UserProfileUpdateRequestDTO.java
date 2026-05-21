package com.househost.auth.dto;

public class UserProfileUpdateRequestDTO {

    public String username;
    public String email;
    public String phone;
    public String role;
    public String currentPassword;
    public String newPassword;

    public UserProfileUpdateRequestDTO() {
    }
}
