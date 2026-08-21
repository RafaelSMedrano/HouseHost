package com.househost.notifier.application.records;

import com.househost.notifier.domain.model.NotificationIntent;

import java.time.Instant;
import java.util.UUID;

public record NotificationClaimRecord(
        UUID notificationIntentId,
        String sourceSystem,
        String notificationType,
        String deliveryProfileKey,
        EmailMessageRecord emailMessageRecord,
        int attemptCount,
        Instant leaseUntil
) {

    public NotificationClaimRecord {
        notificationIntentId = NotificationRecordValidation.requireValue(
                notificationIntentId,
                "Identificador da intencao e obrigatorio."
        );
        sourceSystem = NotificationRecordValidation.requireSymbolicKey(
                sourceSystem,
                NotificationIntent.MAX_SOURCE_SYSTEM_LENGTH,
                "Sistema de origem"
        );
        notificationType = NotificationRecordValidation.requireSymbolicKey(
                notificationType,
                NotificationIntent.MAX_NOTIFICATION_TYPE_LENGTH,
                "Tipo da notificacao"
        );
        deliveryProfileKey = NotificationRecordValidation.requireSymbolicKey(
                deliveryProfileKey,
                NotificationIntent.MAX_DELIVERY_PROFILE_KEY_LENGTH,
                "Perfil de entrega"
        );
        emailMessageRecord = NotificationRecordValidation.requireValue(
                emailMessageRecord,
                "Mensagem reivindicada e obrigatoria."
        );
        attemptCount = NotificationRecordValidation.requirePositive(
                attemptCount,
                "Quantidade de tentativas"
        );
        leaseUntil = NotificationRecordValidation.requireValue(
                leaseUntil,
                "Expiracao da concessao e obrigatoria."
        );
    }
}
