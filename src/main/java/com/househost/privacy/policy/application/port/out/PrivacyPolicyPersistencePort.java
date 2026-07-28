package com.househost.privacy.policy.application.port.out;

import com.househost.privacy.policy.domain.model.PrivacyPolicy;
import java.util.List;
import java.util.Optional;

public interface PrivacyPolicyPersistencePort {
    PrivacyPolicy save(PrivacyPolicy privacyPolicy);

    PrivacyPolicy saveAndFlush(PrivacyPolicy privacyPolicy);

    Optional<PrivacyPolicy> findById(Long id);

    Optional<PrivacyPolicy> findByVersion(int version);

    List<PrivacyPolicy> findAllByVersionDescending();

    Optional<PrivacyPolicy> findCurrentPublished();

    Optional<PrivacyPolicy> findCurrentPublishedForUpdate();
}
