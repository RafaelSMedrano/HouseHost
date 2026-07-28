package com.househost.privacy.policy.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.househost.privacy.policy.application.dto.PrivacyPolicyRequestDTO;
import com.househost.privacy.policy.application.dto.PrivacyPolicyResponseDTO;
import com.househost.privacy.policy.application.dto.PublicPrivacyPolicyResponseDTO;
import com.househost.privacy.policy.application.port.in.PrivacyPolicyUseCase;
import com.househost.privacy.policy.application.port.in.PublicPrivacyPolicyUseCase;
import com.househost.privacy.policy.application.port.out.PrivacyPolicyAuditPort;
import com.househost.privacy.policy.application.port.out.PrivacyPolicyPersistencePort;
import com.househost.privacy.policy.application.port.out.PrivacyPolicyPublisherPort;
import com.househost.privacy.policy.application.records.PublishedPrivacyPolicyRecord;
import com.househost.privacy.policy.domain.exception.PrivacyPolicyUnavailableException;
import com.househost.privacy.policy.domain.exception.PrivacyPolicyConflictException;
import com.househost.privacy.policy.domain.model.PrivacyPolicy;
import com.househost.privacy.policy.domain.model.PrivacyPolicyContentHash;
import com.househost.shared.exception.PrivacyException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrivacyPolicyService implements PrivacyPolicyUseCase, PublicPrivacyPolicyUseCase {
    private final PrivacyPolicyPersistencePort persistencePort;
    private final PrivacyPolicyPublisherPort publisherPort;
    private final PrivacyPolicyAuditPort auditPort;
    private final PrivacyPolicyValidationService validationService;
    private final PrivacyPolicyHashService hashService;

    public PrivacyPolicyService(
            PrivacyPolicyPersistencePort persistencePort,
            PrivacyPolicyPublisherPort publisherPort,
            PrivacyPolicyAuditPort auditPort,
            PrivacyPolicyValidationService validationService,
            PrivacyPolicyHashService hashService
    ) {
        this.persistencePort = persistencePort;
        this.publisherPort = publisherPort;
        this.auditPort = auditPort;
        this.validationService = validationService;
        this.hashService = hashService;
    }

    @Override
    @Transactional
    public PrivacyPolicyResponseDTO createDraft(PrivacyPolicyRequestDTO request) {
        String canonicalContent = validateAndCanonicalize(request);
        if (persistencePort.findByVersion(request.version).isPresent()) {
            throw new PrivacyException("Ja existe uma politica com esta versao.");
        }
        PrivacyPolicy privacyPolicy = new PrivacyPolicy(
                request.version,
                request.title.trim(),
                canonicalContent,
                request.effectiveAt
        );
        PrivacyPolicy savedPrivacyPolicy = persistencePort.save(privacyPolicy);
        auditPort.record("PRIVACY_POLICY_DRAFT_CREATED", savedPrivacyPolicy);
        return new PrivacyPolicyResponseDTO(savedPrivacyPolicy);
    }

    @Override
    @Transactional
    public PrivacyPolicyResponseDTO updateDraft(Long id, PrivacyPolicyRequestDTO request) {
        PrivacyPolicy privacyPolicy = findPolicy(id);
        if (request == null || request.version == null || request.version != privacyPolicy.getVersion()) {
            throw new PrivacyException("A versao de uma politica existente nao pode ser alterada.");
        }
        String canonicalContent = validateAndCanonicalize(request);
        privacyPolicy.updateDraft(request.title.trim(), canonicalContent, request.effectiveAt);
        PrivacyPolicy savedPrivacyPolicy = persistencePort.save(privacyPolicy);
        auditPort.record("PRIVACY_POLICY_DRAFT_UPDATED", savedPrivacyPolicy);
        return new PrivacyPolicyResponseDTO(savedPrivacyPolicy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrivacyPolicyResponseDTO> findAll() {
        return persistencePort.findAllByVersionDescending().stream()
                .map(PrivacyPolicyResponseDTO::new)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PrivacyPolicyResponseDTO findById(Long id) {
        return new PrivacyPolicyResponseDTO(findPolicy(id));
    }

    @Override
    @Transactional
    public PrivacyPolicyResponseDTO publish(Long id, String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new PrivacyException("Usuario publicador nao identificado.");
        }
        return publish(id, publisherPort.findPublisherIdByEmail(authenticatedEmail));
    }

    @Transactional
    public PrivacyPolicyResponseDTO publishInitial(Long id) {
        return publish(id, publisherPort.findInitialPublisherId());
    }

    @Override
    @Transactional(readOnly = true)
    public PublicPrivacyPolicyResponseDTO findCurrentPublished() {
        return new PublicPrivacyPolicyResponseDTO(requireCurrentPolicy());
    }

    @Override
    @Transactional(readOnly = true)
    public PublishedPrivacyPolicyRecord requireCurrentPublished(Long policyId) {
        PrivacyPolicy privacyPolicy = requireCurrentPolicy();
        if (policyId == null || !policyId.equals(privacyPolicy.getId())) {
            throw new PrivacyException("A politica de privacidade informada nao e a versao vigente.");
        }
        return toPublishedRecord(privacyPolicy);
    }

    @Override
    @Transactional
    public PublishedPrivacyPolicyRecord requireCurrentPublishedForAcceptance(Long policyId) {
        PrivacyPolicy currentPrivacyPolicy = persistencePort.findCurrentPublishedForUpdate()
                .orElseThrow(() -> new PrivacyPolicyUnavailableException(
                        "A politica de privacidade vigente esta temporariamente indisponivel."
                ));
        if (policyId == null) {
            throw new PrivacyException("A politica de privacidade e obrigatoria.");
        }
        if (policyId.equals(currentPrivacyPolicy.getId())) {
            return toPublishedRecord(currentPrivacyPolicy);
        }
        if (persistencePort.findById(policyId).isEmpty()) {
            throw new PrivacyException("Politica de privacidade nao encontrada.");
        }
        throw new PrivacyPolicyConflictException(
                "A politica de privacidade foi atualizada. Leia a versao vigente antes de continuar."
        );
    }

    private PrivacyPolicyResponseDTO publish(Long id, Long publisherId) {
        PrivacyPolicy currentPrivacyPolicy = persistencePort.findCurrentPublishedForUpdate()
                .orElse(null);
        PrivacyPolicy privacyPolicy = findPolicy(id);
        JsonNode validatedDocument = validationService.validate(toRequest(privacyPolicy));
        String canonicalContent = hashService.canonicalize(validatedDocument);
        PrivacyPolicyContentHash contentHash = hashService.hash(canonicalContent);
        LocalDateTime publicationTime = LocalDateTime.now();

        if (currentPrivacyPolicy != null
                && !currentPrivacyPolicy.getId().equals(privacyPolicy.getId())) {
            currentPrivacyPolicy.supersede(publicationTime);
            PrivacyPolicy supersededPrivacyPolicy =
                    persistencePort.saveAndFlush(currentPrivacyPolicy);
            auditPort.record("PRIVACY_POLICY_SUPERSEDED", supersededPrivacyPolicy);
        }

        privacyPolicy.publish(contentHash, publisherId, publicationTime);
        PrivacyPolicy publishedPrivacyPolicy = persistencePort.saveAndFlush(privacyPolicy);
        auditPort.record("PRIVACY_POLICY_PUBLISHED", publishedPrivacyPolicy);
        return new PrivacyPolicyResponseDTO(publishedPrivacyPolicy);
    }

    private String validateAndCanonicalize(PrivacyPolicyRequestDTO request) {
        return hashService.canonicalize(validationService.validate(request));
    }

    private PrivacyPolicyRequestDTO toRequest(PrivacyPolicy privacyPolicy) {
        PrivacyPolicyRequestDTO request = new PrivacyPolicyRequestDTO();
        request.version = privacyPolicy.getVersion();
        request.title = privacyPolicy.getTitle();
        request.content = privacyPolicy.getContent();
        request.effectiveAt = privacyPolicy.getEffectiveAt();
        return request;
    }

    private PrivacyPolicy findPolicy(Long id) {
        if (id == null) {
            throw new PrivacyException("Politica de privacidade nao encontrada.");
        }
        return persistencePort.findById(id)
                .orElseThrow(() -> new PrivacyException("Politica de privacidade nao encontrada."));
    }

    private PrivacyPolicy requireCurrentPolicy() {
        return persistencePort.findCurrentPublished()
                .orElseThrow(() -> new PrivacyPolicyUnavailableException(
                        "A politica de privacidade vigente esta temporariamente indisponivel."
                ));
    }

    private PublishedPrivacyPolicyRecord toPublishedRecord(PrivacyPolicy privacyPolicy) {
        return new PublishedPrivacyPolicyRecord(
                privacyPolicy.getId(),
                privacyPolicy.getVersion(),
                privacyPolicy.getContentHash().value(),
                privacyPolicy.getEffectiveAt()
        );
    }
}
