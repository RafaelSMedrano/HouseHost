package com.househost.booking.checkout.application.port.out;

import java.util.Map;

public interface CheckOutAuditPort {
    void record(String eventType, Long entityId, Map<String, Object> metadata);
}
