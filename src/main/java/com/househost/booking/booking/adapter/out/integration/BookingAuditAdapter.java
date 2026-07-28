package com.househost.booking.booking.adapter.out.integration;

import com.househost.audit.application.service.AuditEventService;
import com.househost.booking.booking.application.port.out.BookingAuditPort;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BookingAuditAdapter implements BookingAuditPort {

    private final AuditEventService auditEventService;

    public BookingAuditAdapter(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @Override
    public void record(String eventType, String entityType, Long entityId, Map<String, Object> metadata) {
        auditEventService.recordForJwtActor(
                DataProcessingOperationCodes.BOOKING_MANAGEMENT,
                eventType,
                entityType,
                entityId,
                metadata
        );
    }
}
