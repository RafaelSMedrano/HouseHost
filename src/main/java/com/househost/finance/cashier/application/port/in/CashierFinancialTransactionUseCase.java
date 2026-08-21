package com.househost.finance.cashier.application.port.in;

import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;

public interface CashierFinancialTransactionUseCase {

    void registerTransaction(Long cashierId, FinancialTransaction transaction);

    void settleTransaction(Long cashierId, FinancialTransaction transaction);

    void reverseTransaction(FinancialTransaction transaction);
}

