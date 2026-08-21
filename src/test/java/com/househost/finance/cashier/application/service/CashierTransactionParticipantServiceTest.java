package com.househost.finance.cashier.application.service;

import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class CashierTransactionParticipantServiceTest {

    @Test
    void registersAndSettlesDepositWhenCashierReceivesTransaction() {
        CashierMovementService cashierMovementService = mock(CashierMovementService.class);
        CashierTransactionParticipantService cashierTransactionParticipantService =
                new CashierTransactionParticipantService(cashierMovementService);
        FinancialTransaction transaction = entryTransaction();

        cashierTransactionParticipantService.registerTransaction(1L, transaction);
        cashierTransactionParticipantService.settleTransaction(1L, transaction);

        verify(cashierMovementService).scheduleDeposit(1L, transaction);
        verify(cashierMovementService).settleDeposit(1L, transaction);
        verifyNoMoreInteractions(cashierMovementService);
    }

    @Test
    void registersAndSettlesWithdrawalWhenCashierSendsTransaction() {
        CashierMovementService cashierMovementService = mock(CashierMovementService.class);
        CashierTransactionParticipantService cashierTransactionParticipantService =
                new CashierTransactionParticipantService(cashierMovementService);
        FinancialTransaction transaction = expenseTransaction();

        cashierTransactionParticipantService.registerTransaction(1L, transaction);
        cashierTransactionParticipantService.settleTransaction(1L, transaction);

        verify(cashierMovementService).scheduleWithdrawal(1L, transaction);
        verify(cashierMovementService).settleWithdrawal(1L, transaction);
        verifyNoMoreInteractions(cashierMovementService);
    }

    @Test
    void registersAndReversesEveryInstallmentMovement() {
        CashierMovementService cashierMovementService = mock(CashierMovementService.class);
        CashierTransactionParticipantService cashierTransactionParticipantService =
                new CashierTransactionParticipantService(cashierMovementService);
        InstallmentPlanTransaction installmentPlanTransaction = installmentPlanTransaction();
        InstallmentTransaction firstInstallmentTransaction = installmentPlanTransaction.findInstallment(1);
        InstallmentTransaction secondInstallmentTransaction = installmentPlanTransaction.findInstallment(2);
        firstInstallmentTransaction.restorePersistenceState(101L, null, null);
        secondInstallmentTransaction.restorePersistenceState(102L, null, null);

        cashierTransactionParticipantService.registerTransaction(1L, installmentPlanTransaction);
        cashierTransactionParticipantService.reverseTransaction(installmentPlanTransaction);

        verify(cashierMovementService).scheduleDeposit(1L, firstInstallmentTransaction);
        verify(cashierMovementService).scheduleDeposit(1L, secondInstallmentTransaction);
        verify(cashierMovementService).reverseMovementsForTransaction(101L);
        verify(cashierMovementService).reverseMovementsForTransaction(102L);
        verifyNoMoreInteractions(cashierMovementService);
    }

    @Test
    void reversesSingleTransactionMovement() {
        CashierMovementService cashierMovementService = mock(CashierMovementService.class);
        CashierTransactionParticipantService cashierTransactionParticipantService =
                new CashierTransactionParticipantService(cashierMovementService);
        FinancialTransaction transaction = entryTransaction();
        transaction.restorePersistenceState(100L, null, null);

        cashierTransactionParticipantService.reverseTransaction(transaction);

        verify(cashierMovementService).reverseMovementsForTransaction(100L);
        verifyNoMoreInteractions(cashierMovementService);
    }

    private FinancialTransaction entryTransaction() {
        return new FinancialTransaction(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L,
                FinancialTransactionType.STANDARD,
                new BigDecimal("250.00"),
                LocalDate.of(2026, 8, 13),
                "Entrada no caixa"
        );
    }

    private FinancialTransaction expenseTransaction() {
        return new FinancialTransaction(
                FinancialPartyType.CASHIER,
                1L,
                FinancialPartyType.GUEST,
                20L,
                FinancialTransactionType.STANDARD,
                new BigDecimal("250.00"),
                LocalDate.of(2026, 8, 13),
                "Saida do caixa"
        );
    }

    private InstallmentPlanTransaction installmentPlanTransaction() {
        return new InstallmentPlanTransaction(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L,
                new BigDecimal("400.00"),
                LocalDate.of(2026, 8, 13),
                "Entrada parcelada",
                null,
                2,
                13
        );
    }
}
