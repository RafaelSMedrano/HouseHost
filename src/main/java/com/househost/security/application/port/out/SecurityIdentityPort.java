package com.househost.security.application.port.out;

import com.househost.security.domain.model.SecurityIdentity;

import java.util.Optional;

public interface SecurityIdentityPort {

    Optional<SecurityIdentity> findByEmail(String email);
}
