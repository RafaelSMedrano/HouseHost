package com.househost.privacy.policy.application.dto;

import com.househost.privacy.policy.domain.model.PrivacyPolicy;
import java.time.LocalDateTime;

public record PublicPrivacyPolicyResponseDTO(
        Long id,
        int version,
        String title,
        String content,
        String contentHash,
        LocalDateTime effectiveAt
) {
    public PublicPrivacyPolicyResponseDTO(PrivacyPolicy privacyPolicy) {
        this(
                privacyPolicy.getId(),
                privacyPolicy.getVersion(),
                privacyPolicy.getTitle(),
                privacyPolicy.getContent(),
                privacyPolicy.getContentHash().value(),
                privacyPolicy.getEffectiveAt()
        );
    }
}
