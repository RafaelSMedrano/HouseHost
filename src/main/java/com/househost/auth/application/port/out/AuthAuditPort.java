package com.househost.auth.application.port.out;

import com.househost.auth.application.records.LoginRequestContextRecord;
import com.househost.auth.domain.model.User;
import java.util.Map;

public interface AuthAuditPort {
    void recordForJwtActor(String eventType, Long entityId, Map<String, Object> metadata);
    void recordForExplicitActor(String eventType, User actor, Map<String, Object> metadata);
    void recordLoginOutcome(String eventType, User knownUser, String emailHmacKey,
                            LoginRequestContextRecord context, Map<String, Object> metadata);
}
