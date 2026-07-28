package com.househost.auth.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class TrustedClientOriginResolverTest {
    @Test
    void directClientCannotRotateIdentityWithForwardedHeader() {
        TrustedClientOriginResolver resolver = new TrustedClientOriginResolver("10.0.0.0/8");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.9");
        request.addHeader("X-Forwarded-For", "198.51.100.1");
        assertEquals("203.0.113.9", resolver.resolve(request));
    }

    @Test
    void trustedProxyUsesFirstValidForwardedAddress() {
        TrustedClientOriginResolver resolver = new TrustedClientOriginResolver("10.0.0.0/8");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.1.2.3");
        request.addHeader("X-Forwarded-For", "198.51.100.7, 10.1.2.3");
        assertEquals("198.51.100.7", resolver.resolve(request));
    }

    @Test
    void malformedForwardedValueFallsBackToTrustedDirectPeer() {
        TrustedClientOriginResolver resolver = new TrustedClientOriginResolver("10.0.0.0/8");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.1.2.3");
        request.addHeader("X-Forwarded-For", "attacker.example");
        assertEquals("10.1.2.3", resolver.resolve(request));
    }
}
