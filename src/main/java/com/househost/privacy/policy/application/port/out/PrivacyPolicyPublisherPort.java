package com.househost.privacy.policy.application.port.out;

public interface PrivacyPolicyPublisherPort {
    Long findPublisherIdByEmail(String email);

    Long findInitialPublisherId();
}
