package com.househost.finance.financialtransaction.application.port.out;

import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;

public interface FinancialTransactionSource {

    FinancialTransactionSourceType getType();

    void onSettle(Long sourceId, FinancialTransaction transaction);
}
