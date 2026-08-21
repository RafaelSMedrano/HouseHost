package com.househost.notifier.domain.model;

import com.househost.notifier.domain.exception.NotificationDomainException;

import java.time.Instant;
import java.util.UUID;

public final class NotificationProviderEvent {

    public static final int MAX_TRANSPORT_EVENT_ID_LENGTH = 255;
    public static final int MAX_PROVIDER_EVENT_ID_LENGTH = 255;
    public static final int MAX_PROVIDER_MESSAGE_ID_LENGTH = 255;
    public static final int MAX_BOUNCE_CLASSIFICATION_LENGTH = 100;
    public static final int MAX_PROVIDER_STATUS_CODE_LENGTH = 100;
    public static final int MAX_RAW_EVENT_STORAGE_KEY_LENGTH = 512;

    private final UUID id;
    private final UUID notificationIntentId;
    private final String transportEventId;
    private final String providerEventId;
    private final String providerMessageId;
    private final NotificationEventType eventType;
    private final String bounceType;
    private final String bounceSubType;
    private final String providerStatusCode;
    private final NotificationFailureCategory failureCategory;
    private final Instant occurredAt;
    private final Instant receivedAt;
    private final Instant processedAt;
    private final String rawEventStorageKey;

    public NotificationProviderEvent(
            UUID id,
            UUID notificationIntentId,
            String transportEventId,
            String providerEventId,
            String providerMessageId,
            NotificationEventType eventType,
            String bounceType,
            String bounceSubType,
            String providerStatusCode,
            NotificationFailureCategory failureCategory,
            Instant occurredAt,
            Instant receivedAt,
            Instant processedAt,
            String rawEventStorageKey
    ) {
        this.id = requireValue(id, "Identificador do evento e obrigatorio.");
        this.notificationIntentId = requireValue(
                notificationIntentId,
                "Identificador da intencao e obrigatorio."
        );
        this.transportEventId = requireText(
                transportEventId,
                MAX_TRANSPORT_EVENT_ID_LENGTH,
                "Identificador do evento de transporte"
        );
        this.providerEventId = optionalText(
                providerEventId,
                MAX_PROVIDER_EVENT_ID_LENGTH,
                "Identificador do evento no provedor"
        );
        this.providerMessageId = requireText(
                providerMessageId,
                MAX_PROVIDER_MESSAGE_ID_LENGTH,
                "Identificador da mensagem no provedor"
        );
        this.eventType = requireValue(eventType, "Tipo do evento e obrigatorio.");
        this.bounceType = optionalText(
                bounceType,
                MAX_BOUNCE_CLASSIFICATION_LENGTH,
                "Tipo do bounce"
        );
        this.bounceSubType = optionalText(
                bounceSubType,
                MAX_BOUNCE_CLASSIFICATION_LENGTH,
                "Subtipo do bounce"
        );
        this.providerStatusCode = optionalText(
                providerStatusCode,
                MAX_PROVIDER_STATUS_CODE_LENGTH,
                "Status do provedor"
        );
        this.failureCategory = failureCategory;
        this.occurredAt = requireValue(occurredAt, "Data do evento e obrigatoria.");
        this.receivedAt = requireValue(receivedAt, "Data de recebimento e obrigatoria.");
        this.processedAt = processedAt;
        this.rawEventStorageKey = optionalText(
                rawEventStorageKey,
                MAX_RAW_EVENT_STORAGE_KEY_LENGTH,
                "Referencia do evento bruto"
        );
        validateChronology();
    }

    public UUID getId() {
        return id;
    }

    public UUID getNotificationIntentId() {
        return notificationIntentId;
    }

    public String getTransportEventId() {
        return transportEventId;
    }

    public String getProviderEventId() {
        return providerEventId;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public NotificationEventType getEventType() {
        return eventType;
    }

    public String getBounceType() {
        return bounceType;
    }

    public String getBounceSubType() {
        return bounceSubType;
    }

    public String getProviderStatusCode() {
        return providerStatusCode;
    }

    public NotificationFailureCategory getFailureCategory() {
        return failureCategory;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public String getRawEventStorageKey() {
        return rawEventStorageKey;
    }

    private void validateChronology() {
        if (processedAt != null && processedAt.isBefore(receivedAt)) {
            throw new NotificationDomainException(
                    "Processamento do evento nao pode anteceder seu recebimento."
            );
        }
    }

    private static String requireText(String value, int maximumLength, String fieldName) {
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

    private static String optionalText(String value, int maximumLength, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return requireText(value, maximumLength, fieldName);
    }

    private static <T> T requireValue(T value, String message) {
        if (value == null) {
            throw new NotificationDomainException(message);
        }
        return value;
    }
}
