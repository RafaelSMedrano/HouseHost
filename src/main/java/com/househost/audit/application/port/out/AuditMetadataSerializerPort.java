package com.househost.audit.application.port.out;

import java.util.Map;

public interface AuditMetadataSerializerPort {
    String serialize(Map<String, Object> metadata);
}
