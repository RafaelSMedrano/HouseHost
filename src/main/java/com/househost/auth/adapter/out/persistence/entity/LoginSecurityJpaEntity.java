package com.househost.auth.adapter.out.persistence.entity;

import com.househost.auth.domain.model.LoginSecurityScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;

@Entity
@Table(name = "login_attempt_controls",
        uniqueConstraints = @UniqueConstraint(name = "uk_login_attempt_scope_key", columnNames = {"scope_type", "scope_key"}),
        indexes = @Index(name = "idx_login_attempt_updated_at", columnList = "updated_at"))
public class LoginSecurityJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Enumerated(EnumType.STRING) @Column(name = "scope_type", nullable = false, length = 20)
    LoginSecurityScope scope;
    @Column(name = "scope_key", nullable = false, length = 64)
    String scopeKey;
    @Column(name = "failure_count", nullable = false)
    int failureCount;
    @Column(name = "window_started_at")
    Instant windowStartedAt;
    @Column(name = "last_failed_at")
    Instant lastFailedAt;
    @Column(name = "blocked_until")
    Instant blockedUntil;
    @Version
    long version;
    @Column(name = "created_at", nullable = false)
    Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}
