package com.househost.privacy.policy.adapter.out.integration;

import com.househost.audit.application.service.AuditEventService;
import com.househost.privacy.policy.domain.model.PrivacyPolicy;
import com.househost.privacy.policy.domain.model.PrivacyPolicyContentHash;
import com.househost.privacy.policy.domain.model.PrivacyPolicyStatus;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PrivacyPolicyAuditAdapterTest {
    @Test
    void recordsIntegrityEvidenceWithoutCopyingContentOrPublisherContact() {
        AuditEventService auditEventService = mock(AuditEventService.class);
        PrivacyPolicyAuditAdapter adapter = new PrivacyPolicyAuditAdapter(auditEventService);
        PrivacyPolicy policy = publishedPolicy();

        adapter.record("PRIVACY_POLICY_PUBLISHED", policy);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> metadataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(auditEventService).recordForJwtActor(
                eq("PRIVACY_GOVERNANCE"),
                eq("PRIVACY_POLICY_PUBLISHED"),
                eq("PRIVACY_POLICY"),
                eq(2L),
                metadataCaptor.capture()
        );
        Map<String, Object> metadata = metadataCaptor.getValue();
        assertEquals(2, metadata.get("version"));
        assertEquals("PUBLISHED", metadata.get("status"));
        assertEquals(policy.getContentHash().value(), metadata.get("contentHash"));
        assertFalse(metadata.containsKey("content"));
        assertFalse(metadata.containsKey("publishedByUserId"));
    }

    private PrivacyPolicy publishedPolicy() {
        PrivacyPolicy policy = new PrivacyPolicy(
                2, "Politica", "conteudo integral",
                LocalDateTime.of(2026, 7, 26, 0, 0)
        );
        policy.restorePersistenceState(
                2L,
                new PrivacyPolicyContentHash(
                        "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
                ),
                PrivacyPolicyStatus.PUBLISHED,
                LocalDateTime.of(2026, 7, 26, 10, 0),
                7L,
                LocalDateTime.of(2026, 7, 26, 9, 0),
                LocalDateTime.of(2026, 7, 26, 10, 0)
        );
        return policy;
    }
}
