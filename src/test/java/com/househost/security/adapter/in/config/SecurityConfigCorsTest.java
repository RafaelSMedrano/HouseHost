package com.househost.security.adapter.in.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class SecurityConfigCorsTest {

    @Test
    void exposesCorrelationHeaderToBrowserClients() {
        SecurityConfig securityConfig = new SecurityConfig(null, new ObjectMapper());
        CorsConfigurationSource configurationSource = securityConfig.corsConfigurationSource();
        CorsConfiguration configuration = configurationSource.getCorsConfiguration(
                new MockHttpServletRequest("GET", "/rooms")
        );

        assertNotNull(configuration);
        List<String> exposedHeaderList = configuration.getExposedHeaders();
        assertNotNull(exposedHeaderList);
        assertTrue(exposedHeaderList.contains("X-Correlation-ID"));
    }
}
