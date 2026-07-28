package com.househost.auth.adapter.out.persistence.entity;

import com.househost.auth.domain.model.LoginSecurityControl;

public final class LoginSecurityPersistenceMapper {
    private LoginSecurityPersistenceMapper() { }

    public static LoginSecurityControl toDomain(LoginSecurityJpaEntity entity) {
        LoginSecurityControl control = new LoginSecurityControl(entity.scope, entity.scopeKey);
        control.restorePersistenceState(entity.id, entity.failureCount, entity.windowStartedAt,
                entity.lastFailedAt, entity.blockedUntil, entity.version, entity.createdAt, entity.updatedAt);
        return control;
    }

    public static LoginSecurityJpaEntity toEntity(LoginSecurityControl control) {
        LoginSecurityJpaEntity entity = new LoginSecurityJpaEntity();
        entity.id = control.getId();
        entity.scope = control.getScope();
        entity.scopeKey = control.getScopeKey();
        entity.failureCount = control.getFailureCount();
        entity.windowStartedAt = control.getWindowStartedAt();
        entity.lastFailedAt = control.getLastFailedAt();
        entity.blockedUntil = control.getBlockedUntil();
        entity.version = control.getVersion();
        entity.createdAt = control.getCreatedAt();
        entity.updatedAt = control.getUpdatedAt();
        return entity;
    }
}
