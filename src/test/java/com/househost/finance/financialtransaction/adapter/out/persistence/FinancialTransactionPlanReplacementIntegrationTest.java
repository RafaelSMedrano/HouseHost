package com.househost.finance.financialtransaction.adapter.out.persistence;

import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanReplacementOutcomeDTO;
import com.househost.finance.financialtransaction.application.port.out.FinancialCommandActorPort;
import com.househost.finance.financialtransaction.application.port.out.FinancialCommandIdempotencyPersistencePort;
import com.househost.finance.financialtransaction.application.port.out.FinancialPostCommitAuditPort;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionPlanPersistencePort;
import com.househost.finance.financialtransaction.application.records.FinancialCommandOperation;
import com.househost.finance.financialtransaction.application.records.FinancialCommandStatus;
import com.househost.finance.financialtransaction.application.records.FinancialTransactionPlanMaterializationCommandRecord;
import com.househost.finance.financialtransaction.application.records.FinancialTransactionPlanReplacementCommandRecord;
import com.househost.finance.financialtransaction.application.service.FinancialParticipantNotifier;
import com.househost.finance.financialtransaction.application.service.FinancialTransactionPlanReplacementService;
import com.househost.finance.financialtransaction.application.service.FinancialTransactionPlanValidationService;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialPaymentStructure;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionPlan;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({
        FinancialTransactionPlanPersistenceAdapter.class,
        FinancialCommandIdempotencyPersistenceAdapter.class,
        FinancialTransactionPlanReplacementService.class,
        FinancialTransactionPlanValidationService.class
})
class FinancialTransactionPlanReplacementIntegrationTest {

    @Autowired
    private FinancialTransactionPlanPersistencePort financialTransactionPlanPersistencePort;

    @Autowired
    private FinancialCommandIdempotencyPersistencePort financialCommandIdempotencyPersistencePort;

    @Autowired
    private FinancialTransactionPlanReplacementService financialTransactionPlanReplacementService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private FinancialCommandActorPort financialCommandActorPort;

    @MockBean
    private FinancialParticipantNotifier financialParticipantNotifier;

    @MockBean
    private FinancialPostCommitAuditPort financialPostCommitAuditPort;

    private Long planId;
    private Long provisionalFinancialTransactionId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("delete from cashier_entries");
        jdbcTemplate.update("delete from cashier_expenses");
        jdbcTemplate.update("delete from installment_transactions");
        jdbcTemplate.update("delete from installment_plan_transactions");
        jdbcTemplate.update("delete from financial_command_idempotency");
        jdbcTemplate.update("delete from financial_transactions");
        jdbcTemplate.update("delete from financial_transaction_plans");
        when(financialCommandActorPort.currentActorReference())
                .thenReturn("operator@example.invalid");

