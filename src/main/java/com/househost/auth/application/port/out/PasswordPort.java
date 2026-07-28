package com.househost.auth.application.port.out;

public interface PasswordPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String passwordHash);
}
