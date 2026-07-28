package com.househost.privacy.policy.domain.model;

import com.househost.shared.exception.PrivacyException;

public record PrivacyPolicyContentHash(String value) {
    private static final String FORMAT = "sha256:[0-9a-f]{64}";

    public PrivacyPolicyContentHash {
        if (value == null || !value.matches(FORMAT)) {
            throw new PrivacyException("Hash da politica de privacidade invalido.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
