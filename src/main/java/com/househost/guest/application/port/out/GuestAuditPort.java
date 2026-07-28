package com.househost.guest.application.port.out;

import java.util.Map;

public interface GuestAuditPort {
    void record(String eventType, Long entityId, Map<String, Object> metadata);
}
