package com.househost.notifier.application.records;

import com.househost.notifier.domain.model.NotificationEventType;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import com.househost.notifier.domain.model.NotificationProviderEvent;

import java.time.Instant;

public record NotificationFeedbackRecord(
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
        String rawEventStorageKey
) {

    public NotificationFeedbackRecord {
        transportEventId = NotificationRecordValidation.requireText(
                transportEventId,
                NotificationProviderEvent.MAX_TRANSPORT_EVENT_ID_LENGTH,
                "Identificador do evento de transporte"
        );
        providerEventId = NotificationRecordValidation.optionalText(
                providerEventId,
                NotificationProviderEvent.MAX_PROVIDER_EVENT_ID_LENGTH,
                "Identificador do evento no provedor"
        );
        providerMessageId = NotificationRecordValidation.requireText(
                providerMessageId,
                NotificationProviderEvent.MAX_PROVIDER_MESSAGE_ID_LENGTH,
                "Identificador da mensagem no provedor"
        );
        eventType = NotificationRecordValidation.requireValue(
                eventType,
                "Tipo do evento e obrigatorio."
        );
        bounceType = NotificationRecordValidation.optionalText(
                bounceType,
                NotificationProviderEvent.MAX_BOUNCE_CLASSIFICATION_LENGTH,
                "Tipo do bounce"
        );
        bounceSubType = NotificationRecordValidation.optionalText(
                bounceSubType,
                NotificationProviderEvent.MAX_BOUNCE_CLASSIFICATION_LENGTH,
                "Subtipo do bounce"
        );
        providerStatusCode = NotificationRecordValidation.optionalText(
                providerStatusCode,
                NotificationProviderEvent.MAX_PROVIDER_STATUS_CODE_LENGTH,
                "Status do provedor"
        );
        occurredAt = NotificationRecordValidation.requireValue(
                occurredAt,
                "Data do evento e obrigatoria."
        );
        receivedAt = NotificationRecordValidation.requireValue(
                receivedAt,
                "Data de recebimento e obrigatoria."
        );
        rawEventStorageKey = NotificationRecordValidation.optionalText(
                rawEventStorageKey,
                NotificationProviderEvent.MAX_RAW_EVENT_STORAGE_KEY_LENGTH,
                "Referencia do evento bruto"
        );
    }
}
