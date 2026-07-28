package com.househost.auth.adapter.out.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.househost.audit.application.service.AuditEventService;
import com.househost.auth.application.records.LoginRequestContextRecord;
import com.househost.auth.domain.model.User;
import com.househost.auth.domain.model.UserRole;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuthAuditAdapterTest {

    private final AuditEventService auditEventService = mock(AuditEventService.class);
    private final AuthAuditAdapter authAuditAdapter = new AuthAuditAdapter(auditEventService);

    @Test
    void routesSecurityLoginOutcomeToSecurityAuditOperation() {
        LoginRequestContextRecord loginRequestContextRecord =
                new LoginRequestContextRecord("203.0.113.5", "test-agent");

        authAuditAdapter.recordLoginOutcome(
                "USER_LOGIN_FAILED",
                null,
                "email-hmac-key",
                loginRequestContextRecord,
                Map.of("scope", "EMAIL_IP")
        );

        verify(auditEventService).recordForExplicitContext(
                eq(DataProcessingOperationCodes.SECURITY_AUDIT_MANAGEMENT),
                eq("USER_LOGIN_FAILED"),
                eq("USER"),
                eq(null),
                eq("LOGIN_SUBJECT"),
                eq(null),
                eq("email-hmac-key"),
                any(),
                eq(Map.of("scope", "EMAIL_IP"))
        );
    }

    @Test
    void keepsOrdinaryUserEventUnderUserAccessOperation() {
        User user = new User("Admin", "admin@example.com", "hash", UserRole.ADMIN);
        user.restoreId(7L);

        authAuditAdapter.recordForExplicitActor("USER_LOGIN_SUCCEEDED", user, Map.of());

        verify(auditEventService).recordForExplicitActor(
                DataProcessingOperationCodes.USER_ACCESS_MANAGEMENT,
                "USER_LOGIN_SUCCEEDED",
                "USER",
                7L,
                "USER",
                7L,
                "Admin",
                Map.of()
        );
    }
}
