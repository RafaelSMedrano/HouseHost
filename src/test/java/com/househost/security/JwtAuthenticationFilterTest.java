package com.househost.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.househost.security.adapter.in.web.JwtAuthenticationFilter;
import com.househost.security.adapter.out.token.JwtTokenAdapter;
import com.househost.security.application.service.AuthenticationService;
import com.househost.security.domain.model.SecurityIdentity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.concurrent.atomic.AtomicBoolean;

class JwtAuthenticationFilterTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesUserWithRoleAuthority() throws Exception {
        SecurityIdentity identity = new SecurityIdentity(1L, "Recepcao", "recepcao@househost.test", "RECEPTION");

        JwtTokenAdapter tokenProvider = new JwtTokenAdapter();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                new AuthenticationService(tokenProvider, email -> java.util.Optional.of(identity)),
                new ObjectMapper()
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tokenProvider.generate(identity.email()));
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> chainCalled.set(true));

        assertEquals(identity.email(), SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_RECEPTION")));
        assertTrue(chainCalled.get());
    }

    @Test
    void rejectsTokenWhenUserNoLongerExists() throws Exception {
        String email = "removido@househost.test";

        JwtTokenAdapter tokenProvider = new JwtTokenAdapter();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                new AuthenticationService(tokenProvider, ignored -> java.util.Optional.empty()),
                new ObjectMapper()
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tokenProvider.generate(email));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> { });

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token invalido ou expirado"));
        assertTrue(SecurityContextHolder.getContext().getAuthentication() == null);
    }
}
