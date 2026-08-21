package com.househost.finance.financialtransaction.application.records;

import com.househost.finance.financialtransaction.domain.model.FinancialPaymentStructure;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;

public record FinancialTransactionPlanReplacementCommandRecord(
        Long planId,
        FinancialTransactionType purpose,
        Long scheduledFinancialTransactionId,
        FinancialPaymentStructure structure,
        FinancialTransactionMethod method,
        Integer installmentsQuantity,
        String idempotencyKey
) {
}
