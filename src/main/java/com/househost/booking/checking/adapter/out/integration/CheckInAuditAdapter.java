package com.househost.booking.checking.adapter.out.integration;

import com.househost.audit.application.service.AuditEventService;
import com.househost.booking.checking.application.port.out.CheckInAuditPort;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class CheckInAuditAdapter implements CheckInAuditPort {
    private final AuditEventService auditEventService;

    public CheckInAuditAdapter(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @Override
    public void record(String eventType, Long entityId, Map<String, Object> metadata) {
        auditEventService.recordForJwtActor(
                DataProcessingOperationCodes.STAY_MANAGEMENT,
                eventType, "CHECK_IN", entityId, metadata
        );
    }
}
