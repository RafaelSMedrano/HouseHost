package com.househost.auth.adapter.out.integration;

import com.househost.auth.application.port.out.PasswordPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoderAdapter implements PasswordPort {
    private final PasswordEncoder passwordEncoder;
    public PasswordEncoderAdapter(PasswordEncoder passwordEncoder) { this.passwordEncoder=passwordEncoder; }
    public String encode(String rawPassword) { return passwordEncoder.encode(rawPassword); }
    public boolean matches(String rawPassword, String passwordHash) { return passwordEncoder.matches(rawPassword, passwordHash); }
}
