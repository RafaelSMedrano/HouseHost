package com.househost.observability.application.service;

import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class CorrelationIdService {

    private static final int MAX_LENGTH = 64;
    private static final Pattern VALID_PATTERN = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._:-]{0,63}");

    public String resolve(String suppliedCorrelationId) {
        return isValid(suppliedCorrelationId) ? suppliedCorrelationId : UUID.randomUUID().toString();
    }

    public boolean isValid(String correlationId) {
        return correlationId != null
                && correlationId.length() <= MAX_LENGTH
                && VALID_PATTERN.matcher(correlationId).matches();
    }
}
