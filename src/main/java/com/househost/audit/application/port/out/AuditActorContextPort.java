package com.househost.audit.application.port.out;

import com.househost.audit.domain.model.AuditActor;
import com.househost.audit.domain.model.AuditEventContext;

public interface AuditActorContextPort {
    AuditActor currentActor();
    AuditEventContext currentRequestContext();
}
