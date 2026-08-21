package com.househost.notifier.application.records;

import com.househost.notifier.domain.exception.NotificationDomainException;

import java.util.regex.Pattern;

final class NotificationRecordValidation {

    private static final Pattern SYMBOLIC_KEY_PATTERN = Pattern.compile(
            "[A-Za-z][A-Za-z0-9_]*(?:[.-][A-Za-z0-9_]+)*"
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
    );

    private NotificationRecordValidation() {
    }

    static String requireText(String value, int maximumLength, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new NotificationDomainException(fieldName + " e obrigatorio.");
        }
        String normalizedValue = value.trim();
        if (normalizedValue.length() > maximumLength) {
            throw new NotificationDomainException(
                    fieldName + " excede o limite de " + maximumLength + " caracteres."
            );
        }
        return normalizedValue;
    }

    static String optionalText(String value, int maximumLength, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return requireText(value, maximumLength, fieldName);
    }

    static String requireContent(String value, int maximumLength, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new NotificationDomainException(fieldName + " e obrigatorio.");
        }
        if (value.length() > maximumLength) {
            throw new NotificationDomainException(
                    fieldName + " excede o limite de " + maximumLength + " caracteres."
            );
        }
        return value;
    }

    static String requireSymbolicKey(String value, int maximumLength, String fieldName) {
        String normalizedValue = requireText(value, maximumLength, fieldName);
        if (!SYMBOLIC_KEY_PATTERN.matcher(normalizedValue).matches()) {
            throw new NotificationDomainException(
                    fieldName + " deve usar identificador textual estavel."
            );
        }
        return normalizedValue;
    }

    static String requireEmail(String value, int maximumLength) {
        String normalizedValue = requireText(value, maximumLength, "Destinatario");
        if (!EMAIL_PATTERN.matcher(normalizedValue).matches()) {
            throw new NotificationDomainException("Destinatario deve ser um email valido.");
        }
        return normalizedValue;
    }

    static String requireHeader(String value, int maximumLength, String fieldName) {
        String normalizedValue = requireText(value, maximumLength, fieldName);
        if (normalizedValue.contains("\r") || normalizedValue.contains("\n")) {
            throw new NotificationDomainException(
                    fieldName + " nao pode conter quebra de linha."
            );
        }
        return normalizedValue;
    }

    static <T> T requireValue(T value, String message) {
        if (value == null) {
            throw new NotificationDomainException(message);
        }
        return value;
    }

    static int requirePositive(int value, String fieldName) {
        if (value < 1) {
            throw new NotificationDomainException(fieldName + " deve ser positiva.");
        }
        return value;
    }
}
