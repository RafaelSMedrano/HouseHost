package com.househost.audit.adapter.out.persistence;

import com.househost.audit.adapter.out.persistence.entity.AuditEventPersistenceMapper;
import com.househost.audit.application.port.out.AuditEventPersistencePort;
import com.househost.audit.domain.model.AuditEvent;
import org.springframework.stereotype.Component;

@Component
public class AuditEventPersistenceAdapter implements AuditEventPersistencePort {
    private final AuditEventJpaRepository repository;
    public AuditEventPersistenceAdapter(AuditEventJpaRepository repository) { this.repository = repository; }
    public AuditEvent save(AuditEvent event) {
        return AuditEventPersistenceMapper.toDomain(repository.save(AuditEventPersistenceMapper.toEntity(event)));
    }
}
