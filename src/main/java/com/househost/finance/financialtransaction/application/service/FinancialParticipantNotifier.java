package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import org.springframework.stereotype.Component;

@Component
public class FinancialParticipantNotifier {
    private final FinancialPartyResolver partyResolver;

    public FinancialParticipantNotifier(FinancialPartyResolver partyResolver) {
        this.partyResolver = partyResolver;
    }

    public void notifyCreation(FinancialTransaction transaction) {
        partyResolver.resolve(transaction.getSenderType())
                .onCreate(transaction.getSenderId(), transaction);
        partyResolver.resolve(transaction.getReceiverType())
                .onCreate(transaction.getReceiverId(), transaction);
    }

    public void notifySettlement(FinancialTransaction transaction) {
        partyResolver.resolve(transaction.getSenderType())
                .onSettle(transaction.getSenderId(), transaction);
        partyResolver.resolve(transaction.getReceiverType())
                .onSettle(transaction.getReceiverId(), transaction);
    }

    public void notifyDeletion(FinancialTransaction transaction) {
        partyResolver.resolve(transaction.getSenderType()).onDelete(transaction);
        if (transaction.getSenderType() != transaction.getReceiverType()) {
            partyResolver.resolve(transaction.getReceiverType()).onDelete(transaction);
        }
    }
}
