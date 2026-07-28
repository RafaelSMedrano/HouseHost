package com.househost.security.adapter.in.web;

import com.househost.security.application.port.in.AuthenticationUseCase;
import com.househost.security.domain.model.SecurityIdentity;
import com.househost.shared.dto.ResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthenticationUseCase authenticationUseCase;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(AuthenticationUseCase authenticationUseCase, ObjectMapper objectMapper) {
        this.authenticationUseCase = authenticationUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER); // Aqui o backend recebe o JWT enviado em Authorization: Bearer <token>.

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                SecurityIdentity identity = authenticationUseCase.authenticate(token);
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + identity.role());
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(identity.email(), null, java.util.List.of(authority));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
            writeUnauthorizedResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(new ResponseDTO("error", "Token invalido ou expirado.", null)));
    }
}
