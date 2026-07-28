package com.househost.shared.exception;

import com.househost.privacy.policy.domain.exception.PrivacyPolicyConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrivacyPolicyConflictExceptionHandlerTest {
    @Test
    void mapsOnlyPolicyVersionConflictToHttp409() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        var response = handler.handlePrivacyPolicyConflict(
                new PrivacyPolicyConflictException("Politica atualizada.")
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Politica atualizada.", response.getBody().getMessage());
    }
}
