package com.househost.finance.financialtransaction.adapter.out.integration;

import com.househost.finance.cashier.application.port.in.CashierFinancialTransactionUseCase;
import com.househost.finance.financialtransaction.application.port.out.FinancialParty;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import org.springframework.stereotype.Component;

@Component
public class CashierFinancialPartyAdapter implements FinancialParty {

    private static final FinancialPartyType TYPE = FinancialPartyType.CASHIER;

    private final CashierFinancialTransactionUseCase cashierFinancialTransactionUseCase;

    public CashierFinancialPartyAdapter(
            CashierFinancialTransactionUseCase cashierFinancialTransactionUseCase
    ) {
        this.cashierFinancialTransactionUseCase = cashierFinancialTransactionUseCase;
    }

    @Override
    public FinancialPartyType getType() {
        return TYPE;
    }

    @Override
    public void onCreate(Long partyId, FinancialTransaction transaction) {
        cashierFinancialTransactionUseCase.registerTransaction(partyId, transaction);
    }

    @Override
    public void onSettle(Long partyId, FinancialTransaction transaction) {
        cashierFinancialTransactionUseCase.settleTransaction(partyId, transaction);
    }

    @Override
    public void onDelete(FinancialTransaction transaction) {
        cashierFinancialTransactionUseCase.reverseTransaction(transaction);
    }
}

