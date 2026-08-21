package com.househost.finance.financialtransaction.application.port.out;

import java.util.Map;

public interface FinancialPostCommitAuditPort {

    void recordAfterCommit(
            String eventType,
            Long entityId,
            Map<String, Object> metadataMap
    );
}
