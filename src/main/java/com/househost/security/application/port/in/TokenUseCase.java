package com.househost.security.application.port.in;

public interface TokenUseCase {

    String generateToken(String username);

    long getExpirationSeconds();
}
