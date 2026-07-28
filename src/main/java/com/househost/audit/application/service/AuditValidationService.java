package com.househost.audit.application.service;

import org.springframework.stereotype.Service;

@Service
public class AuditValidationService {
    public String required(String value, String fieldName) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(fieldName + " e obrigatorio para auditoria.");
        return value.trim();
    }
    public String truncate(String value, int maxLength) {
        return value == null || value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
