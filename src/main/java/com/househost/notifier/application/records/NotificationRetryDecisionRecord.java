package com.househost.notifier.application.records;

import com.househost.notifier.domain.exception.NotificationDomainException;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import com.househost.notifier.domain.model.NotificationStatus;

import java.time.Instant;

public record NotificationRetryDecisionRecord(
        NotificationStatus targetStatus,
        Instant nextAttemptAt,
        NotificationFailureCategory failureCategory
) {

    public NotificationRetryDecisionRecord {
        targetStatus = NotificationRecordValidation.requireValue(
                targetStatus,
                "Status de destino e obrigatorio."
        );
        failureCategory = NotificationRecordValidation.requireValue(
                failureCategory,
                "Categoria da falha e obrigatoria."
        );
        if (targetStatus == NotificationStatus.RETRYABLE_FAILURE) {
            nextAttemptAt = NotificationRecordValidation.requireValue(
                    nextAttemptAt,
                    "Proxima tentativa e obrigatoria."
            );
        } else if (targetStatus != NotificationStatus.EXHAUSTED || nextAttemptAt != null) {
            throw new NotificationDomainException(
                    "Decisao de retry deve resultar em nova tentativa ou esgotamento."
            );
        }
    }

    public static NotificationRetryDecisionRecord retryAt(
            Instant nextAttemptAt,
            NotificationFailureCategory failureCategory
    ) {
        return new NotificationRetryDecisionRecord(
                NotificationStatus.RETRYABLE_FAILURE,
                nextAttemptAt,
                failureCategory
        );
    }

    public static NotificationRetryDecisionRecord exhausted(
            NotificationFailureCategory failureCategory
    ) {
        return new NotificationRetryDecisionRecord(
                NotificationStatus.EXHAUSTED,
                null,
                failureCategory
        );
    }
}
