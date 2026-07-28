package com.househost.auth.adapter.in.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.househost.auth.adapter.in.web.TrustedClientOriginResolver;
import com.househost.auth.application.port.in.AuthUseCase;
import com.househost.auth.application.port.in.UserUseCase;
import com.househost.auth.domain.exception.LoginProtectionUnavailableException;
import com.househost.auth.domain.exception.LoginTemporarilyBlockedException;
import com.househost.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthControllerLoginProtectionTest {
    private final AuthUseCase authUseCase = mock(AuthUseCase.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        UserUseCase userUseCase = mock(UserUseCase.class);
        TrustedClientOriginResolver originResolver = new TrustedClientOriginResolver("");
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(authUseCase, userUseCase, originResolver))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void blockedLoginReturnsGeneric429AndRetryAfter() throws Exception {
        when(authUseCase.login(any(), any())).thenThrow(new LoginTemporarilyBlockedException(73));
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"person@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "73"))
                .andExpect(jsonPath("$.message").value(
                        "Muitas tentativas de acesso. Aguarde alguns minutos e tente novamente."));
    }

    @Test
    void protectionFailureReturns503() throws Exception {
        when(authUseCase.login(any(), any())).thenThrow(new LoginProtectionUnavailableException());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"person@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isServiceUnavailable());
    }
}
