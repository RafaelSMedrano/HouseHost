package com.househost.finance.financialtransaction.adapter.out.integration;

import com.househost.audit.application.service.AuditEventService;
import com.househost.finance.financialtransaction.application.port.out.FinancialAuditPort;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FinancialAuditAdapter implements FinancialAuditPort {

    private final AuditEventService auditEventService;

    public FinancialAuditAdapter(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @Override
    public void record(String eventType, Long entityId, Map<String, Object> metadata) {
        auditEventService.recordForJwtActor(
                DataProcessingOperationCodes.FINANCIAL_MANAGEMENT,
                eventType,
                "FINANCIAL_TRANSACTION",
                entityId,
                metadata
        );
    }
}
