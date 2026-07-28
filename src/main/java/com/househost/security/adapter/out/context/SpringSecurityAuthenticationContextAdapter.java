package com.househost.security.adapter.out.context;

import com.househost.security.application.port.out.AuthenticationContextPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityAuthenticationContextAdapter implements AuthenticationContextPort {

    @Override
    public Optional<String> currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String username = authentication.getName();
        if (username == null || username.isBlank() || "anonymousUser".equals(username)) {
            return Optional.empty();
        }

        return Optional.of(username.trim());
    }
}
