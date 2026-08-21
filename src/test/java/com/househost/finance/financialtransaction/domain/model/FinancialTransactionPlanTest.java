package com.househost.finance.financialtransaction.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialTransactionPlanTest {

    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2026, 8, 18);
    private static final LocalDate PLAN_DUE_DATE = LocalDate.of(2027, 12, 31);

    @Test
    void defensivelyOrdersAndTotalizesDirectComponentsWithStableEqualDates() {
        FinancialTransaction laterFinancialTransaction = financialTransaction(
                3L,
                new BigDecimal("300.00"),
                LocalDate.of(2027, 3, 10),
                FinancialTransactionType.PLAN_CHECK_OUT_PAYMENT,
                FinancialTransactionStatus.WAITING
        );
        FinancialTransaction firstEqualDateFinancialTransaction = financialTransaction(
                1L,
                new BigDecimal("100.00"),
                LocalDate.of(2027, 2, 10),
                FinancialTransactionType.PLAN_DOWN_PAYMENT,
                FinancialTransactionStatus.WAITING
        );
        FinancialTransaction secondEqualDateFinancialTransaction = financialTransaction(
                2L,
                new BigDecimal("200.00"),
                LocalDate.of(2027, 2, 10),
                FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                FinancialTransactionStatus.WAITING
        );
        List<FinancialTransaction> sourceFinancialTransactionList = new ArrayList<>(List.of(
                laterFinancialTransaction,
                firstEqualDateFinancialTransaction,
                secondEqualDateFinancialTransaction
        ));

        FinancialTransactionPlan financialTransactionPlan = plan(sourceFinancialTransactionList);
        sourceFinancialTransactionList.clear();

        assertEquals(
                List.of(
                        firstEqualDateFinancialTransaction,
                        secondEqualDateFinancialTransaction,
                        laterFinancialTransaction
                ),
                financialTransactionPlan.getFinancialTransactionList()
        );
        assertEquals(new BigDecimal("600.00"), financialTransactionPlan.getTotalAmount());
        assertThrows(
                UnsupportedOperationException.class,
                () -> financialTransactionPlan.getFinancialTransactionList().clear()
        );
        assertEquals(firstEqualDateFinancialTransaction,
                financialTransactionPlan.findFinancialTransactionByOrder(1).orElseThrow());
        assertEquals(3, financialTransactionPlan.getFinancialTransactionCount());
        assertTrue(financialTransactionPlan.containsFinancialTransaction(2L));
    }

    @Test
    void rejectsInvalidDirectComponentsAndDeadlineViolations() {
        FinancialTransaction validFinancialTransaction = financialTransaction(
                1L,
                new BigDecimal("100.00"),
                LocalDate.of(2027, 2, 10),
                FinancialTransactionType.PLAN_TRANSACTION,
                FinancialTransactionStatus.WAITING
        );
        FinancialTransaction mismatchedParticipantFinancialTransaction = new FinancialTransaction(
                FinancialPartyType.GUEST,
                99L,
                FinancialPartyType.CASHIER,
                1L,
                FinancialTransactionType.PLAN_TRANSACTION,
                new BigDecimal("100.00"),
                TRANSACTION_DATE,
                LocalDate.of(2027, 2, 10),
                "Participante divergente",
                FinancialTransactionMethod.PIX,
                FinancialTransactionStatus.WAITING
        );
        FinancialTransaction lateFinancialTransaction = financialTransaction(
                2L,
                new BigDecimal("100.00"),
                PLAN_DUE_DATE.plusDays(1),
                FinancialTransactionType.PLAN_TRANSACTION,
                FinancialTransactionStatus.WAITING
        );
        InstallmentPlanTransaction installmentPlanTransaction = installmentPlanTransaction(
                3L,
                new BigDecimal("300.00"),
                3
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> plan(List.of(validFinancialTransaction, validFinancialTransaction))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> plan(List.of(mismatchedParticipantFinancialTransaction))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> plan(List.of(lateFinancialTransaction))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> plan(List.of(installmentPlanTransaction.getInstallments().get(0)))
        );
    }

    @Test
    void countsInstallmentBlockOnceAndExpandsItOnlyForSettlement() {
        FinancialTransaction ordinaryFinancialTransaction = financialTransaction(
                1L,
                new BigDecimal("100.00"),
                LocalDate.of(2026, 9, 10),
                FinancialTransactionType.PLAN_DOWN_PAYMENT,
                FinancialTransactionStatus.WAITING
        );
        InstallmentPlanTransaction installmentPlanTransaction = installmentPlanTransaction(
                2L,
                new BigDecimal("100.00"),
                3
        );

        FinancialTransactionPlan financialTransactionPlan = plan(List.of(
                ordinaryFinancialTransaction,
                installmentPlanTransaction
        ));
        List<FinancialTransaction> settlementFinancialTransactionList =
                financialTransactionPlan.getSettlementFinancialTransactionList();

        assertEquals(new BigDecimal("200.00"), financialTransactionPlan.getTotalAmount());
        assertEquals(4, settlementFinancialTransactionList.size());
        assertFalse(settlementFinancialTransactionList.contains(installmentPlanTransaction));
        assertInstanceOf(InstallmentTransaction.class, settlementFinancialTransactionList.get(1));
        assertEquals(new BigDecimal("33.33"), installmentPlanTransaction.getInstallments().get(0).getAmount());
        assertEquals(new BigDecimal("33.34"), installmentPlanTransaction.getInstallments().get(2).getAmount());
    }

    @Test
    void derivesSettlementQueriesAmountsAndStatusPrecedence() {
        FinancialTransaction settledFinancialTransaction = financialTransaction(
                1L,
                new BigDecimal("100.00"),
                LocalDate.of(2026, 7, 10),
                FinancialTransactionType.PLAN_DOWN_PAYMENT,
                FinancialTransactionStatus.SETTLED
        );
        settledFinancialTransaction.restorePersistenceState(
                1L,
                TRANSACTION_DATE,
                LocalDate.of(2026, 7, 11),
                null,
                null
        );
        FinancialTransaction waitingFinancialTransaction = financialTransaction(
                2L,
                new BigDecimal("300.00"),
                LocalDate.of(2026, 9, 10),
                FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                FinancialTransactionStatus.WAITING
        );
        FinancialTransactionPlan financialTransactionPlan = plan(List.of(
                settledFinancialTransaction,
                waitingFinancialTransaction
        ));

        financialTransactionPlan.refreshDerivedState(LocalDate.of(2026, 8, 18));

        assertEquals(
                FinancialTransactionPlanStatus.PARTIALLY_SETTLED,
                financialTransactionPlan.getStatus()
        );
        assertEquals(new BigDecimal("100.00"), financialTransactionPlan.calculateSettledAmount());
        assertEquals(new BigDecimal("300.00"), financialTransactionPlan.calculateOutstandingAmount());
        assertEquals(new BigDecimal("25.00"), financialTransactionPlan.calculateSettlementPercentage());
        assertEquals(waitingFinancialTransaction,
                financialTransactionPlan.findNextFinancialTransactionToSettle().orElseThrow());
        assertEquals(settledFinancialTransaction,
                financialTransactionPlan.findLastSettledFinancialTransaction().orElseThrow());

        financialTransactionPlan.refreshDerivedState(LocalDate.of(2026, 10, 1));
        assertEquals(FinancialTransactionPlanStatus.OVERDUE, financialTransactionPlan.getStatus());
        assertEquals(new BigDecimal("300.00"),
                financialTransactionPlan.calculateOverdueAmount(LocalDate.of(2026, 10, 1)));
    }

    @Test
    void derivesCompleteSettlementDateAndMakesSettledPlanImmutable() {
        FinancialTransaction firstFinancialTransaction = settledFinancialTransaction(
                1L,
                new BigDecimal("100.00"),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 12)
        );
        FinancialTransaction lastFinancialTransaction = settledFinancialTransaction(
                2L,
                new BigDecimal("200.00"),
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 15)
        );
        FinancialTransactionPlan financialTransactionPlan = plan(List.of(
                firstFinancialTransaction,
                lastFinancialTransaction
        ));

        assertEquals(FinancialTransactionPlanStatus.SETTLED, financialTransactionPlan.getStatus());
        assertEquals(LocalDate.of(2026, 9, 15), financialTransactionPlan.getPlanSettlementDate());
        assertTrue(financialTransactionPlan.isFullySettled());
        assertFalse(financialTransactionPlan.isEligibleForPhysicalDeletion());
        assertThrows(
                IllegalStateException.class,
                () -> financialTransactionPlan.removeFinancialTransaction(1L)
        );
        assertThrows(IllegalStateException.class, financialTransactionPlan::cancel);
    }

    @Test
    void managesCompositionDeadlineCancellationAndIdentityMembership() {
        FinancialTransaction firstFinancialTransaction = financialTransaction(
                1L,
                new BigDecimal("100.00"),
                LocalDate.of(2027, 2, 10),
                FinancialTransactionType.PLAN_DOWN_PAYMENT,
                FinancialTransactionStatus.WAITING
        );
        FinancialTransaction secondFinancialTransaction = financialTransaction(
                2L,
                new BigDecimal("200.00"),
                LocalDate.of(2027, 3, 10),
                FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                FinancialTransactionStatus.WAITING
        );
        FinancialTransactionPlan financialTransactionPlan = plan(List.of(
                firstFinancialTransaction,
                secondFinancialTransaction
        ));
        financialTransactionPlan.assignIdentity(50L);
        FinancialTransaction replacementFinancialTransaction = financialTransaction(
                3L,
                new BigDecimal("250.00"),
                LocalDate.of(2027, 3, 10),
                FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                FinancialTransactionStatus.WAITING
        );
        replacementFinancialTransaction.assignPlanMembership(50L, 2);

        financialTransactionPlan.replaceFinancialTransaction(2L, replacementFinancialTransaction);
        FinancialTransaction removedFinancialTransaction =
                financialTransactionPlan.removeFinancialTransaction(1L);
        financialTransactionPlan.extendPlanDueDate(LocalDate.of(2028, 1, 31));

        assertEquals(firstFinancialTransaction, removedFinancialTransaction);
        assertEquals(new BigDecimal("250.00"), financialTransactionPlan.getTotalAmount());
        assertEquals(FinancialTransactionSourceType.PLAN,
                replacementFinancialTransaction.getSourceType());
        assertEquals(50L, replacementFinancialTransaction.getSourceId());
        assertEquals(1, replacementFinancialTransaction.getPlanComponentOrder());
        assertEquals(LocalDate.of(2028, 1, 31), financialTransactionPlan.getPlanDueDate());
        assertTrue(financialTransactionPlan.isEligibleForPhysicalDeletion());

        financialTransactionPlan.cancel();
        assertEquals(FinancialTransactionPlanStatus.CANCELED, financialTransactionPlan.getStatus());
        assertThrows(
                IllegalStateException.class,
                () -> financialTransactionPlan.addFinancialTransaction(replacementFinancialTransaction)
        );
    }

    @Test
    void enforcesInstallmentQuantityFromTwoThroughTwelve() {
        assertThrows(
                IllegalArgumentException.class,
                () -> installmentPlanTransaction(null, new BigDecimal("100.00"), 1)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> installmentPlanTransaction(null, new BigDecimal("100.00"), 13)
        );
        assertEquals(2,
                installmentPlanTransaction(null, new BigDecimal("100.00"), 2)
                        .getInstallments().size());
        assertEquals(12,
                installmentPlanTransaction(null, new BigDecimal("120.00"), 12)
                        .getInstallments().size());
    }

    @Test
    void answersDateRangeOptionalAndPayableSummaryQueries() {
        FinancialTransaction canceledFinancialTransaction = financialTransaction(
                1L,
                new BigDecimal("50.00"),
                LocalDate.of(2026, 7, 10),
                FinancialTransactionType.PLAN_DOWN_PAYMENT,
                FinancialTransactionStatus.CANCELED
        );
        FinancialTransaction overdueFinancialTransaction = financialTransaction(
                2L,
                new BigDecimal("100.00"),
                LocalDate.of(2026, 8, 10),
                FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                FinancialTransactionStatus.WAITING
        );
        FinancialTransaction futureFinancialTransaction = financialTransaction(
                3L,
                new BigDecimal("200.00"),
                LocalDate.of(2027, 3, 10),
                FinancialTransactionType.PLAN_CHECK_OUT_PAYMENT,
                FinancialTransactionStatus.WAITING
        );
        FinancialTransactionPlan financialTransactionPlan = plan(List.of(
                futureFinancialTransaction,
                canceledFinancialTransaction,
                overdueFinancialTransaction
        ));

        assertTrue(financialTransactionPlan.findFinancialTransactionById(999L).isEmpty());
        assertTrue(financialTransactionPlan.findFinancialTransactionByOrder(0).isEmpty());
        assertEquals(
                List.of(overdueFinancialTransaction),
                financialTransactionPlan.getFinancialTransactionListDueOn(
                        LocalDate.of(2026, 8, 10)
                )
        );
        assertEquals(
                List.of(canceledFinancialTransaction, overdueFinancialTransaction),
                financialTransactionPlan.getFinancialTransactionListDueBetween(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 8, 31)
                )
        );
        assertEquals(
                List.of(overdueFinancialTransaction),
                financialTransactionPlan.getOverdueFinancialTransactionList(
                        LocalDate.of(2026, 9, 1)
                )
        );
        assertTrue(financialTransactionPlan.hasOverdueFinancialTransaction(
                LocalDate.of(2026, 9, 1)
        ));
        assertEquals(LocalDate.of(2026, 7, 10), financialTransactionPlan.getFirstDueDate());
        assertEquals(LocalDate.of(2027, 3, 10), financialTransactionPlan.getLastDueDate());
        assertEquals(new BigDecimal("300.00"), financialTransactionPlan.calculateOutstandingAmount());
        assertEquals(3, financialTransactionPlan.getUnsettledFinancialTransactionList().size());
        assertThrows(
                IllegalArgumentException.class,
                () -> financialTransactionPlan.getFinancialTransactionListDueBetween(
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 8, 1)
                )
        );
    }

    @Test
    void addsSingleAndGroupedComponentsWhileRecalculatingStableOrderAndTotal() {
        FinancialTransaction firstFinancialTransaction = financialTransaction(
                1L,
                new BigDecimal("100.00"),
                LocalDate.of(2027, 3, 10),
                FinancialTransactionType.PLAN_TRANSACTION,
                FinancialTransactionStatus.WAITING
        );
        FinancialTransactionPlan financialTransactionPlan = plan(List.of(
                firstFinancialTransaction
        ));
        financialTransactionPlan.assignIdentity(80L);
        FinancialTransaction earlierFinancialTransaction = financialTransaction(
                2L,
                new BigDecimal("50.00"),
                LocalDate.of(2027, 1, 10),
                FinancialTransactionType.PLAN_DOWN_PAYMENT,
                FinancialTransactionStatus.WAITING
        );
        FinancialTransaction middleFinancialTransaction = financialTransaction(
                3L,
                new BigDecimal("75.00"),
                LocalDate.of(2027, 2, 10),
                FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                FinancialTransactionStatus.WAITING
        );
        earlierFinancialTransaction.assignPlanMembership(80L, 2);
        middleFinancialTransaction.assignPlanMembership(80L, 3);

        financialTransactionPlan.addFinancialTransaction(earlierFinancialTransaction);
        financialTransactionPlan.addFinancialTransactionList(List.of(
                middleFinancialTransaction
        ));

        assertEquals(
                List.of(
                        earlierFinancialTransaction,
                        middleFinancialTransaction,
                        firstFinancialTransaction
                ),
                financialTransactionPlan.getFinancialTransactionList()
        );
        assertEquals(new BigDecimal("225.00"), financialTransactionPlan.getTotalAmount());
        assertEquals(FinancialTransactionPlanStatus.ACTIVE, financialTransactionPlan.getStatus());
        assertEquals(1, earlierFinancialTransaction.getPlanComponentOrder());
        assertEquals(2, middleFinancialTransaction.getPlanComponentOrder());
        assertEquals(3, firstFinancialTransaction.getPlanComponentOrder());
    }

    @Test
    void acceptsStandardComponentsAndRejectsInvalidPlanMembershipAndExternalOwnership() {
        FinancialTransaction standardFinancialTransaction = financialTransaction(
                1L,
                new BigDecimal("100.00"),
                LocalDate.of(2027, 3, 10),
                FinancialTransactionType.STANDARD,
                FinancialTransactionStatus.WAITING
        );
        assertDoesNotThrow(() -> plan(List.of(standardFinancialTransaction)));

        FinancialTransaction initialFinancialTransaction = financialTransaction(
                2L,
                new BigDecimal("100.00"),
                LocalDate.of(2027, 3, 10),
                FinancialTransactionType.PLAN_TRANSACTION,
                FinancialTransactionStatus.WAITING
        );
        FinancialTransactionPlan financialTransactionPlan = plan(List.of(
                initialFinancialTransaction
        ));
        financialTransactionPlan.assignIdentity(90L);
        FinancialTransaction wrongPlanFinancialTransaction = financialTransaction(
                3L,
                new BigDecimal("50.00"),
                LocalDate.of(2027, 4, 10),
                FinancialTransactionType.PLAN_TRANSACTION,
                FinancialTransactionStatus.WAITING
        );
        wrongPlanFinancialTransaction.assignPlanMembership(91L, 2);

        assertThrows(
                IllegalArgumentException.class,
                () -> financialTransactionPlan.addFinancialTransaction(
                        wrongPlanFinancialTransaction
                )
        );
        assertThrows(
                IllegalStateException.class,
                () -> financialTransactionPlan.assignIdentity(91L)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new FinancialTransactionPlan(
                        FinancialPartyType.GUEST,
                        20L,
                        FinancialPartyType.CASHIER,
                        1L,
                        FinancialTransactionSourceType.PLAN,
                        30L,
                        List.of(financialTransaction(
                                4L,
                                new BigDecimal("100.00"),
                                LocalDate.of(2027, 3, 10),
                                FinancialTransactionType.PLAN_TRANSACTION,
                                FinancialTransactionStatus.WAITING
                        )),
                        PLAN_DUE_DATE,
                        "Origem invalida"
                )
        );
    }

    @Test
    void preventsRemovalOfAnInstallmentBlockWithSettledHistory() {
        InstallmentPlanTransaction installmentPlanTransaction = installmentPlanTransaction(
                10L,
                new BigDecimal("300.00"),
                3
        );
        installmentPlanTransaction.getInstallments().get(0).settle();
        installmentPlanTransaction.refreshStatus();
        FinancialTransactionPlan financialTransactionPlan = plan(List.of(
                installmentPlanTransaction,
                financialTransaction(
                        11L,
                        new BigDecimal("100.00"),
                        LocalDate.of(2027, 8, 10),
                        FinancialTransactionType.PLAN_TRANSACTION,
                        FinancialTransactionStatus.WAITING
                )
        ));
        financialTransactionPlan.assignIdentity(100L);

        assertThrows(
                IllegalStateException.class,
                () -> financialTransactionPlan.removeFinancialTransaction(10L)
        );
        assertThrows(IllegalStateException.class, financialTransactionPlan::cancel);
        assertFalse(financialTransactionPlan.isEligibleForPhysicalDeletion());
    }

    private FinancialTransactionPlan plan(
            List<FinancialTransaction> financialTransactionList
    ) {
        return new FinancialTransactionPlan(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L,
                FinancialTransactionSourceType.BOOKING,
                30L,
                financialTransactionList,
                PLAN_DUE_DATE,
                "Plano da reserva"
        );
    }

    private FinancialTransaction financialTransaction(
            Long id,
            BigDecimal amount,
            LocalDate dueDate,
            FinancialTransactionType financialTransactionType,
            FinancialTransactionStatus financialTransactionStatus
    ) {
        FinancialTransaction financialTransaction = new FinancialTransaction(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L,
                financialTransactionType,
                amount,
                TRANSACTION_DATE,
                dueDate,
                "Componente do plano",
                FinancialTransactionMethod.PIX,
                financialTransactionStatus
        );
        financialTransaction.restorePersistenceState(id, null, null);
        return financialTransaction;
    }

    private FinancialTransaction settledFinancialTransaction(
            Long id,
            BigDecimal amount,
            LocalDate dueDate,
            LocalDate settlementDate
    ) {
        FinancialTransaction financialTransaction = financialTransaction(
                id,
                amount,
                dueDate,
                FinancialTransactionType.PLAN_TRANSACTION,
                FinancialTransactionStatus.SETTLED
        );
        financialTransaction.restorePersistenceState(
                id,
                TRANSACTION_DATE,
                settlementDate,
                null,
                null
        );
        return financialTransaction;
    }

    private InstallmentPlanTransaction installmentPlanTransaction(
            Long id,
            BigDecimal amount,
            int installmentsQuantity
    ) {
        InstallmentPlanTransaction installmentPlanTransaction = new InstallmentPlanTransaction(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L,
                amount,
                LocalDate.of(2026, 9, 10),
                "Bloco parcelado",
                FinancialTransactionMethod.CREDIT_CARD,
                installmentsQuantity,
                10,
                FinancialTransactionType.INSTALLMENT_PLAN_BLOCK
        );
        installmentPlanTransaction.restorePersistenceState(id, null, null);
        return installmentPlanTransaction;
    }
}
