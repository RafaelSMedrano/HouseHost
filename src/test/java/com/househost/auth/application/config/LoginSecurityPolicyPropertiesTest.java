package com.househost.auth.application.config;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class LoginSecurityPolicyPropertiesTest {
    @Test
    void defaultsMatchGoverningSpec() {
        LoginSecurityPolicyProperties properties = new LoginSecurityPolicyProperties();
        properties.validate();
        assertPolicy(properties.getPair(), 10, Duration.ofMinutes(5), Duration.ofMinutes(15));
        assertPolicy(properties.getIp(), 30, Duration.ofMinutes(5), Duration.ofMinutes(30));
        assertPolicy(properties.getAccount(), 20, Duration.ofMinutes(10), Duration.ofMinutes(15));
        assertEquals(Duration.ofDays(30), properties.getRetention());
    }

    @Test
    void zeroOrNegativeConfigurationFailsValidation() {
        LoginSecurityPolicyProperties properties = new LoginSecurityPolicyProperties();
        properties.getPair().setMaxFailures(0);
        assertThrows(IllegalStateException.class, properties::validate);

        properties = new LoginSecurityPolicyProperties();
        properties.getIp().setWindow(Duration.ZERO);
        assertThrows(IllegalStateException.class, properties::validate);
    }

    private void assertPolicy(LoginSecurityPolicyProperties.ScopePolicy policy, int failures,
                              Duration window, Duration block) {
        assertEquals(failures, policy.getMaxFailures());
        assertEquals(window, policy.getWindow());
        assertEquals(block, policy.getBlock());
    }
}
