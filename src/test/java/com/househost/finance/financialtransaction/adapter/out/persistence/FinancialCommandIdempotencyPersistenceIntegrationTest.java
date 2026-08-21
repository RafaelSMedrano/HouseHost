package com.househost.finance.financialtransaction.adapter.out.persistence;

import com.househost.finance.financialtransaction.application.records.FinancialCommandIdempotencyRecord;
import com.househost.finance.financialtransaction.application.records.FinancialCommandOperation;
import com.househost.finance.financialtransaction.application.records.FinancialCommandStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class FinancialCommandIdempotencyPersistenceIntegrationTest {

    @Autowired
    private FinancialCommandIdempotencyJpaRepository financialCommandIdempotencyJpaRepository;

    @Test
    void persistsAndReloadsCompletedOutcome() {
        FinancialCommandIdempotencyPersistenceAdapter financialCommandIdempotencyPersistenceAdapter =
                new FinancialCommandIdempotencyPersistenceAdapter(
                        financialCommandIdempotencyJpaRepository
                );
        FinancialCommandIdempotencyRecord savedFinancialCommandIdempotencyRecord =
                financialCommandIdempotencyPersistenceAdapter.save(inProgressRecord());
        financialCommandIdempotencyPersistenceAdapter.save(
                savedFinancialCommandIdempotencyRecord.complete(40L, 50L)
        );

        FinancialCommandIdempotencyRecord financialCommandIdempotencyRecord =
                financialCommandIdempotencyPersistenceAdapter.find(
                                FinancialCommandOperation.RESERVATION_PLAN_CREATION,
                                "operator@example.invalid",
                                "reservation-command"
                        )
                        .orElseThrow();

        assertEquals(FinancialCommandStatus.COMPLETED, financialCommandIdempotencyRecord.status());
        assertEquals(40L, financialCommandIdempotencyRecord.bookingId());
        assertEquals(50L, financialCommandIdempotencyRecord.planId());
        assertTrue(financialCommandIdempotencyRecord.completedAt() != null);
    }

    @Test
    void uniqueScopePreventsConcurrentDuplicateCommandIdentity() {
        FinancialCommandIdempotencyPersistenceAdapter financialCommandIdempotencyPersistenceAdapter =
                new FinancialCommandIdempotencyPersistenceAdapter(
                        financialCommandIdempotencyJpaRepository
                );
        financialCommandIdempotencyPersistenceAdapter.save(inProgressRecord());

        assertThrows(
                DataIntegrityViolationException.class,
                () -> financialCommandIdempotencyPersistenceAdapter.save(inProgressRecord())
        );
    }

    @Test
    void persistsDefinitiveTransactionOutcomeForReplacementReplay() {
        FinancialCommandIdempotencyPersistenceAdapter financialCommandIdempotencyPersistenceAdapter =
                new FinancialCommandIdempotencyPersistenceAdapter(
                        financialCommandIdempotencyJpaRepository
                );
        FinancialCommandIdempotencyRecord replacementFinancialCommandIdempotencyRecord =
                new FinancialCommandIdempotencyRecord(
                        null,
                        FinancialCommandOperation.PAYMENT_REPLACEMENT,
                        "operator@example.invalid",
                        "replacement-command",
                        FinancialCommandStatus.IN_PROGRESS,
                        null,
                        50L,
                        null,
                        LocalDateTime.now(),
                        null
                );
        FinancialCommandIdempotencyRecord savedFinancialCommandIdempotencyRecord =
                financialCommandIdempotencyPersistenceAdapter.save(
                        replacementFinancialCommandIdempotencyRecord
                );

        financialCommandIdempotencyPersistenceAdapter.save(
                savedFinancialCommandIdempotencyRecord.completeReplacement(50L, 201L)
        );

        FinancialCommandIdempotencyRecord completedFinancialCommandIdempotencyRecord =
                financialCommandIdempotencyPersistenceAdapter.find(
                                FinancialCommandOperation.PAYMENT_REPLACEMENT,
                                "operator@example.invalid",
                                "replacement-command"
                        )
                        .orElseThrow();
        assertEquals(50L, completedFinancialCommandIdempotencyRecord.planId());
        assertEquals(
                201L,
                completedFinancialCommandIdempotencyRecord.financialTransactionId()
        );
    }

    private FinancialCommandIdempotencyRecord inProgressRecord() {
        return new FinancialCommandIdempotencyRecord(
                null,
                FinancialCommandOperation.RESERVATION_PLAN_CREATION,
                "operator@example.invalid",
                "reservation-command",
                FinancialCommandStatus.IN_PROGRESS,
                null,
                null,
                null,
                LocalDateTime.now(),
                null
        );
    }
}
