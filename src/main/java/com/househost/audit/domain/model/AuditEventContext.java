package com.househost.audit.domain.model;

public record AuditEventContext(String ipAddress, String userAgent) {
}
