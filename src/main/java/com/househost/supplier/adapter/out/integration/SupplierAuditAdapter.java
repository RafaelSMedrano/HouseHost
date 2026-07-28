package com.househost.supplier.adapter.out.integration;

import com.househost.audit.application.service.AuditEventService;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import com.househost.supplier.application.port.out.SupplierAuditPort;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SupplierAuditAdapter implements SupplierAuditPort {
    private final AuditEventService auditEventService;
    public SupplierAuditAdapter(AuditEventService auditEventService) { this.auditEventService = auditEventService; }
    public void record(String eventType, Long entityId, Map<String, Object> metadataMap) {
        auditEventService.recordForJwtActor(DataProcessingOperationCodes.SUPPLIER_GOVERNANCE,
                eventType, "SUPPLIER", entityId, metadataMap);
    }
}
