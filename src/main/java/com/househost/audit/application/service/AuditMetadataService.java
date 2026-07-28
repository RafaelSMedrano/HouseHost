package com.househost.audit.application.service;

import com.househost.audit.application.port.out.AuditMetadataSerializerPort;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AuditMetadataService {
    private final AuditMetadataSerializerPort serializerPort;
    public AuditMetadataService(AuditMetadataSerializerPort serializerPort) { this.serializerPort = serializerPort; }
    public String serialize(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return "{}";
        return serializerPort.serialize(metadata);
    }
}
