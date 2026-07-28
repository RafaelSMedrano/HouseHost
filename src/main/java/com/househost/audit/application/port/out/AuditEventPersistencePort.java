package com.househost.audit.application.port.out;

import com.househost.audit.domain.model.AuditEvent;

public interface AuditEventPersistencePort {
    AuditEvent save(AuditEvent event);
}
