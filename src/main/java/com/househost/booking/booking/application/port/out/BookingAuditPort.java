package com.househost.booking.booking.application.port.out;

import java.util.Map;

public interface BookingAuditPort {

    void record(String eventType, String entityType, Long entityId, Map<String, Object> metadata);
}
