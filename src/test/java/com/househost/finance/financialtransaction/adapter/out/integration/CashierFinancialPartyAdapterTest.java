package com.househost.finance.financialtransaction.adapter.out.integration;

import com.househost.finance.cashier.application.port.in.CashierFinancialTransactionUseCase;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CashierFinancialPartyAdapterTest {

    @Test
    void translatesFinancialPartyCallbacksToCashierUseCase() {
        CashierFinancialTransactionUseCase cashierFinancialTransactionUseCase =
                mock(CashierFinancialTransactionUseCase.class);
        CashierFinancialPartyAdapter cashierFinancialPartyAdapter = new CashierFinancialPartyAdapter(
                cashierFinancialTransactionUseCase
        );
        FinancialTransaction transaction = mock(FinancialTransaction.class);

        cashierFinancialPartyAdapter.onCreate(1L, transaction);
        cashierFinancialPartyAdapter.onSettle(1L, transaction);
        cashierFinancialPartyAdapter.onDelete(transaction);

        assertEquals(FinancialPartyType.CASHIER, cashierFinancialPartyAdapter.getType());
        verify(cashierFinancialTransactionUseCase).registerTransaction(1L, transaction);
        verify(cashierFinancialTransactionUseCase).settleTransaction(1L, transaction);
        verify(cashierFinancialTransactionUseCase).reverseTransaction(transaction);
    }
}

