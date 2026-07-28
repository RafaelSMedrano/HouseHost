package com.househost.auth.adapter.out.integration;

import com.househost.audit.application.service.AuditEventService;
import com.househost.audit.domain.model.AuditEventContext;
import com.househost.auth.application.records.LoginRequestContextRecord;
import com.househost.auth.application.port.out.AuthAuditPort;
import com.househost.auth.domain.model.User;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AuthAuditAdapter implements AuthAuditPort {
    private final AuditEventService auditEventService;

    public AuthAuditAdapter(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @Override
    public void recordForJwtActor(String eventType, Long entityId, Map<String, Object> metadata) {
        auditEventService.recordForJwtActor(
                DataProcessingOperationCodes.USER_ACCESS_MANAGEMENT,
                eventType,
                "USER",
                entityId,
                metadata
        );
    }

    @Override
    public void recordForExplicitActor(String eventType, User actor, Map<String, Object> metadata) {
        auditEventService.recordForExplicitActor(
                DataProcessingOperationCodes.USER_ACCESS_MANAGEMENT,
                eventType,
                "USER",
                actor.getId(),
                "USER",
                actor.getId(),
                actor.getUsername(),
                metadata
        );
    }

    @Override
    public void recordLoginOutcome(String eventType, User knownUser, String emailHmacKey,
            LoginRequestContextRecord context, Map<String, Object> metadata) {
        Long userId = knownUser == null ? null : knownUser.getId();
        String actorType = knownUser == null ? "LOGIN_SUBJECT" : "USER";
        String actorLabel = knownUser == null ? emailHmacKey : knownUser.getUsername();
        auditEventService.recordForExplicitContext(
                DataProcessingOperationCodes.SECURITY_AUDIT_MANAGEMENT,
                eventType,
                "USER",
                userId,
                actorType,
                userId,
                actorLabel,
                new AuditEventContext(context.ipAddress(), context.userAgent()),
                metadata
        );
    }
}
