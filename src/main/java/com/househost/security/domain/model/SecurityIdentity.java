package com.househost.security.domain.model;

public record SecurityIdentity(Long id, String username, String email, String role) {
}
