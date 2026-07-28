package com.househost.auth.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.househost.auth.application.dto.*;
import com.househost.auth.application.records.*;
import com.househost.auth.application.port.out.*;
import com.househost.auth.domain.exception.LoginProtectionUnavailableException;
import com.househost.auth.domain.exception.LoginTemporarilyBlockedException;
import com.househost.auth.domain.model.*;
import com.househost.security.application.port.in.AccessControlUseCase;
import com.househost.security.application.port.in.TokenUseCase;
import com.househost.shared.exception.InvalidLoginException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceLoginProtectionTest {
    private final UserPersistencePort users = mock(UserPersistencePort.class);
    private final PasswordPort passwords = mock(PasswordPort.class);
    private final TokenUseCase tokens = mock(TokenUseCase.class);
    private final AuthAuditPort audit = mock(AuthAuditPort.class);
    private final AccessControlUseCase access = mock(AccessControlUseCase.class);
    private final LoginSecurityService loginSecurityService = mock(LoginSecurityService.class);
    private final LoginSecurityAlertPort alerts = mock(LoginSecurityAlertPort.class);
    private AuthService service;
    private LoginRequestDTO request;
    private final LoginRequestContextRecord context = new LoginRequestContextRecord("203.0.113.5", "test-agent");

    @BeforeEach
    void setUp() {
        AuthValidationService validation = new AuthValidationService(users, passwords);
        service = new AuthService(users, passwords, tokens, audit, access, validation,
                loginSecurityService, alerts, "dummy-hash");
        request = new LoginRequestDTO();
        request.email = "Unknown@Example.com";
        request.password = "wrong";
        when(loginSecurityService.deriveEmailHmacKey(anyString())).thenReturn("email-hmac-key");
        when(loginSecurityService.ensureAllowed(anyString(), anyString())).thenReturn(Optional.empty());
    }

    @Test
    void unknownEmailPerformsDummyComparisonAndReturnsGenericInvalidLogin() {
        when(users.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(passwords.matches("wrong", "dummy-hash")).thenReturn(false);
        when(loginSecurityService.registerFailure(anyString(), anyString())).thenReturn(List.of());
        assertThrows(InvalidLoginException.class, () -> service.login(request, context));
        verify(passwords).matches("wrong", "dummy-hash");
        verify(audit).recordLoginOutcome(eq("USER_LOGIN_FAILED"), isNull(),
                eq("email-hmac-key"), eq(context), anyMap());
        verifyNoInteractions(tokens);
    }

    @Test
    void activeRestrictionSkipsPasswordAndTokenAndReturnsRetrySeconds() {
        when(loginSecurityService.ensureAllowed(anyString(), anyString())).thenReturn(Optional.of(
                new ActiveLoginRestrictionRecord(LoginSecurityScope.EMAIL_IP,
                        Instant.parse("2026-07-24T12:15:00Z"), 900)));
        LoginTemporarilyBlockedException exception = assertThrows(LoginTemporarilyBlockedException.class,
                () -> service.login(request, context));
        assertEquals(900, exception.getRetryAfterSeconds());
        verifyNoInteractions(passwords, tokens);
        verify(users, never()).findByEmail(anyString());
        verify(audit).recordLoginOutcome(eq("USER_LOGIN_RATE_LIMITED"), isNull(), anyString(),
                eq(context), anyMap());
    }

    @Test
    void firstAccountBlockAuditsAndAlerts() {
        when(users.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwords.matches(anyString(), anyString())).thenReturn(false);
        Instant blockedUntil = Instant.parse("2026-07-24T12:15:00Z");
        when(loginSecurityService.registerFailure(anyString(), anyString())).thenReturn(List.of(
                new LoginSecurityFailureResultRecord(LoginSecurityScope.ACCOUNT, 20, true, blockedUntil)));
        when(loginSecurityService.ensureAllowed(anyString(), anyString())).thenReturn(Optional.empty(), Optional.of(
                new ActiveLoginRestrictionRecord(LoginSecurityScope.ACCOUNT, blockedUntil, 900)));
        assertThrows(LoginTemporarilyBlockedException.class, () -> service.login(request, context));
        verify(audit).recordLoginOutcome(eq("USER_LOGIN_BLOCKED"), isNull(), anyString(),
                eq(context), anyMap());
        verify(alerts).send(argThat(alert -> "DISTRIBUTED_ACCOUNT_TARGETING".equals(alert.type())));
    }

    @Test
    void protectionFailureFailsClosedBeforeAuthentication() {
        when(loginSecurityService.ensureAllowed(anyString(), anyString())).thenThrow(new IllegalStateException("database down"));
        assertThrows(LoginProtectionUnavailableException.class, () -> service.login(request, context));
        verifyNoInteractions(passwords, tokens);
        verify(alerts).send(argThat(alert -> "LOGIN_PROTECTION_UNAVAILABLE".equals(alert.type())));
    }

    @Test
    void successfulLoginClearsStateBeforeGeneratingCompatibleResponse() {
        User user = new User("Admin", "admin@example.com", "real-hash", UserRole.ADMIN);
        user.restoreId(7L);
        request.email = "ADMIN@example.com";
        request.password = "correct";
        when(users.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(passwords.matches("correct", "real-hash")).thenReturn(true);
        when(tokens.generateToken("admin@example.com")).thenReturn("token");
        when(tokens.getExpirationSeconds()).thenReturn(3600L);
        LoginResponseDTO response = service.login(request, context);
        assertEquals("token", response.getToken());
        verify(loginSecurityService).registerSuccess("admin@example.com", "203.0.113.5");
        verify(tokens).generateToken("admin@example.com");
        verify(audit).recordForExplicitActor(eq("USER_LOGIN_SUCCEEDED"), eq(user), anyMap());
    }
}
