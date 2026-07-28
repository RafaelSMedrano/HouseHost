package com.househost.security.adapter.out.integration;

import com.househost.auth.application.port.out.UserPersistencePort;
import com.househost.security.application.port.out.SecurityIdentityPort;
import com.househost.security.domain.model.SecurityIdentity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserSecurityIdentityAdapter implements SecurityIdentityPort {

    private final UserPersistencePort userPersistencePort;

    public UserSecurityIdentityAdapter(UserPersistencePort userPersistencePort) {
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public Optional<SecurityIdentity> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        return userPersistencePort.findByEmail(email.trim())
                .map(user -> new SecurityIdentity(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole().name()
                ));
    }
}
