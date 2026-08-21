package com.househost.finance.financialtransaction.application.records;

import java.time.LocalDateTime;

public record FinancialCommandIdempotencyRecord(
        Long id,
        FinancialCommandOperation operation,
        String actorReference,
        String idempotencyKey,
        FinancialCommandStatus status,
        Long bookingId,
        Long planId,
        Long financialTransactionId,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {

    public FinancialCommandIdempotencyRecord complete(Long bookingId, Long planId) {
        return new FinancialCommandIdempotencyRecord(
                id,
                operation,
                actorReference,
                idempotencyKey,
                FinancialCommandStatus.COMPLETED,
                bookingId,
                planId,
                null,
                createdAt,
                LocalDateTime.now()
        );
    }

    public FinancialCommandIdempotencyRecord completeReplacement(
            Long planId,
            Long financialTransactionId
    ) {
        return new FinancialCommandIdempotencyRecord(
                id,
                operation,
                actorReference,
                idempotencyKey,
                FinancialCommandStatus.COMPLETED,
                null,
                planId,
                financialTransactionId,
                createdAt,
                LocalDateTime.now()
        );
    }
}
