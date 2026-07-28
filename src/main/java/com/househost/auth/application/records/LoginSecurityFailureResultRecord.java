package com.househost.auth.application.records;

import com.househost.auth.domain.model.LoginSecurityScope;
import java.time.Instant;

public record LoginSecurityFailureResultRecord(LoginSecurityScope scope, int failureCount,
                                              boolean newlyBlocked, Instant blockedUntil) {
}
