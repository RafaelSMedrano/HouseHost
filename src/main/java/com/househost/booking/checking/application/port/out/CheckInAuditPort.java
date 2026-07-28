package com.househost.booking.checking.application.port.out;

import java.util.Map;

public interface CheckInAuditPort {
    void record(String eventType, Long entityId, Map<String, Object> metadata);
}
