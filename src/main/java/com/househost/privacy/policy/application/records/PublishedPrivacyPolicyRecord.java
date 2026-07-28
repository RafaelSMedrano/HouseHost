package com.househost.privacy.policy.application.records;

import java.time.LocalDateTime;

public record PublishedPrivacyPolicyRecord(
        Long policyId,
        int version,
        String contentHash,
        LocalDateTime effectiveAt
) {
}
