package com.househost.auth.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class LoginSecurityControlTest {
    private static final Instant NOW = Instant.parse("2026-07-24T12:00:00Z");

    @Test
    void tenthFailureBlocksPairForFifteenMinutes() {
        LoginSecurityControl control = new LoginSecurityControl(LoginSecurityScope.EMAIL_IP, "protected");
        for (int attempt = 1; attempt < 10; attempt++) {
            assertFalse(control.registerFailure(NOW.plusSeconds(attempt), 10,
                    Duration.ofMinutes(5), Duration.ofMinutes(15)));
        }
        assertTrue(control.registerFailure(NOW.plusSeconds(10), 10,
                Duration.ofMinutes(5), Duration.ofMinutes(15)));
        assertEquals(10, control.getFailureCount());
        assertEquals(NOW.plusSeconds(10).plus(Duration.ofMinutes(15)), control.getBlockedUntil());
    }

    @Test
    void expiredWindowRestartsAtOneAndRestrictionExpiresWithoutCleanup() {
        LoginSecurityControl control = new LoginSecurityControl(LoginSecurityScope.EMAIL_IP, "protected");
        control.registerFailure(NOW, 2, Duration.ofMinutes(5), Duration.ofMinutes(15));
        control.registerFailure(NOW.plusSeconds(1), 2, Duration.ofMinutes(5), Duration.ofMinutes(15));
        assertFalse(control.isBlocked(NOW.plus(Duration.ofMinutes(16))));
        control.registerFailure(NOW.plus(Duration.ofMinutes(20)), 2,
                Duration.ofMinutes(5), Duration.ofMinutes(15));
        assertEquals(1, control.getFailureCount());
    }
}
