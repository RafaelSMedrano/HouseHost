package com.househost.security.application.port.in;

import com.househost.security.domain.model.SecurityIdentity;

public interface AuthenticationUseCase {

    SecurityIdentity authenticate(String token);
}
