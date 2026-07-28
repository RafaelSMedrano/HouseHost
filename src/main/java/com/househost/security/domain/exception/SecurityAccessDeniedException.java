package com.househost.security.domain.exception;

public class SecurityAccessDeniedException extends RuntimeException {

    public SecurityAccessDeniedException(String message) {
        super(message);
    }
}
