package com.househost.privacy.policy.adapter.out.persistence.entity;

import com.househost.privacy.policy.domain.model.PrivacyPolicy;
import com.househost.privacy.policy.domain.model.PrivacyPolicyContentHash;
import com.househost.privacy.policy.domain.model.PrivacyPolicyStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrivacyPolicyPersistenceMapperTest {
    @Test
    void preservesPublishedStateAndCurrentSlotInRoundTrip() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 26, 8, 0);
        LocalDateTime publishedAt = LocalDateTime.of(2026, 7, 26, 9, 0);
        PrivacyPolicy original = new PrivacyPolicy(2, "Politica", "{}", createdAt);
        original.restorePersistenceState(
                20L,
                new PrivacyPolicyContentHash(
                        "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
                ),
                PrivacyPolicyStatus.PUBLISHED,
                publishedAt,
                4L,
                createdAt,
                publishedAt
        );

        PrivacyPolicyJpaEntity entity = PrivacyPolicyPersistenceMapper.toEntity(original);
        PrivacyPolicy restored = PrivacyPolicyPersistenceMapper.toDomain(entity);

        assertEquals("CURRENT", entity.currentSlot);
        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getVersion(), restored.getVersion());
        assertEquals(original.getContentHash(), restored.getContentHash());
        assertEquals(original.getStatus(), restored.getStatus());
        assertEquals(original.getPublishedAt(), restored.getPublishedAt());
        assertEquals(original.getPublishedByUserId(), restored.getPublishedByUserId());
    }
}
