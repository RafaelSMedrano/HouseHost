package com.househost.privacy.policy.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.househost.privacy.policy.application.dto.PrivacyPolicyRequestDTO;
import com.househost.privacy.policy.application.port.out.PrivacyPolicyAuditPort;
import com.househost.privacy.policy.application.port.out.PrivacyPolicyPersistencePort;
import com.househost.privacy.policy.application.port.out.PrivacyPolicyPublisherPort;
import com.househost.privacy.policy.domain.exception.PrivacyPolicyUnavailableException;
import com.househost.privacy.policy.domain.exception.PrivacyPolicyConflictException;
import com.househost.privacy.policy.domain.model.PrivacyPolicy;
import com.househost.privacy.policy.domain.model.PrivacyPolicyContentHash;
import com.househost.privacy.policy.domain.model.PrivacyPolicyStatus;
import com.househost.shared.exception.PrivacyException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrivacyPolicyServiceTest {
    private PrivacyPolicyPersistencePort persistencePort;
    private PrivacyPolicyPublisherPort publisherPort;
    private PrivacyPolicyAuditPort auditPort;
    private PrivacyPolicyService service;

    @BeforeEach
    void setUp() {
        persistencePort = mock(PrivacyPolicyPersistencePort.class);
        publisherPort = mock(PrivacyPolicyPublisherPort.class);
        auditPort = mock(PrivacyPolicyAuditPort.class);
        ObjectMapper objectMapper = new ObjectMapper();
        service = new PrivacyPolicyService(
                persistencePort,
                publisherPort,
                auditPort,
                new PrivacyPolicyValidationService(objectMapper),
                new PrivacyPolicyHashService(objectMapper)
        );
        when(persistencePort.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void publishingSupersedesPreviousPolicyAndRecordsMinimizedAuditEvents() {
        PrivacyPolicy current = publishedPolicy(1L, 1);
        PrivacyPolicy draft = draftPolicy(2L, 2);
        when(persistencePort.findById(2L)).thenReturn(Optional.of(draft));
        when(persistencePort.findCurrentPublishedForUpdate()).thenReturn(Optional.of(current));
        when(publisherPort.findPublisherIdByEmail("admin@example.com")).thenReturn(9L);

        var response = service.publish(2L, "admin@example.com");

        assertEquals(PrivacyPolicyStatus.SUPERSEDED, current.getStatus());
        assertEquals(PrivacyPolicyStatus.PUBLISHED, draft.getStatus());
        assertEquals(9L, draft.getPublishedByUserId());
        assertTrue(response.contentHash().matches("sha256:[0-9a-f]{64}"));
        verify(auditPort).record("PRIVACY_POLICY_SUPERSEDED", current);
        verify(auditPort).record("PRIVACY_POLICY_PUBLISHED", draft);
    }

    @Test
    void publicContractRequiresAndReturnsOnlyCurrentPublishedPolicy() {
        PrivacyPolicy current = publishedPolicy(2L, 2);
        when(persistencePort.findCurrentPublished()).thenReturn(Optional.of(current));

        var response = service.findCurrentPublished();
        var publishedPrivacyPolicyRecord = service.requireCurrentPublished(2L);

        assertEquals(2, response.version());
        assertEquals(current.getContentHash().value(), response.contentHash());
        assertEquals(2L, publishedPrivacyPolicyRecord.policyId());
    }

    @Test
    void publicContractFailsClosedWhenThereIsNoCurrentPolicy() {
        when(persistencePort.findCurrentPublished()).thenReturn(Optional.empty());

        assertThrows(PrivacyPolicyUnavailableException.class, service::findCurrentPublished);
    }

    @Test
    void acceptanceLocksCurrentPolicyAndReturnsServerEvidence() {
        PrivacyPolicy current = publishedPolicy(2L, 2);
        when(persistencePort.findCurrentPublishedForUpdate()).thenReturn(Optional.of(current));

        var publishedPrivacyPolicyRecord = service.requireCurrentPublishedForAcceptance(2L);

        assertEquals(2L, publishedPrivacyPolicyRecord.policyId());
        assertEquals(2, publishedPrivacyPolicyRecord.version());
        verify(persistencePort).findCurrentPublishedForUpdate();
    }

    @Test
    void acceptanceRejectsKnownPolicyThatIsNoLongerCurrent() {
        PrivacyPolicy previous = publishedPolicy(1L, 1);
        previous.supersede(LocalDateTime.of(2026, 7, 27, 10, 0));
        PrivacyPolicy current = publishedPolicy(2L, 2);
        when(persistencePort.findCurrentPublishedForUpdate()).thenReturn(Optional.of(current));
        when(persistencePort.findById(1L)).thenReturn(Optional.of(previous));

        assertThrows(
                PrivacyPolicyConflictException.class,
                () -> service.requireCurrentPublishedForAcceptance(1L)
        );
    }

    @Test
    void acceptanceFailsClosedWhenThereIsNoCurrentPolicy() {
        when(persistencePort.findCurrentPublishedForUpdate()).thenReturn(Optional.empty());

        assertThrows(
                PrivacyPolicyUnavailableException.class,
                () -> service.requireCurrentPublishedForAcceptance(2L)
        );
    }

    @Test
    void acceptanceRejectsUnknownPolicyAsControlledClientError() {
        PrivacyPolicy current = publishedPolicy(2L, 2);
        when(persistencePort.findCurrentPublishedForUpdate()).thenReturn(Optional.of(current));
        when(persistencePort.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                PrivacyException.class,
                () -> service.requireCurrentPublishedForAcceptance(999L)
        );
    }

    @Test
    void createDraftCanonicalizesContentBeforePersistence() {
        when(persistencePort.findByVersion(3)).thenReturn(Optional.empty());
        when(persistencePort.save(any())).thenAnswer(invocation -> {
            PrivacyPolicy policy = invocation.getArgument(0);
            policy.prepareForCreation();
            policy.restorePersistenceState(
                    3L, null, PrivacyPolicyStatus.DRAFT, null, null,
                    policy.getCreatedAt(), policy.getUpdatedAt()
            );
            return policy;
        });

        service.createDraft(request(3));

        ArgumentCaptor<PrivacyPolicy> captor = ArgumentCaptor.forClass(PrivacyPolicy.class);
        verify(persistencePort).save(captor.capture());
        assertEquals(
                "{\"schemaVersion\":1,\"sections\":[{\"heading\":\"Inicio\",\"nodes\":[{\"text\":\"Texto\",\"type\":\"paragraph\"}]}]}",
                captor.getValue().getContent()
        );
    }

    private PrivacyPolicy draftPolicy(Long id, int version) {
        PrivacyPolicy policy = new PrivacyPolicy(
                version, "Politica " + version, request(version).content,
                LocalDateTime.of(2026, 7, 26, 0, 0)
        );
        policy.restorePersistenceState(
                id, null, PrivacyPolicyStatus.DRAFT, null, null,
                LocalDateTime.of(2026, 7, 26, 0, 0),
                LocalDateTime.of(2026, 7, 26, 0, 0)
        );
        return policy;
    }

    private PrivacyPolicy publishedPolicy(Long id, int version) {
        PrivacyPolicy policy = draftPolicy(id, version);
        policy.publish(
                new PrivacyPolicyContentHash(
                        "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
                ),
                1L,
                LocalDateTime.of(2026, 7, 26, 10, 0)
        );
        return policy;
    }

    private PrivacyPolicyRequestDTO request(int version) {
        PrivacyPolicyRequestDTO request = new PrivacyPolicyRequestDTO();
        request.version = version;
        request.title = "Politica " + version;
        request.content = """
                {"sections":[{"nodes":[{"type":"paragraph","text":"Texto"}],"heading":"Inicio"}],"schemaVersion":1}
                """;
        request.effectiveAt = LocalDateTime.of(2026, 7, 26, 0, 0);
        return request;
    }
}
