package com.househost.security.application.service;

import com.househost.security.application.port.in.TokenUseCase;
import com.househost.security.application.port.out.TokenProviderPort;
import org.springframework.stereotype.Service;

@Service
public class TokenService implements TokenUseCase {

    private final TokenProviderPort tokenProviderPort;

    public TokenService(TokenProviderPort tokenProviderPort) {
        this.tokenProviderPort = tokenProviderPort;
    }

    @Override
    public String generateToken(String username) {
        return tokenProviderPort.generate(username);
    }

    @Override
    public long getExpirationSeconds() {
        return tokenProviderPort.expirationSeconds();
    }
}
