package com.househost.booking.checkout.adapter.out.integration;

import com.househost.audit.application.service.AuditEventService;
import com.househost.booking.checkout.application.port.out.CheckOutAuditPort;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CheckOutAuditAdapter implements CheckOutAuditPort {
    private final AuditEventService auditEventService;

    public CheckOutAuditAdapter(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @Override
    public void record(String eventType, Long entityId, Map<String, Object> metadata) {
        auditEventService.recordForJwtActor(
                DataProcessingOperationCodes.STAY_MANAGEMENT,
                eventType,
                "CHECK_OUT",
                entityId,
                metadata
        );
    }
}
