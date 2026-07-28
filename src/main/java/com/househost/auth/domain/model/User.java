package com.househost.auth.domain.model;

public class User {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String passwordHash;
    private UserRole role;
    private String photoUrl;

    public User(String username, String email, String passwordHash, UserRole role) {
        this(username, email, passwordHash, role, null);
    }

    public User(String username, String email, String passwordHash, UserRole role, String photoUrl) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.photoUrl = photoUrl;
    }

    public void restoreId(Long id) { this.id = id; }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void updateProfile(String username, String email, String phone, UserRole role) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }
    public void updatePasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
