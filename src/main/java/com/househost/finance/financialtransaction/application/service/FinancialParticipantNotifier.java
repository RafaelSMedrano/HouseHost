package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import org.springframework.stereotype.Component;

@Component
public class FinancialParticipantNotifier {
    private final FinancialPartyResolver financialPartyResolver;
    private final FinancialTransactionSourceResolver financialTransactionSourceResolver;

    public FinancialParticipantNotifier(
            FinancialPartyResolver financialPartyResolver,
            FinancialTransactionSourceResolver financialTransactionSourceResolver
    ) {
        this.financialPartyResolver = financialPartyResolver;
        this.financialTransactionSourceResolver = financialTransactionSourceResolver;
    }

    public void notifyCreation(FinancialTransaction transaction) {
        if (transaction instanceof InstallmentPlanTransaction installmentPlanTransaction) {
            installmentPlanTransaction.getInstallments().forEach(this::notifyParticipantCreation);
        } else {
            notifyParticipantCreation(transaction);
        }
        notifySourceCreation(transaction);
    }

    public void notifyParticipantDeletion(FinancialTransaction transaction) {
        if (transaction instanceof InstallmentPlanTransaction installmentPlanTransaction) {
            installmentPlanTransaction.getInstallments().forEach(this::notifyParticipantDeletion);
            return;
        }
        financialPartyResolver.resolve(transaction.getSenderType()).onDelete(transaction);
        if (transaction.getSenderType() != transaction.getReceiverType()) {
            financialPartyResolver.resolve(transaction.getReceiverType()).onDelete(transaction);
        }
    }

    private void notifyParticipantCreation(FinancialTransaction transaction) {
        financialPartyResolver.resolve(transaction.getSenderType())
                .onCreate(transaction.getSenderId(), transaction);
        financialPartyResolver.resolve(transaction.getReceiverType())
                .onCreate(transaction.getReceiverId(), transaction);
    }

    public void notifySettlement(FinancialTransaction transaction) {
        if (transaction instanceof InstallmentPlanTransaction installmentPlanTransaction) {
            installmentPlanTransaction.getInstallments().forEach(this::notifyParticipantSettlement);
        } else {
            notifyParticipantSettlement(transaction);
        }
        notifySourceSettlement(transaction);
    }

    public void notifyInstallmentSettlement(FinancialTransaction installmentTransaction) {
        notifyParticipantSettlement(installmentTransaction);
    }

    public void notifySourceSettlementOnly(FinancialTransaction transaction) {
        notifySourceSettlement(transaction);
    }

    public void notifyDeletion(FinancialTransaction transaction) {
        notifyParticipantDeletion(transaction);
        notifySourceDeletion(transaction);
    }

    public void notifySourceDeletionOnly(FinancialTransaction transaction) {
        notifySourceDeletion(transaction);
    }

    private void notifySourceCreation(FinancialTransaction transaction) {
        if (transaction.getSourceType() == null || transaction.getSourceId() == null) {
            return;
        }

        financialTransactionSourceResolver.resolve(transaction.getSourceType())
                .onCreate(transaction.getSourceId(), transaction);
    }

    private void notifyParticipantSettlement(FinancialTransaction transaction) {
        financialPartyResolver.resolve(transaction.getSenderType())
                .onSettle(transaction.getSenderId(), transaction);
        financialPartyResolver.resolve(transaction.getReceiverType())
                .onSettle(transaction.getReceiverId(), transaction);
    }

    private void notifySourceSettlement(FinancialTransaction transaction) {
        if (transaction.getSourceType() == null || transaction.getSourceId() == null) {
            return;
        }

        financialTransactionSourceResolver.resolve(transaction.getSourceType())
                .onSettle(transaction.getSourceId(), transaction);
    }

    private void notifySourceDeletion(FinancialTransaction transaction) {
        if (transaction.getSourceType() == null || transaction.getSourceId() == null) {
            return;
        }

        financialTransactionSourceResolver.resolve(transaction.getSourceType())
                .onDelete(transaction.getSourceId(), transaction);
    }
}
