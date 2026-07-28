package com.househost.privacy.policy.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.househost.privacy.policy.application.dto.PrivacyPolicyRequestDTO;
import com.househost.shared.exception.PrivacyException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class PrivacyPolicyValidationService {
    private static final int TITLE_LIMIT = 180;
    private static final int CONTENT_LIMIT = 100_000;
    private static final int TEXT_LIMIT = 8_000;
    private static final Set<String> ROOT_FIELDS = Set.of("schemaVersion", "sections");
    private static final Set<String> SECTION_FIELDS = Set.of("heading", "nodes");
    private static final Set<String> PARAGRAPH_FIELDS = Set.of("type", "text");
    private static final Set<String> LIST_FIELDS = Set.of("type", "items");
    private static final Set<String> LINK_FIELDS = Set.of("type", "text", "url");

    private final ObjectMapper objectMapper;

    public PrivacyPolicyValidationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode validate(PrivacyPolicyRequestDTO request) {
        if (request == null) {
            throw new PrivacyException("Os dados da politica de privacidade sao obrigatorios.");
        }
        if (request.version == null || request.version <= 0) {
            throw new PrivacyException("A versao da politica deve ser um numero positivo.");
        }
        requiredText(request.title, "O titulo", TITLE_LIMIT);
        if (request.effectiveAt == null) {
            throw new PrivacyException("A vigencia da politica e obrigatoria.");
        }
        requiredText(request.content, "O conteudo", CONTENT_LIMIT);
        return validateDocument(request.content);
    }

    private JsonNode validateDocument(String content) {
        try {
            JsonNode document = objectMapper.readTree(content);
            requireObject(document, "O documento da politica");
            requireOnlyFields(document, ROOT_FIELDS);
            if (document.path("schemaVersion").asInt(-1) != 1) {
                throw new PrivacyException("Use schemaVersion 1 para o documento da politica.");
            }
            JsonNode sectionList = document.get("sections");
            if (sectionList == null || !sectionList.isArray() || sectionList.isEmpty()) {
                throw new PrivacyException("A politica deve possuir secoes.");
            }
            sectionList.forEach(this::validateSection);
            return document;
        } catch (JsonProcessingException exception) {
            throw new PrivacyException("O conteudo da politica deve ser um JSON estruturado valido.");
        }
    }

    private void validateSection(JsonNode section) {
        requireObject(section, "Cada secao");
        requireOnlyFields(section, SECTION_FIELDS);
        requiredText(section.path("heading").asText(null), "O titulo da secao", TITLE_LIMIT);
        JsonNode nodeList = section.get("nodes");
        if (nodeList == null || !nodeList.isArray() || nodeList.isEmpty()) {
            throw new PrivacyException("Cada secao deve possuir conteudo.");
        }
        nodeList.forEach(this::validateNode);
    }

    private void validateNode(JsonNode node) {
        requireObject(node, "Cada bloco da politica");
        String type = node.path("type").asText();
        switch (type) {
            case "paragraph" -> validateParagraph(node);
            case "list" -> validateList(node);
            case "link" -> validateLink(node);
            default -> throw new PrivacyException("Tipo de bloco nao suportado na politica: " + type);
        }
    }

    private void validateParagraph(JsonNode node) {
        requireOnlyFields(node, PARAGRAPH_FIELDS);
        requiredSafeText(node.path("text").asText(null), "O paragrafo");
    }

    private void validateList(JsonNode node) {
        requireOnlyFields(node, LIST_FIELDS);
        JsonNode itemList = node.get("items");
        if (itemList == null || !itemList.isArray() || itemList.isEmpty()) {
            throw new PrivacyException("Uma lista da politica deve possuir itens.");
        }
        itemList.forEach(item -> requiredSafeText(item.asText(null), "O item da lista"));
    }

    private void validateLink(JsonNode node) {
        requireOnlyFields(node, LINK_FIELDS);
        requiredSafeText(node.path("text").asText(null), "O texto do link");
        String url = node.path("url").asText(null);
        requiredText(url, "A URL do link", 2_000);
        try {
            URI uri = new URI(url);
            if (!("https".equalsIgnoreCase(uri.getScheme())
                    || "http".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null) {
                throw new PrivacyException("A politica aceita apenas links HTTP ou HTTPS.");
            }
        } catch (URISyntaxException exception) {
            throw new PrivacyException("A URL da politica e invalida.");
        }
    }

    private void requiredSafeText(String value, String label) {
        requiredText(value, label, TEXT_LIMIT);
        String normalized = value.toLowerCase();
        if (value.contains("<") || value.contains(">") || normalized.contains("javascript:")) {
            throw new PrivacyException(label + " contem markup ou codigo nao permitido.");
        }
    }

    private void requiredText(String value, String label, int limit) {
        if (value == null || value.isBlank()) {
            throw new PrivacyException(label + " e obrigatorio.");
        }
        if (value.length() > limit) {
            throw new PrivacyException(label + " excede o limite de " + limit + " caracteres.");
        }
    }

    private void requireObject(JsonNode node, String label) {
        if (node == null || !node.isObject()) {
            throw new PrivacyException(label + " deve ser um objeto estruturado.");
        }
    }

    private void requireOnlyFields(JsonNode node, Set<String> allowedFieldSet) {
        node.fieldNames().forEachRemaining(field -> {
            if (!allowedFieldSet.contains(field)) {
                throw new PrivacyException("Campo nao suportado no documento da politica: " + field);
            }
        });
    }
}
