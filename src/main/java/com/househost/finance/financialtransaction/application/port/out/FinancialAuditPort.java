package com.househost.finance.financialtransaction.application.port.out;

import java.util.Map;

public interface FinancialAuditPort {
    void record(String eventType, Long entityId, Map<String, Object> metadata);
}