        FinancialTransactionPlan savedFinancialTransactionPlan =
                financialTransactionPlanPersistencePort.save(initialPlan());
        planId = savedFinancialTransactionPlan.getId();
        provisionalFinancialTransactionId = savedFinancialTransactionPlan
                .getFinancialTransactionList().stream()
                .filter(financialTransaction -> financialTransaction.getType()
                        == FinancialTransactionType.PLAN_CHECK_IN_PAYMENT)
                .findFirst()
                .orElseThrow()
                .getId();
    }

    @Test
    void rollsBackOldDeletionAndNewPersistenceWhenParticipantCreationFails() {
        doThrow(new IllegalStateException("new participant failed"))
                .when(financialParticipantNotifier)
                .notifyCreation(any());

        assertThrows(
                IllegalStateException.class,
                () -> financialTransactionPlanReplacementService.replace(command(
                        "replacement-rollback"
                ))
        );

        FinancialTransactionPlan reloadedFinancialTransactionPlan =
                financialTransactionPlanPersistencePort.findById(planId).orElseThrow();
        assertEquals(2, reloadedFinancialTransactionPlan.getFinancialTransactionCount());
        assertFalse(reloadedFinancialTransactionPlan
                .findFinancialTransactionById(provisionalFinancialTransactionId)
                .isEmpty());
        assertEquals(2, jdbcTemplate.queryForObject(
                "select count(*) from financial_transactions",
                Integer.class
        ));
        assertEquals(0, jdbcTemplate.queryForObject(
                "select count(*) from financial_command_idempotency",
                Integer.class
        ));
        verify(financialPostCommitAuditPort, never()).recordAfterCommit(any(), any(), any());
    }

    @Test
    void concurrentReplayCreatesExactlyOneDefinitiveTransaction() throws Exception {
        FinancialTransactionPlanReplacementCommandRecord
                financialTransactionPlanReplacementCommandRecord = command(
                "replacement-concurrent"
        );
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            Future<FinancialTransactionPlanReplacementOutcomeDTO> firstOutcomeDTOFuture =
                    executorService.submit(() -> financialTransactionPlanReplacementService
                            .replace(financialTransactionPlanReplacementCommandRecord));
            Future<FinancialTransactionPlanReplacementOutcomeDTO> secondOutcomeDTOFuture =
                    executorService.submit(() -> financialTransactionPlanReplacementService
                            .replace(financialTransactionPlanReplacementCommandRecord));

            FinancialTransactionPlanReplacementOutcomeDTO firstOutcomeDTO =
                    firstOutcomeDTOFuture.get();
            FinancialTransactionPlanReplacementOutcomeDTO secondOutcomeDTO =
                    secondOutcomeDTOFuture.get();

            assertEquals(
                    firstOutcomeDTO.getDefinitiveComponent().getId(),
                    secondOutcomeDTO.getDefinitiveComponent().getId()
            );
        } finally {
            executorService.shutdownNow();
        }

        FinancialTransactionPlan reloadedFinancialTransactionPlan =
                financialTransactionPlanPersistencePort.findById(planId).orElseThrow();
        assertEquals(2, reloadedFinancialTransactionPlan.getFinancialTransactionCount());
        assertFalse(reloadedFinancialTransactionPlan.containsFinancialTransaction(
                provisionalFinancialTransactionId
        ));
        assertEquals(2, jdbcTemplate.queryForObject(
                "select count(*) from financial_transactions",
                Integer.class
        ));
        assertEquals(
                FinancialCommandStatus.COMPLETED,
                financialCommandIdempotencyPersistencePort.find(
                                FinancialCommandOperation.PAYMENT_REPLACEMENT,
                                "operator@example.invalid",
                                "replacement-concurrent"
                        )
                        .orElseThrow()
                        .status()
        );
        verify(financialParticipantNotifier, times(1)).notifyParticipantDeletion(any());
        verify(financialParticipantNotifier, times(1)).notifyCreation(any());
        verify(financialPostCommitAuditPort, times(1)).recordAfterCommit(any(), any(), any());
    }

    @Test
    void bookingOwnedCheckoutMaterializationLocksSourceAndReplaysOnce() {
        Long provisionalCheckOutFinancialTransactionId =
                financialTransactionPlanPersistencePort.findById(planId)
                        .orElseThrow()
                        .getFinancialTransactionList().stream()
                        .filter(financialTransaction -> financialTransaction.getType()
                                == FinancialTransactionType.PLAN_CHECK_OUT_PAYMENT)
                        .findFirst()
                        .orElseThrow()
                        .getId();
        FinancialTransactionPlanMaterializationCommandRecord
                financialTransactionPlanMaterializationCommandRecord =
                new FinancialTransactionPlanMaterializationCommandRecord(
                        40L,
                        FinancialTransactionType.PLAN_CHECK_OUT_PAYMENT,
                        true,
                        FinancialPaymentStructure.SIMPLE,
                        FinancialTransactionMethod.PIX,
                        null,
                        "checkout-booking-owned"
                );

        FinancialTransactionPlanReplacementOutcomeDTO firstOutcomeDTO =
                financialTransactionPlanReplacementService.materializeForBooking(
                                financialTransactionPlanMaterializationCommandRecord
                        )
                        .orElseThrow();
        FinancialTransactionPlanReplacementOutcomeDTO replayOutcomeDTO =
                financialTransactionPlanReplacementService.materializeForBooking(
                                financialTransactionPlanMaterializationCommandRecord
                        )
                        .orElseThrow();

        assertEquals(
                firstOutcomeDTO.getDefinitiveComponent().getId(),
                replayOutcomeDTO.getDefinitiveComponent().getId()
        );
        assertTrue(replayOutcomeDTO.isIdempotentReplay());
        FinancialTransactionPlan reloadedFinancialTransactionPlan =
                financialTransactionPlanPersistencePort.findBySource(
                                FinancialTransactionSourceType.BOOKING,
                                40L
                        )
                        .orElseThrow();
        assertFalse(reloadedFinancialTransactionPlan.containsFinancialTransaction(
                provisionalCheckOutFinancialTransactionId
        ));
        assertEquals(2, reloadedFinancialTransactionPlan.getFinancialTransactionCount());
        verify(financialParticipantNotifier, times(1)).notifyParticipantDeletion(any());
        verify(financialPostCommitAuditPort, times(1)).recordAfterCommit(any(), any(), any());
    }

    @Test
    void auditRegistrationFailureRollsBackBeforeCommit() {
        doThrow(new IllegalStateException("audit registration failed"))
                .when(financialPostCommitAuditPort)
                .recordAfterCommit(any(), any(), any());

        assertThrows(
                IllegalStateException.class,
                () -> financialTransactionPlanReplacementService.replace(command(
                        "replacement-audit-registration"
                ))
        );

        FinancialTransactionPlan reloadedFinancialTransactionPlan =
                financialTransactionPlanPersistencePort.findById(planId).orElseThrow();
        assertFalse(reloadedFinancialTransactionPlan
                .findFinancialTransactionById(provisionalFinancialTransactionId)
                .isEmpty());
        assertEquals(2, jdbcTemplate.queryForObject(
                "select count(*) from financial_transactions",
                Integer.class
        ));
        assertEquals(0, jdbcTemplate.queryForObject(
                "select count(*) from financial_command_idempotency",
                Integer.class
        ));
    }

    private FinancialTransactionPlanReplacementCommandRecord command(String idempotencyKey) {
        return new FinancialTransactionPlanReplacementCommandRecord(
                planId,
                FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                provisionalFinancialTransactionId,
                FinancialPaymentStructure.SIMPLE,
                FinancialTransactionMethod.PIX,
                null,
                idempotencyKey
        );
    }

    private FinancialTransactionPlan initialPlan() {
        return new FinancialTransactionPlan(
                FinancialPartyType.GUEST,
                7L,
                FinancialPartyType.CASHIER,
                1L,
                FinancialTransactionSourceType.BOOKING,
                40L,
                List.of(
                        provisionalTransaction(
                                FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                                new BigDecimal("600.00"),
                                LocalDate.now().plusDays(1)
                        ),
                        provisionalTransaction(
                                FinancialTransactionType.PLAN_CHECK_OUT_PAYMENT,
                                new BigDecimal("400.00"),
                                LocalDate.now().plusMonths(1)
                        )
                ),
                LocalDate.now().plusMonths(6),
                "Plano financeiro da reserva"
        );
    }

    private FinancialTransaction provisionalTransaction(
            FinancialTransactionType financialTransactionType,
            BigDecimal amount,
            LocalDate dueDate
    ) {
        return new FinancialTransaction(
                FinancialPartyType.GUEST,
                7L,
                FinancialPartyType.CASHIER,
                1L,
                financialTransactionType,
                amount,
                LocalDate.now(),
                dueDate,
                "Pagamento provisorio",
                null,
                FinancialTransactionStatus.WAITING
        );
    }
}
