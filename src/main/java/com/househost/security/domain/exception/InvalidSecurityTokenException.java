package com.househost.security.domain.exception;

public class InvalidSecurityTokenException extends RuntimeException {

    public InvalidSecurityTokenException() {
        super("Token invalido ou expirado.");
    }
}
