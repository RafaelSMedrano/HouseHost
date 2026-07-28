package com.househost.security.application.service;

import com.househost.security.application.port.in.AuthenticationUseCase;
import com.househost.security.application.port.out.SecurityIdentityPort;
import com.househost.security.application.port.out.TokenProviderPort;
import com.househost.security.domain.exception.InvalidSecurityTokenException;
import com.househost.security.domain.model.SecurityIdentity;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements AuthenticationUseCase {

    private final TokenProviderPort tokenProviderPort;
    private final SecurityIdentityPort securityIdentityPort;

    public AuthenticationService(TokenProviderPort tokenProviderPort, SecurityIdentityPort securityIdentityPort) {
        this.tokenProviderPort = tokenProviderPort;
        this.securityIdentityPort = securityIdentityPort;
    }

    @Override
    public SecurityIdentity authenticate(String token) {
        if (!tokenProviderPort.isValid(token)) {
            throw new InvalidSecurityTokenException();
        }

        String email = tokenProviderPort.extractSubject(token);
        return securityIdentityPort.findByEmail(email)
                .orElseThrow(InvalidSecurityTokenException::new);
    }
}
