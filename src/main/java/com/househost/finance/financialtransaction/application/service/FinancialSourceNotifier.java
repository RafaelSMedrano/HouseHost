package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import org.springframework.stereotype.Component;

@Component
public class FinancialSourceNotifier {
    private final FinancialTransactionSourceResolver sourceResolver;

    public FinancialSourceNotifier(FinancialTransactionSourceResolver sourceResolver) {
        this.sourceResolver = sourceResolver;
    }

    public void notifySettlement(FinancialTransaction transaction) {
        if (transaction.getSourceType() == null || transaction.getSourceId() == null) {
            return;
        }
        sourceResolver.resolve(transaction.getSourceType())
                .onSettle(transaction.getSourceId(), transaction);
    }
}
