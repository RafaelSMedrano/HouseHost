package com.househost.audit.domain.model;

public record AuditActor(String actorType, Long actorId, String actorLabel, AuditEventContext context) {
}
