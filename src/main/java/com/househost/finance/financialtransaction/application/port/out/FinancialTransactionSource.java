package com.househost.finance.financialtransaction.application.port.out;

import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;

public interface FinancialTransactionSource {

    FinancialTransactionSourceType getType();

    default void onCreate(Long sourceId, FinancialTransaction transaction) {
    }

    void onSettle(Long sourceId, FinancialTransaction transaction);

    default void onDelete(Long sourceId, FinancialTransaction transaction) {
    }
}
