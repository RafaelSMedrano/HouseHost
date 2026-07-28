package com.househost.security.application.port.out;

public interface TokenProviderPort {

    String generate(String subject);

    String extractSubject(String token);

    boolean isValid(String token);

    long expirationSeconds();
}
