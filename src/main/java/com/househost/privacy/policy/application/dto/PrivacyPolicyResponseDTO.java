package com.househost.privacy.policy.application.dto;

import com.househost.privacy.policy.domain.model.PrivacyPolicy;
import com.househost.privacy.policy.domain.model.PrivacyPolicyStatus;
import java.time.LocalDateTime;

public record PrivacyPolicyResponseDTO(
        Long id,
        int version,
        String title,
        String content,
        String contentHash,
        PrivacyPolicyStatus status,
        LocalDateTime effectiveAt,
        LocalDateTime publishedAt,
        Long publishedByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public PrivacyPolicyResponseDTO(PrivacyPolicy privacyPolicy) {
        this(
                privacyPolicy.getId(),
                privacyPolicy.getVersion(),
                privacyPolicy.getTitle(),
                privacyPolicy.getContent(),
                privacyPolicy.getContentHash() == null
                        ? null
                        : privacyPolicy.getContentHash().value(),
                privacyPolicy.getStatus(),
                privacyPolicy.getEffectiveAt(),
                privacyPolicy.getPublishedAt(),
                privacyPolicy.getPublishedByUserId(),
                privacyPolicy.getCreatedAt(),
                privacyPolicy.getUpdatedAt()
        );
    }
}
