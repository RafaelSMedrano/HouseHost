package com.househost.auth.application.records;

import com.househost.auth.domain.model.LoginSecurityScope;
import java.time.Instant;

public record ActiveLoginRestrictionRecord(LoginSecurityScope scope, Instant blockedUntil,
                                           long remainingSeconds) {
}
