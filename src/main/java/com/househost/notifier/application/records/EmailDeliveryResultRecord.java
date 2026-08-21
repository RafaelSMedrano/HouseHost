package com.househost.notifier.application.records;

import com.househost.notifier.domain.exception.NotificationDomainException;
import com.househost.notifier.domain.model.EmailDeliveryOutcome;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import com.househost.notifier.domain.model.NotificationIntent;

public record EmailDeliveryResultRecord(
        EmailDeliveryOutcome outcome,
        String providerMessageId,
        NotificationFailureCategory failureCategory
) {

    public EmailDeliveryResultRecord {
        outcome = NotificationRecordValidation.requireValue(
                outcome,
                "Resultado da entrega e obrigatorio."
        );
        providerMessageId = NotificationRecordValidation.optionalText(
                providerMessageId,
                NotificationIntent.MAX_PROVIDER_MESSAGE_ID_LENGTH,
                "Identificador da mensagem no provedor"
        );
        if (outcome == EmailDeliveryOutcome.ACCEPTED) {
            if (providerMessageId == null || failureCategory != null) {
                throw new NotificationDomainException(
                        "Aceite exige identificador do provedor e nao admite categoria de falha."
                );
            }
        } else if (providerMessageId != null || failureCategory == null) {
            throw new NotificationDomainException(
                    "Falha exige categoria e nao admite identificador de mensagem aceita."
            );
        }
    }

    public static EmailDeliveryResultRecord accepted(String providerMessageId) {
        return new EmailDeliveryResultRecord(
                EmailDeliveryOutcome.ACCEPTED,
                providerMessageId,
                null
        );
    }

    public static EmailDeliveryResultRecord retryableFailure(
            NotificationFailureCategory failureCategory
    ) {
        return new EmailDeliveryResultRecord(
                EmailDeliveryOutcome.RETRYABLE_FAILURE,
                null,
                failureCategory
        );
    }

    public static EmailDeliveryResultRecord permanentFailure(
            NotificationFailureCategory failureCategory
    ) {
        return new EmailDeliveryResultRecord(
                EmailDeliveryOutcome.PERMANENT_FAILURE,
                null,
                failureCategory
        );
    }
}
