package com.househost.finance.cashier.application.service;

import com.househost.finance.cashier.application.port.in.CashierFinancialTransactionUseCase;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import org.springframework.stereotype.Service;

@Service
public class CashierTransactionParticipantService implements CashierFinancialTransactionUseCase {

    private final CashierMovementService cashierMovementService;

    CashierTransactionParticipantService(CashierMovementService cashierMovementService) {
        this.cashierMovementService = cashierMovementService;
    }

    @Override
    public void registerTransaction(Long cashierId, FinancialTransaction transaction) {
        if (transaction instanceof InstallmentPlanTransaction installmentPlanTransaction) {
            installmentPlanTransaction.getInstallments().forEach(
                    installmentTransaction -> registerSingleTransaction(cashierId, installmentTransaction)
            );
            return;
        }
        registerSingleTransaction(cashierId, transaction);
    }

    private void registerSingleTransaction(Long cashierId, FinancialTransaction transaction) {
        if (transaction.getReceiverType() == FinancialPartyType.CASHIER
                && cashierId.equals(transaction.getReceiverId())) {
            cashierMovementService.scheduleDeposit(cashierId, transaction);
        }
        if (transaction.getSenderType() == FinancialPartyType.CASHIER
                && cashierId.equals(transaction.getSenderId())) {
            cashierMovementService.scheduleWithdrawal(cashierId, transaction);
        }
    }

    @Override
    public void settleTransaction(Long cashierId, FinancialTransaction transaction) {
        if (transaction.getReceiverType() == FinancialPartyType.CASHIER
                && cashierId.equals(transaction.getReceiverId())) {
            cashierMovementService.settleDeposit(cashierId, transaction);
        }
        if (transaction.getSenderType() == FinancialPartyType.CASHIER
                && cashierId.equals(transaction.getSenderId())) {
            cashierMovementService.settleWithdrawal(cashierId, transaction);
        }
    }

    @Override
    public void reverseTransaction(FinancialTransaction transaction) {
        if (transaction instanceof InstallmentPlanTransaction installmentPlanTransaction) {
            installmentPlanTransaction.getInstallments().forEach(
                    installmentTransaction -> cashierMovementService.reverseMovementsForTransaction(
                            installmentTransaction.getId()
                    )
            );
            return;
        }
        cashierMovementService.reverseMovementsForTransaction(transaction.getId());
    }
}
