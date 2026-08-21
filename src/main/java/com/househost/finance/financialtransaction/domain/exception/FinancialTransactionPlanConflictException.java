package com.househost.finance.financialtransaction.domain.exception;

import com.househost.shared.exception.FinanceException;

public class FinancialTransactionPlanConflictException extends FinanceException {

    public FinancialTransactionPlanConflictException(String message) {
        super(message);
    }
}
