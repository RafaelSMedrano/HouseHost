package com.househost.audit.adapter.out.persistence;

import com.househost.audit.adapter.out.persistence.entity.AuditEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface AuditEventJpaRepository extends JpaRepository<AuditEventJpaEntity, Long> {
}
