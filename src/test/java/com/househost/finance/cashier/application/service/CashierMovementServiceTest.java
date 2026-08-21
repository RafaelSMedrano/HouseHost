package com.househost.finance.cashier.application.service;

import com.househost.finance.cashier.application.port.out.CashierEntryPersistencePort;
import com.househost.finance.cashier.application.port.out.CashierExpensePersistencePort;
import com.househost.finance.cashier.application.port.out.CashierPersistencePort;
import com.househost.finance.cashier.domain.model.Cashier;
import com.househost.finance.cashier.domain.model.CashierEntry;
import com.househost.finance.cashier.domain.model.CashierExpense;
import com.househost.finance.cashier.domain.model.CashierStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CashierMovementServiceTest {

    @Test
    void schedulesDepositAtDueDateWithoutChangingRealizedBalances() {
        TestContextRecord testContextRecord = testContextRecord();
        Cashier cashier = cashier();
        FinancialTransaction financialTransaction = depositTransaction();
        when(testContextRecord.cashierPersistencePort.findByIdForUpdate(1L))
                .thenReturn(Optional.of(cashier));
        when(testContextRecord.cashierEntryPersistencePort.findBySourceTransactionIdAndCashierId(100L, 1L))
                .thenReturn(Optional.empty());
        when(testContextRecord.cashierEntryPersistencePort.save(any(CashierEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CashierEntry cashierEntry = testContextRecord.cashierMovementService.scheduleDeposit(
                1L,
                financialTransaction
        );

        assertEquals(financialTransaction.getDueDate(), cashierEntry.getDueDate());
        assertNull(cashierEntry.getSettlementDate());
        assertEquals(new BigDecimal("250.00"), cashier.getOnWaiting());
        assertEquals(new BigDecimal("250.00"), cashier.getExpectedInflow());
        assertEquals(new BigDecimal("1000.00"), cashier.getCashOnHand());
        assertEquals(BigDecimal.ZERO, cashier.getTotalInflow());
    }

    @Test
    void settlesDepositOnceAndPreservesDueDate() {
        TestContextRecord testContextRecord = testContextRecord();
        Cashier cashier = cashier();
        FinancialTransaction financialTransaction = depositTransaction();
        CashierEntry cashierEntry = waitingEntry(cashier, financialTransaction);
        cashier.deposit(cashierEntry);
        financialTransaction.setStatus(FinancialTransactionStatus.SETTLED);
        when(testContextRecord.cashierPersistencePort.findByIdForUpdate(1L))
                .thenReturn(Optional.of(cashier));
        when(testContextRecord.cashierEntryPersistencePort.findBySourceTransactionIdAndCashierId(100L, 1L))
                .thenReturn(Optional.of(cashierEntry));
        when(testContextRecord.cashierEntryPersistencePort.save(cashierEntry)).thenReturn(cashierEntry);

        CashierEntry settledCashierEntry = testContextRecord.cashierMovementService.settleDeposit(
                1L,
                financialTransaction
        );
        CashierEntry repeatedCashierEntry = testContextRecord.cashierMovementService.settleDeposit(
                1L,
                financialTransaction
        );

        assertEquals(financialTransaction.getDueDate(), settledCashierEntry.getDueDate());
        assertEquals(financialTransaction.getSettlementDate(), settledCashierEntry.getSettlementDate());
        assertEquals(FinancialTransactionStatus.SETTLED, settledCashierEntry.getStatus());
        assertEquals(new BigDecimal("1250.00"), cashier.getCashOnHand());
        assertEquals(new BigDecimal("250.00"), cashier.getTotalInflow());
        assertEquals(BigDecimal.ZERO.setScale(2), cashier.getOnWaiting());
        assertEquals(settledCashierEntry, repeatedCashierEntry);
        verify(testContextRecord.cashierEntryPersistencePort).save(cashierEntry);
    }

    @Test
    void reversesWaitingWithdrawalWithoutChangingRealizedBalances() {
        TestContextRecord testContextRecord = testContextRecord();
        Cashier cashier = cashier();
        FinancialTransaction financialTransaction = withdrawalTransaction();
        CashierExpense cashierExpense = waitingExpense(cashier, financialTransaction);
        cashier.withdraw(cashierExpense);
        when(testContextRecord.cashierExpensePersistencePort.findBySourceTransactionId(100L))
                .thenReturn(
                        List.of(cashierExpense),
                        List.of(cashierExpense),
                        List.of(),
                        List.of()
                );
        when(testContextRecord.cashierEntryPersistencePort.findBySourceTransactionId(100L))
                .thenReturn(List.of());
        when(testContextRecord.cashierPersistencePort.findByIdForUpdate(1L))
                .thenReturn(Optional.of(cashier));

        testContextRecord.cashierMovementService.reverseMovementsForTransaction(100L);
        testContextRecord.cashierMovementService.reverseMovementsForTransaction(100L);

        assertEquals(BigDecimal.ZERO.setScale(2), cashier.getOnWaiting());
        assertEquals(BigDecimal.ZERO.setScale(2), cashier.getExpectedOutflow());
        assertEquals(new BigDecimal("1000.00"), cashier.getCashOnHand());
        assertEquals(BigDecimal.ZERO, cashier.getTotalOutflow());
        verify(testContextRecord.cashierExpensePersistencePort).deleteAll(List.of(cashierExpense));
    }

    @Test
    void repeatedScheduleReturnsExistingMovementWithoutDuplicatingProjection() {
        TestContextRecord testContextRecord = testContextRecord();
        Cashier cashier = cashier();
        FinancialTransaction financialTransaction = depositTransaction();
        CashierEntry cashierEntry = waitingEntry(cashier, financialTransaction);
        when(testContextRecord.cashierPersistencePort.findByIdForUpdate(1L))
                .thenReturn(Optional.of(cashier));
        when(testContextRecord.cashierEntryPersistencePort.findBySourceTransactionIdAndCashierId(100L, 1L))
                .thenReturn(Optional.of(cashierEntry));

        CashierEntry existingCashierEntry = testContextRecord.cashierMovementService.scheduleDeposit(
                1L,
                financialTransaction
        );

        assertEquals(cashierEntry, existingCashierEntry);
        assertEquals(BigDecimal.ZERO, cashier.getOnWaiting());
        verify(testContextRecord.cashierEntryPersistencePort, never()).save(any(CashierEntry.class));
        verify(testContextRecord.cashierPersistencePort, never()).save(any(Cashier.class));
    }

    @Test
    void replacementReversesAndReschedulesWithoutChangingRealizedBalances() {
        TestContextRecord testContextRecord = testContextRecord();
        Cashier cashier = cashier();
        FinancialTransaction provisionalFinancialTransaction = depositTransaction();
        FinancialTransaction definitiveFinancialTransaction = transaction(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L
        );
        definitiveFinancialTransaction.restorePersistenceState(101L, null, null);
        CashierEntry provisionalCashierEntry = waitingEntry(
                cashier,
                provisionalFinancialTransaction
        );
        cashier.deposit(provisionalCashierEntry);
        when(testContextRecord.cashierEntryPersistencePort
                .findBySourceTransactionId(100L))
                .thenReturn(List.of(provisionalCashierEntry), List.of(provisionalCashierEntry));
        when(testContextRecord.cashierExpensePersistencePort
                .findBySourceTransactionId(100L))
                .thenReturn(List.of());
        when(testContextRecord.cashierPersistencePort.findByIdForUpdate(1L))
                .thenReturn(Optional.of(cashier));
        when(testContextRecord.cashierEntryPersistencePort
                .findBySourceTransactionIdAndCashierId(101L, 1L))
                .thenReturn(Optional.empty());
        when(testContextRecord.cashierEntryPersistencePort.save(any(CashierEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        testContextRecord.cashierMovementService.reverseMovementsForTransaction(100L);
        testContextRecord.cashierMovementService.scheduleDeposit(
                1L,
                definitiveFinancialTransaction
        );

        assertEquals(new BigDecimal("1000.00"), cashier.getCashOnHand());
        assertEquals(BigDecimal.ZERO, cashier.getTotalInflow());
        assertEquals(new BigDecimal("250.00"), cashier.getOnWaiting());
        assertEquals(new BigDecimal("250.00"), cashier.getExpectedInflow());
    }

    private TestContextRecord testContextRecord() {
        CashierPersistencePort cashierPersistencePort = mock(CashierPersistencePort.class);
        CashierEntryPersistencePort cashierEntryPersistencePort = mock(CashierEntryPersistencePort.class);
        CashierExpensePersistencePort cashierExpensePersistencePort = mock(CashierExpensePersistencePort.class);
        CashierMovementValidationService cashierMovementValidationService =
                new CashierMovementValidationService();
        CashierMovementService cashierMovementService = new CashierMovementService(
                cashierPersistencePort,
                cashierEntryPersistencePort,
                cashierExpensePersistencePort,
                cashierMovementValidationService
        );
        return new TestContextRecord(
                cashierMovementService,
                cashierPersistencePort,
                cashierEntryPersistencePort,
                cashierExpensePersistencePort
        );
    }

    private Cashier cashier() {
        Cashier cashier = new Cashier(
                "Caixa principal",
                null,
                new BigDecimal("1000.00"),
                CashierStatus.OPEN
        );
        cashier.restorePersistenceState(1L, BigDecimal.ZERO, null, null);
        return cashier;
    }

    private CashierEntry waitingEntry(
            Cashier cashier,
            FinancialTransaction financialTransaction
    ) {
        return new CashierEntry(
                cashier,
                "Entrada agendada",
                financialTransaction.getAmount(),
                financialTransaction.getDueDate(),
                "FINANCIAL_TRANSACTION",
                FinancialTransactionStatus.WAITING,
                financialTransaction
        );
    }

    private CashierExpense waitingExpense(
            Cashier cashier,
            FinancialTransaction financialTransaction
    ) {
        return new CashierExpense(
                cashier,
                "Saida agendada",
                financialTransaction.getAmount().negate(),
                financialTransaction.getDueDate(),
                "FINANCIAL_TRANSACTION",
                FinancialTransactionStatus.WAITING,
                financialTransaction
        );
    }

    private FinancialTransaction depositTransaction() {
        FinancialTransaction financialTransaction = transaction(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L
        );
        financialTransaction.restorePersistenceState(100L, null, null);
        return financialTransaction;
    }

    private FinancialTransaction withdrawalTransaction() {
        FinancialTransaction financialTransaction = transaction(
                FinancialPartyType.CASHIER,
                1L,
                FinancialPartyType.GUEST,
                20L
        );
        financialTransaction.restorePersistenceState(100L, null, null);
        return financialTransaction;
    }

    private FinancialTransaction transaction(
            FinancialPartyType senderType,
            Long senderId,
            FinancialPartyType receiverType,
            Long receiverId
    ) {
        return new FinancialTransaction(
                senderType,
                senderId,
                receiverType,
                receiverId,
                FinancialTransactionType.STANDARD,
                new BigDecimal("250.00"),
                LocalDate.of(2026, 8, 18),
                LocalDate.of(2026, 9, 10),
                "Movimento do caixa",
                null,
                FinancialTransactionStatus.WAITING
        );
    }

    private record TestContextRecord(
            CashierMovementService cashierMovementService,
            CashierPersistencePort cashierPersistencePort,
            CashierEntryPersistencePort cashierEntryPersistencePort,
            CashierExpensePersistencePort cashierExpensePersistencePort
    ) {
    }
}
