package com.househost.auth.application.dto;
import com.househost.auth.domain.model.UserRole;
public class RegistrationResponseDTO {
    public Long id; public String username; public String email; public String phone; public UserRole role; public String photoUrl;
    public RegistrationResponseDTO() {}
    public RegistrationResponseDTO(Long id, String username, String email, String phone, UserRole role, String photoUrl) { this.id=id; this.username=username; this.email=email; this.phone=phone; this.role=role; this.photoUrl=photoUrl; }
}
