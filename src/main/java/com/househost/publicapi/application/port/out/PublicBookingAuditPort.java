package com.househost.publicapi.application.port.out;

import com.househost.audit.domain.model.AuditEventContext;

import java.util.Map;

public interface PublicBookingAuditPort {

    void recordBookingEvent(
            String eventType,
            String entityType,
            Long entityId,
            String actorType,
            Long actorId,
            String actorLabel,
            AuditEventContext context,
            Map<String, Object> metadata
    );
}
