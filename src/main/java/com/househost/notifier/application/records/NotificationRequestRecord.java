package com.househost.notifier.application.records;

import com.househost.notifier.domain.model.NotificationChannel;
import com.househost.notifier.domain.model.NotificationIntent;

public record NotificationRequestRecord(
        String sourceSystem,
        String externalEventId,
        String idempotencyKey,
        String correlationKey,
        String notificationType,
        NotificationChannel channel,
        String deliveryProfileKey,
        EmailMessageRecord emailMessageRecord
) {

    public NotificationRequestRecord {
        sourceSystem = NotificationRecordValidation.requireSymbolicKey(
                sourceSystem,
                NotificationIntent.MAX_SOURCE_SYSTEM_LENGTH,
                "Sistema de origem"
        );
        externalEventId = NotificationRecordValidation.requireText(
                externalEventId,
                NotificationIntent.MAX_EXTERNAL_EVENT_ID_LENGTH,
                "Identificador externo do evento"
        );
        idempotencyKey = NotificationRecordValidation.requireText(
                idempotencyKey,
                NotificationIntent.MAX_IDEMPOTENCY_KEY_LENGTH,
                "Chave de idempotencia"
        );
        correlationKey = NotificationRecordValidation.optionalText(
                correlationKey,
                NotificationIntent.MAX_CORRELATION_KEY_LENGTH,
                "Chave de correlacao"
        );
        notificationType = NotificationRecordValidation.requireSymbolicKey(
                notificationType,
                NotificationIntent.MAX_NOTIFICATION_TYPE_LENGTH,
                "Tipo da notificacao"
        );
        channel = NotificationRecordValidation.requireValue(
                channel,
                "Canal da notificacao e obrigatorio."
        );
        deliveryProfileKey = NotificationRecordValidation.requireSymbolicKey(
                deliveryProfileKey,
                NotificationIntent.MAX_DELIVERY_PROFILE_KEY_LENGTH,
                "Perfil de entrega"
        );
        emailMessageRecord = NotificationRecordValidation.requireValue(
                emailMessageRecord,
                "Mensagem de email e obrigatoria."
        );
    }
}
