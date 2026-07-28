package com.househost.auth.application.dto;
import com.househost.auth.domain.model.UserRole;
public class RegistrationRequestDTO { public String username; public String password; public String email; public UserRole role; public String photoUrl; public RegistrationRequestDTO() {} }
