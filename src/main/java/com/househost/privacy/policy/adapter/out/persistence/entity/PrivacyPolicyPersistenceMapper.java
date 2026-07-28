package com.househost.privacy.policy.adapter.out.persistence.entity;

import com.househost.privacy.policy.domain.model.PrivacyPolicy;
import com.househost.privacy.policy.domain.model.PrivacyPolicyContentHash;
import com.househost.privacy.policy.domain.model.PrivacyPolicyStatus;

public final class PrivacyPolicyPersistenceMapper {
    private PrivacyPolicyPersistenceMapper() {
    }

    public static PrivacyPolicy toDomain(PrivacyPolicyJpaEntity policyJpaEntity) {
        PrivacyPolicy privacyPolicy = new PrivacyPolicy(
                policyJpaEntity.version,
                policyJpaEntity.title,
                policyJpaEntity.content,
                policyJpaEntity.effectiveAt
        );
        privacyPolicy.restorePersistenceState(
                policyJpaEntity.id,
                policyJpaEntity.contentHash == null
                        ? null
                        : new PrivacyPolicyContentHash(policyJpaEntity.contentHash),
                policyJpaEntity.status,
                policyJpaEntity.publishedAt,
                policyJpaEntity.publishedByUserId,
                policyJpaEntity.createdAt,
                policyJpaEntity.updatedAt
        );
        return privacyPolicy;
    }

    public static PrivacyPolicyJpaEntity toEntity(PrivacyPolicy privacyPolicy) {
        PrivacyPolicyJpaEntity policyJpaEntity = new PrivacyPolicyJpaEntity();
        policyJpaEntity.id = privacyPolicy.getId();
        policyJpaEntity.version = privacyPolicy.getVersion();
        policyJpaEntity.title = privacyPolicy.getTitle();
        policyJpaEntity.content = privacyPolicy.getContent();
        policyJpaEntity.contentHash = privacyPolicy.getContentHash() == null
                ? null
                : privacyPolicy.getContentHash().value();
        policyJpaEntity.status = privacyPolicy.getStatus();
        policyJpaEntity.effectiveAt = privacyPolicy.getEffectiveAt();
        policyJpaEntity.publishedAt = privacyPolicy.getPublishedAt();
        policyJpaEntity.publishedByUserId = privacyPolicy.getPublishedByUserId();
        policyJpaEntity.currentSlot = privacyPolicy.getStatus() == PrivacyPolicyStatus.PUBLISHED
                ? "CURRENT"
                : null;
        policyJpaEntity.createdAt = privacyPolicy.getCreatedAt();
        policyJpaEntity.updatedAt = privacyPolicy.getUpdatedAt();
        return policyJpaEntity;
    }
}
