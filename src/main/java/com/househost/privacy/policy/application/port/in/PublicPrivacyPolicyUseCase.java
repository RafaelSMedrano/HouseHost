package com.househost.privacy.policy.application.port.in;

import com.househost.privacy.policy.application.dto.PublicPrivacyPolicyResponseDTO;
import com.househost.privacy.policy.application.records.PublishedPrivacyPolicyRecord;

public interface PublicPrivacyPolicyUseCase {
    PublicPrivacyPolicyResponseDTO findCurrentPublished();

    PublishedPrivacyPolicyRecord requireCurrentPublished(Long policyId);

    PublishedPrivacyPolicyRecord requireCurrentPublishedForAcceptance(Long policyId);
}
