package com.househost.auth.domain.model;

import java.time.Duration;
import java.time.Instant;

public class LoginSecurityControl {
    private Long id;
    private final LoginSecurityScope scope;
    private final String scopeKey;
    private int failureCount;
    private Instant windowStartedAt;
    private Instant lastFailedAt;
    private Instant blockedUntil;
    private long version;
    private Instant createdAt;
    private Instant updatedAt;

    public LoginSecurityControl(LoginSecurityScope scope, String scopeKey) {
        if (scope == null || scopeKey == null || scopeKey.isBlank()) {
            throw new IllegalArgumentException("Scope and protected scope key are required");
        }
        this.scope = scope;
        this.scopeKey = scopeKey;
    }

    public boolean registerFailure(Instant now, int threshold, Duration window, Duration blockDuration) {
        boolean windowExpired = windowStartedAt == null || !now.isBefore(windowStartedAt.plus(window));
        if (windowExpired) {
            failureCount = 0;
            windowStartedAt = now;
        }
        failureCount++;
        lastFailedAt = now;
        boolean newlyBlocked = failureCount >= threshold && !isBlocked(now);
        if (newlyBlocked) {
            blockedUntil = now.plus(blockDuration);
        }
        updatedAt = now;
        if (createdAt == null) {
            createdAt = now;
        }
        return newlyBlocked;
    }

    public boolean isBlocked(Instant now) {
        return blockedUntil != null && now.isBefore(blockedUntil);
    }

    public long remainingSeconds(Instant now) {
        if (!isBlocked(now)) {
            return 0;
        }
        long seconds = Duration.between(now, blockedUntil).toSeconds();
        return Math.max(1, seconds + (Duration.between(now, blockedUntil).toMillisPart() > 0 ? 1 : 0));
    }

    public void clear(Instant now) {
        failureCount = 0;
        windowStartedAt = null;
        lastFailedAt = null;
        blockedUntil = null;
        updatedAt = now;
    }

    public void restorePersistenceState(Long id, int failureCount, Instant windowStartedAt,
                                        Instant lastFailedAt, Instant blockedUntil, long version,
                                        Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.failureCount = failureCount;
        this.windowStartedAt = windowStartedAt;
        this.lastFailedAt = lastFailedAt;
        this.blockedUntil = blockedUntil;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public LoginSecurityScope getScope() { return scope; }
    public String getScopeKey() { return scopeKey; }
    public int getFailureCount() { return failureCount; }
    public Instant getWindowStartedAt() { return windowStartedAt; }
    public Instant getLastFailedAt() { return lastFailedAt; }
    public Instant getBlockedUntil() { return blockedUntil; }
    public long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
