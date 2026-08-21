package com.househost.observability.application.service;

import com.househost.observability.application.dto.ClientLogRequestDTO;
import com.househost.observability.application.records.ClientLogRequestContextRecord;
import com.househost.observability.application.records.SanitizedClientLogRecord;
import com.househost.observability.domain.exception.ClientLogRejectedException;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ClientLogValidationService {

    private static final Pattern CONTROL_PATTERN = Pattern.compile("[\\p{Cntrl}\\p{Cf}]+");
    private static final Pattern URL_QUERY_PATTERN = Pattern.compile("([a-zA-Z][a-zA-Z0-9+.-]*://[^\\s?]+)\\?[^\\s]*");
    private static final Pattern SENSITIVE_VALUE_PATTERN = Pattern.compile(
            "(?i)\\b(password|passwd|senha|token|authorization|cookie|session|document(?:number)?|cpf|email|phone|telefone|creditcard|card)\\b\\s*[:=]\\s*[^\\s,;]+"
    );
    private static final Pattern JWT_PATTERN = Pattern.compile("\\beyJ[A-Za-z0-9_-]{8,}\\.[A-Za-z0-9_-]{8,}(?:\\.[A-Za-z0-9_-]{8,})?\\b");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)(?:\\+?55\\s*)?(?:\\(?\\d{2}\\)?[ .-]*)?9?\\d{4}[ .-]*\\d{4}(?!\\d)");
    private static final Pattern DOCUMENT_PATTERN = Pattern.compile("(?<!\\d)\\d{3}[.-]?\\d{3}[.-]?\\d{3}-?\\d{2}(?!\\d)");

    private final CorrelationIdService correlationIdService;

    public ClientLogValidationService(CorrelationIdService correlationIdService) {
        this.correlationIdService = correlationIdService;
    }

    public SanitizedClientLogRecord sanitize(
            ClientLogRequestDTO request,
            ClientLogRequestContextRecord contextRecord
    ) {
        if (request.getCorrelationId() != null && !correlationIdService.isValid(request.getCorrelationId())) {
            throw new ClientLogRejectedException();
        }

        String sanitizedMessage = sanitizeFreeText(request.getMessage(), 1000);
        if (sanitizedMessage.isBlank()) {
            throw new ClientLogRejectedException();
        }

        return new SanitizedClientLogRecord(
                request.getLevel(),
                sanitizeScalar(request.getEvent(), 80),
                sanitizedMessage,
                request.getCorrelationId(),
                sanitizeRoute(request.getRoute()),
                sanitizeScalar(request.getMethod(), 10),
                request.getStatus(),
                request.getDurationMs(),
                sanitizeFreeText(request.getStack(), 8000),
                request.getClientTimestamp(),
                contextRecord.actorReference(),
                contextRecord.originReference(),
                correlationIdService.isValid(contextRecord.requestCorrelationId())
                        ? contextRecord.requestCorrelationId()
                        : null,
                contextRecord.receivedAt()
        );
    }

    private String sanitizeRoute(String route) {
        if (route == null) {
            return null;
        }
        int queryIndex = route.indexOf('?');
        String path = queryIndex >= 0 ? route.substring(0, queryIndex) : route;
        return sanitizeScalar(path, 512);
    }

    private String sanitizeFreeText(String value, int maximumLength) {
        if (value == null) {
            return null;
        }
        String sanitized = CONTROL_PATTERN.matcher(value).replaceAll(" ");
        sanitized = URL_QUERY_PATTERN.matcher(sanitized).replaceAll("$1?[REDACTED]");
        sanitized = SENSITIVE_VALUE_PATTERN.matcher(sanitized).replaceAll("$1=[REDACTED]");
        sanitized = JWT_PATTERN.matcher(sanitized).replaceAll("[REDACTED_JWT]");
        sanitized = EMAIL_PATTERN.matcher(sanitized).replaceAll("[REDACTED_EMAIL]");
        sanitized = PHONE_PATTERN.matcher(sanitized).replaceAll("[REDACTED_PHONE]");
        sanitized = DOCUMENT_PATTERN.matcher(sanitized).replaceAll("[REDACTED_DOCUMENT]");
        sanitized = sanitized.strip();
        return truncate(sanitized, maximumLength);
    }

    private String sanitizeScalar(String value, int maximumLength) {
        if (value == null) {
            return null;
        }
        return truncate(CONTROL_PATTERN.matcher(value).replaceAll("").strip(), maximumLength);
    }

    private String truncate(String value, int maximumLength) {
        return value.length() <= maximumLength ? value : value.substring(0, maximumLength);
    }
}
