package com.househost.privacy.policy.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.househost.privacy.policy.domain.model.PrivacyPolicyContentHash;
import com.househost.shared.exception.PrivacyException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;
import org.springframework.stereotype.Service;

@Service
public class PrivacyPolicyHashService {
    private final ObjectMapper objectMapper;

    public PrivacyPolicyHashService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String canonicalize(JsonNode document) {
        try {
            return objectMapper.writeValueAsString(sort(document));
        } catch (JsonProcessingException exception) {
            throw new PrivacyException("Nao foi possivel canonicalizar a politica de privacidade.");
        }
    }

    public PrivacyPolicyContentHash hash(String canonicalContent) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonicalContent.getBytes(StandardCharsets.UTF_8));
            return new PrivacyPolicyContentHash("sha256:" + toHex(digest));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponivel.", exception);
        }
    }

    private JsonNode sort(JsonNode node) {
        if (node.isObject()) {
            ObjectNode sortedObject = objectMapper.createObjectNode();
            TreeMap<String, JsonNode> fieldMap = new TreeMap<>();
            node.fields().forEachRemaining(entry -> fieldMap.put(entry.getKey(), entry.getValue()));
            fieldMap.forEach((field, value) -> sortedObject.set(field, sort(value)));
            return sortedObject;
        }
        if (node.isArray()) {
            ArrayNode sortedArray = objectMapper.createArrayNode();
            node.forEach(value -> sortedArray.add(sort(value)));
            return sortedArray;
        }
        return node;
    }

    private String toHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            hex.append(Character.forDigit((value >>> 4) & 0x0f, 16));
            hex.append(Character.forDigit(value & 0x0f, 16));
        }
        return hex.toString();
    }
}
