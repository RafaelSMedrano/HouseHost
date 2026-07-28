package com.househost.privacy.policy.adapter.in.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.househost.privacy.policy.application.dto.PrivacyPolicyRequestDTO;
import com.househost.privacy.policy.application.dto.PrivacyPolicyResponseDTO;
import com.househost.privacy.policy.application.port.out.PrivacyPolicyPersistencePort;
import com.househost.privacy.policy.application.service.PrivacyPolicyHashService;
import com.househost.privacy.policy.application.service.PrivacyPolicyService;
import com.househost.privacy.policy.application.service.PrivacyPolicyValidationService;
import com.househost.privacy.policy.domain.model.PrivacyPolicy;
import com.househost.privacy.policy.domain.model.PrivacyPolicyContentHash;
import com.househost.privacy.policy.domain.model.PrivacyPolicyStatus;
import java.time.LocalDateTime;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(120)
public class PrivacyPolicyCatalogInitializer implements ApplicationRunner {
    private static final LocalDateTime EFFECTIVE_AT = LocalDateTime.of(2026, 7, 26, 0, 0);

    private final PrivacyPolicyPersistencePort persistencePort;
    private final PrivacyPolicyService policyService;
    private final PrivacyPolicyValidationService validationService;
    private final PrivacyPolicyHashService hashService;

    public PrivacyPolicyCatalogInitializer(
            PrivacyPolicyPersistencePort persistencePort,
            PrivacyPolicyService policyService,
            PrivacyPolicyValidationService validationService,
            PrivacyPolicyHashService hashService
    ) {
        this.persistencePort = persistencePort;
        this.policyService = policyService;
        this.validationService = validationService;
        this.hashService = hashService;
    }

    @Override
    public void run(ApplicationArguments args) {
        PrivacyPolicyRequestDTO request = initialRequest();
        JsonNode document = validationService.validate(request);
        String canonicalContent = hashService.canonicalize(document);
        PrivacyPolicyContentHash expectedHash = hashService.hash(canonicalContent);

        persistencePort.findByVersion(PrivacyPolicyInitialContent.VERSION)
                .ifPresentOrElse(
                        existingPolicy -> verifyOrPublish(existingPolicy, canonicalContent, expectedHash),
                        () -> createAndPublish(request)
                );
    }

    private void verifyOrPublish(
            PrivacyPolicy existingPolicy,
            String canonicalContent,
            PrivacyPolicyContentHash expectedHash
    ) {
        if (!canonicalContent.equals(existingPolicy.getContent())) {
            throw new IllegalStateException(
                    "A versao 2 existente possui conteudo diferente da politica publica confiavel."
            );
        }
        if (existingPolicy.getContentHash() != null
                && !expectedHash.equals(existingPolicy.getContentHash())) {
            throw new IllegalStateException(
                    "A versao 2 existente possui hash diferente da politica publica confiavel."
            );
        }
        if (existingPolicy.getStatus() == PrivacyPolicyStatus.DRAFT) {
            if (persistencePort.findCurrentPublished().isPresent()) {
                throw new IllegalStateException(
                        "Existe outra politica vigente; a versao 2 em rascunho exige revisao humana."
                );
            }
            policyService.publishInitial(existingPolicy.getId());
        }
    }

    private void createAndPublish(PrivacyPolicyRequestDTO request) {
        if (persistencePort.findCurrentPublished().isPresent()) {
            throw new IllegalStateException(
                    "Existe politica vigente sem a versao 2 confiavel; migracao interrompida."
            );
        }
        PrivacyPolicyResponseDTO createdPolicy = policyService.createDraft(request);
        policyService.publishInitial(createdPolicy.id());
    }

    private PrivacyPolicyRequestDTO initialRequest() {
        PrivacyPolicyRequestDTO request = new PrivacyPolicyRequestDTO();
        request.version = PrivacyPolicyInitialContent.VERSION;
        request.title = PrivacyPolicyInitialContent.TITLE;
        request.content = PrivacyPolicyInitialContent.CONTENT;
        request.effectiveAt = EFFECTIVE_AT;
        return request;
    }
}
