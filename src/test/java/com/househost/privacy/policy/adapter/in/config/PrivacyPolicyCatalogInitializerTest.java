package com.househost.privacy.policy.adapter.in.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.househost.privacy.policy.application.dto.PrivacyPolicyResponseDTO;
import com.househost.privacy.policy.application.port.out.PrivacyPolicyPersistencePort;
import com.househost.privacy.policy.application.service.PrivacyPolicyHashService;
import com.househost.privacy.policy.application.service.PrivacyPolicyService;
import com.househost.privacy.policy.application.service.PrivacyPolicyValidationService;
import com.househost.privacy.policy.domain.model.PrivacyPolicy;
import com.househost.privacy.policy.domain.model.PrivacyPolicyStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrivacyPolicyCatalogInitializerTest {
    private PrivacyPolicyPersistencePort persistencePort;
    private PrivacyPolicyService policyService;
    private PrivacyPolicyValidationService validationService;
    private PrivacyPolicyHashService hashService;
    private PrivacyPolicyCatalogInitializer initializer;

    @BeforeEach
    void setUp() {
        persistencePort = mock(PrivacyPolicyPersistencePort.class);
        policyService = mock(PrivacyPolicyService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        validationService = new PrivacyPolicyValidationService(objectMapper);
        hashService = new PrivacyPolicyHashService(objectMapper);
        initializer = new PrivacyPolicyCatalogInitializer(
                persistencePort, policyService, validationService, hashService
        );
    }

    @Test
    void createsOnlyVersionTwoAndPublishesItWhenCatalogIsEmpty() throws Exception {
        when(persistencePort.findByVersion(2)).thenReturn(Optional.empty());
        when(persistencePort.findCurrentPublished()).thenReturn(Optional.empty());
        when(policyService.createDraft(any())).thenReturn(new PrivacyPolicyResponseDTO(
                22L, 2, PrivacyPolicyInitialContent.TITLE, PrivacyPolicyInitialContent.CONTENT,
                null, PrivacyPolicyStatus.DRAFT, LocalDateTime.of(2026, 7, 26, 0, 0),
                null, null, LocalDateTime.now(), LocalDateTime.now()
        ));

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(persistencePort).findByVersion(2);
        verify(persistencePort, never()).findByVersion(1);
        verify(policyService).publishInitial(22L);
    }

    @Test
    void leavesMatchingPublishedVersionUntouchedOnEveryRestart() throws Exception {
        PrivacyPolicy existing = matchingPublishedPolicy();
        when(persistencePort.findByVersion(2)).thenReturn(Optional.of(existing));

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(policyService, never()).createDraft(any());
        verify(policyService, never()).publishInitial(any());
    }

    @Test
    void refusesToOverwriteConflictingVersionTwo() {
        PrivacyPolicy conflicting = new PrivacyPolicy(
                2,
                PrivacyPolicyInitialContent.TITLE,
                "{\"schemaVersion\":1,\"sections\":[]}",
                LocalDateTime.of(2026, 7, 26, 0, 0)
        );
        conflicting.restorePersistenceState(
                2L, null, PrivacyPolicyStatus.DRAFT, null, null,
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(persistencePort.findByVersion(2)).thenReturn(Optional.of(conflicting));

        assertThrows(IllegalStateException.class, () ->
                initializer.run(new DefaultApplicationArguments(new String[0])));
        verify(policyService, never()).createDraft(any());
    }

    private PrivacyPolicy matchingPublishedPolicy() {
        var request = new com.househost.privacy.policy.application.dto.PrivacyPolicyRequestDTO();
        request.version = 2;
        request.title = PrivacyPolicyInitialContent.TITLE;
        request.content = PrivacyPolicyInitialContent.CONTENT;
        request.effectiveAt = LocalDateTime.of(2026, 7, 26, 0, 0);
        String canonical = hashService.canonicalize(validationService.validate(request));
        PrivacyPolicy policy = new PrivacyPolicy(2, request.title, canonical, request.effectiveAt);
        policy.restorePersistenceState(
                2L,
                hashService.hash(canonical),
                PrivacyPolicyStatus.PUBLISHED,
                LocalDateTime.of(2026, 7, 26, 10, 0),
                1L,
                LocalDateTime.of(2026, 7, 26, 9, 0),
                LocalDateTime.of(2026, 7, 26, 10, 0)
        );
        return policy;
    }
}
