package com.househost.auth.application.dto;
import com.househost.auth.domain.model.UserRole;
public class LoginResponseDTO {
    private Long id; private String username; private String email; private String phone; private UserRole role; private String photoUrl; private String token; private String tokenType; private Long expiresIn;
    public LoginResponseDTO() {}
    public LoginResponseDTO(Long id, String username, String email, String phone, UserRole role, String photoUrl) { this(id, username, email, phone, role, photoUrl, null, null); }
    public LoginResponseDTO(Long id, String username, String email, String phone, UserRole role, String photoUrl, String token, Long expiresIn) { this.id=id; this.username=username; this.email=email; this.phone=phone; this.role=role; this.photoUrl=photoUrl; this.token=token; this.tokenType=token == null ? null : "Bearer"; this.expiresIn=expiresIn; }
    public Long getId(){return id;} public String getUsername(){return username;} public String getEmail(){return email;} public String getPhone(){return phone;} public UserRole getRole(){return role;} public String getPhotoUrl(){return photoUrl;} public String getToken(){return token;} public String getTokenType(){return tokenType;} public Long getExpiresIn(){return expiresIn;}
}
