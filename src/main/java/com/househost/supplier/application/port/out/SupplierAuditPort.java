package com.househost.supplier.application.port.out;

import java.util.Map;

public interface SupplierAuditPort {
    void record(String eventType, Long entityId, Map<String, Object> metadataMap);
}
