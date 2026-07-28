package com.househost.auth.adapter.out.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DummyPasswordConfig {
    @Bean("loginDummyPasswordHash")
    public String loginDummyPasswordHash(PasswordEncoder passwordEncoder) {
        return passwordEncoder.encode("househost-login-dummy-password");
    }
}
