package com.househost.shared.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.househost.auth.domain.exception.LoginProtectionUnavailableException;
import com.househost.auth.domain.exception.LoginTemporarilyBlockedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class LoginProtectionExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void blockedResponseHas429AndRetryAfter() {
        var response = handler.handleLoginTemporarilyBlocked(new LoginTemporarilyBlockedException(37));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("37", response.getHeaders().getFirst("Retry-After"));
        assertNotNull(response.getBody());
    }

    @Test
    void unavailableProtectionHas503() {
        var response = handler.handleLoginProtectionUnavailable(new LoginProtectionUnavailableException());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }
}
