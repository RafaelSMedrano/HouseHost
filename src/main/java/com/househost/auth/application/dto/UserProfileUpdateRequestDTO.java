package com.househost.auth.application.dto;
import com.househost.auth.domain.model.UserRole;
public class UserProfileUpdateRequestDTO { public String username; public String email; public String phone; public UserRole role; public String currentPassword; public String newPassword; public UserProfileUpdateRequestDTO() {} }
