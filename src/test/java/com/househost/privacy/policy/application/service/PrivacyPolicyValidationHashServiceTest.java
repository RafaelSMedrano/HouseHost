package com.househost.privacy.policy.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.househost.privacy.policy.application.dto.PrivacyPolicyRequestDTO;
import com.househost.shared.exception.PrivacyException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrivacyPolicyValidationHashServiceTest {
    private PrivacyPolicyValidationService validationService;
    private PrivacyPolicyHashService hashService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        validationService = new PrivacyPolicyValidationService(objectMapper);
        hashService = new PrivacyPolicyHashService(objectMapper);
    }

    @Test
    void canonicalizesEquivalentObjectsAndBuildsDeterministicSha256() {
        PrivacyPolicyRequestDTO first = request("""
                {"sections":[{"nodes":[{"text":"Texto","type":"paragraph"}],"heading":"Inicio"}],"schemaVersion":1}
                """);
        PrivacyPolicyRequestDTO second = request("""
                {"schemaVersion":1,"sections":[{"heading":"Inicio","nodes":[{"type":"paragraph","text":"Texto"}]}]}
                """);

        String firstCanonical = hashService.canonicalize(validationService.validate(first));
        String secondCanonical = hashService.canonicalize(validationService.validate(second));

        assertEquals(firstCanonical, secondCanonical);
        assertEquals(hashService.hash(firstCanonical), hashService.hash(secondCanonical));
        assertTrue(hashService.hash(firstCanonical).value().matches("sha256:[0-9a-f]{64}"));
    }

    @Test
    void rejectsExecutableMarkupUnsupportedNodesAndUnsafeLinks() {
        assertThrows(PrivacyException.class, () -> validationService.validate(request("""
                {"schemaVersion":1,"sections":[{"heading":"X","nodes":[{"type":"paragraph","text":"<script>alert(1)</script>"}]}]}
                """)));
        assertThrows(PrivacyException.class, () -> validationService.validate(request("""
                {"schemaVersion":1,"sections":[{"heading":"X","nodes":[{"type":"html","text":"X"}]}]}
                """)));
        assertThrows(PrivacyException.class, () -> validationService.validate(request("""
                {"schemaVersion":1,"sections":[{"heading":"X","nodes":[{"type":"link","text":"X","url":"javascript:alert(1)"}]}]}
                """)));
        assertThrows(PrivacyException.class, () -> validationService.validate(request("""
                {"schemaVersion":1,"sections":[{"heading":"X","nodes":[{"type":"link","text":"X","url":"https:relative"}]}]}
                """)));
    }

    @Test
    void rejectsUnknownFieldsInsteadOfSilentlyRenderingThem() {
        assertThrows(PrivacyException.class, () -> validationService.validate(request("""
                {"schemaVersion":1,"sections":[],"script":"alert(1)"}
                """)));
    }

    private PrivacyPolicyRequestDTO request(String content) {
        PrivacyPolicyRequestDTO request = new PrivacyPolicyRequestDTO();
        request.version = 2;
        request.title = "Politica de Privacidade";
        request.content = content;
        request.effectiveAt = LocalDateTime.of(2026, 7, 26, 0, 0);
        return request;
    }
}
