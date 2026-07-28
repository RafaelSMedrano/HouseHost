package com.househost.finance.cashier.application.service;

import com.househost.finance.financialtransaction.application.port.out.FinancialParty;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import org.springframework.stereotype.Service;

@Service
public class CashierTransactionParticipantService implements FinancialParty {

    private final CashierMovementService movementService;

    CashierTransactionParticipantService(CashierMovementService movementService) {
        this.movementService = movementService;
    }

    @Override
    public FinancialPartyType getType() {
        return FinancialPartyType.CASHIER;
    }

    @Override
    public void onCreate(Long cashierId, FinancialTransaction transaction) {
        if (transaction instanceof InstallmentPlanTransaction plan) {
            plan.getInstallments().forEach(installment -> onCreateSingleTransaction(cashierId, installment));
            return;
        }
        onCreateSingleTransaction(cashierId, transaction);
    }

    private void onCreateSingleTransaction(Long cashierId, FinancialTransaction transaction) {
        if (transaction.getReceiverType() == FinancialPartyType.CASHIER
                && cashierId.equals(transaction.getReceiverId())) {
            movementService.scheduleDeposit(cashierId, transaction);
        }
        if (transaction.getSenderType() == FinancialPartyType.CASHIER
                && cashierId.equals(transaction.getSenderId())) {
            movementService.scheduleWithdrawal(cashierId, transaction);
        }
    }

    @Override
    public void onSettle(Long cashierId, FinancialTransaction transaction) {
        if (transaction.getReceiverType() == FinancialPartyType.CASHIER
                && cashierId.equals(transaction.getReceiverId())) {
            movementService.settleDeposit(cashierId, transaction);
        }
        if (transaction.getSenderType() == FinancialPartyType.CASHIER
                && cashierId.equals(transaction.getSenderId())) {
            movementService.settleWithdrawal(cashierId, transaction);
        }
    }

    @Override
    public void onDelete(FinancialTransaction transaction) {
        if (transaction instanceof InstallmentPlanTransaction plan) {
            plan.getInstallments().forEach(
                    installment -> movementService.reverseMovementsForTransaction(installment.getId())
            );
            return;
        }
        movementService.reverseMovementsForTransaction(transaction.getId());
    }
}
