package com.househost.finance.financialtransaction.application.records;

import com.househost.finance.financialtransaction.domain.model.FinancialPaymentStructure;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;

public record FinancialTransactionPlanMaterializationCommandRecord(
        Long bookingId,
        FinancialTransactionType purpose,
        boolean materializationRequested,
        FinancialPaymentStructure structure,
        FinancialTransactionMethod method,
        Integer installmentsQuantity,
        String idempotencyKey
) {
}
