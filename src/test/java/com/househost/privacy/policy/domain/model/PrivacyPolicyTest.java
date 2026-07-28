package com.househost.privacy.policy.domain.model;

import com.househost.shared.exception.PrivacyException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrivacyPolicyTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 27, 12, 0);
    private static final PrivacyPolicyContentHash HASH = new PrivacyPolicyContentHash(
            "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
    );

    @Test
    void permitsEditingOnlyWhileDraft() {
        PrivacyPolicy policy = draft();
        policy.updateDraft("Novo titulo", "{\"schemaVersion\":1}", NOW.plusDays(1));

        assertEquals("Novo titulo", policy.getTitle());
        policy.publish(HASH, 7L, NOW);

        assertThrows(PrivacyException.class, () ->
                policy.updateDraft("Alterado", "{}", NOW.plusDays(2)));
    }

    @Test
    void publishedAndSupersededPoliciesRemainImmutable() {
        PrivacyPolicy policy = draft();
        policy.publish(HASH, 7L, NOW);
        policy.supersede(NOW.plusHours(1));

        assertEquals(PrivacyPolicyStatus.SUPERSEDED, policy.getStatus());
        assertEquals(HASH, policy.getContentHash());
        assertEquals(7L, policy.getPublishedByUserId());
        assertThrows(PrivacyException.class, () -> policy.publish(HASH, 8L, NOW.plusHours(2)));
        assertThrows(PrivacyException.class, () -> policy.updateDraft("x", "{}", NOW));
    }

    private PrivacyPolicy draft() {
        return new PrivacyPolicy(2, "Politica", "{}", NOW);
    }
}
