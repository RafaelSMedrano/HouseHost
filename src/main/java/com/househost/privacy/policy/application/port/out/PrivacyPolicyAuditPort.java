package com.househost.privacy.policy.application.port.out;

import com.househost.privacy.policy.domain.model.PrivacyPolicy;

public interface PrivacyPolicyAuditPort {
    void record(String eventType, PrivacyPolicy privacyPolicy);
}
