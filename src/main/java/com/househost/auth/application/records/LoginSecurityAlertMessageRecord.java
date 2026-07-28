package com.househost.auth.application.records;

import com.househost.auth.domain.model.LoginSecurityScope;
import java.time.Instant;

public record LoginSecurityAlertMessageRecord(String type, LoginSecurityScope scope, int failureCount,
                                              Instant blockedUntil, String emailHmacKey,
                                              String detail) {
}
