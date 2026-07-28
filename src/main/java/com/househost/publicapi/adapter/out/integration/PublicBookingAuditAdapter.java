package com.househost.publicapi.adapter.out.integration;

import com.househost.audit.application.service.AuditEventService;
import com.househost.audit.domain.model.AuditEventContext;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import com.househost.publicapi.application.port.out.PublicBookingAuditPort;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PublicBookingAuditAdapter implements PublicBookingAuditPort {

    private final AuditEventService auditEventService;

    public PublicBookingAuditAdapter(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @Override
    public void recordBookingEvent(
            String eventType,
            String entityType,
            Long entityId,
            String actorType,
            Long actorId,
            String actorLabel,
            AuditEventContext context,
            Map<String, Object> metadata
    ) {
        auditEventService.record(
                DataProcessingOperationCodes.BOOKING_MANAGEMENT,
                eventType,
                entityType,
                entityId,
                actorType,
                actorId,
                actorLabel,
                context,
                metadata
        );
    }

}
