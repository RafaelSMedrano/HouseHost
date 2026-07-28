package com.househost.guest.adapter.out.integration;

import com.househost.audit.application.service.AuditEventService;
import com.househost.guest.application.port.out.GuestAuditPort;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GuestAuditAdapter implements GuestAuditPort {

    private final AuditEventService auditEventService;

    public GuestAuditAdapter(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @Override
    public void record(String eventType, Long entityId, Map<String, Object> metadata) {
        auditEventService.recordForJwtActor(
                DataProcessingOperationCodes.GUEST_MANAGEMENT,
                eventType,
                "GUEST",
                entityId,
                metadata
        );
    }
}
