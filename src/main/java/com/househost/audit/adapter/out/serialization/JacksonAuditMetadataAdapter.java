package com.househost.audit.adapter.out.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.househost.audit.application.port.out.AuditMetadataSerializerPort;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class JacksonAuditMetadataAdapter implements AuditMetadataSerializerPort {
    private final ObjectMapper objectMapper;
    public JacksonAuditMetadataAdapter(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }
    public String serialize(Map<String, Object> metadata) {
        try { return objectMapper.writeValueAsString(metadata); }
        catch (JsonProcessingException exception) { return "{\"serializationError\":true}"; }
    }
}
