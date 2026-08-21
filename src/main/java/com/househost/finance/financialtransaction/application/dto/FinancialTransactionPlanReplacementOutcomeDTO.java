package com.househost.finance.financialtransaction.application.dto;

import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionPlan;

public class FinancialTransactionPlanReplacementOutcomeDTO {

    private final FinancialTransactionPlanComponentSummaryDTO definitiveComponent;
    private final FinancialTransactionPlanSummaryDTO financialTransactionPlan;
    private final boolean idempotentReplay;

    public FinancialTransactionPlanReplacementOutcomeDTO(
            FinancialTransaction definitiveFinancialTransaction,
            FinancialTransactionPlan financialTransactionPlan,
            boolean idempotentReplay
    ) {
        definitiveComponent = new FinancialTransactionPlanComponentSummaryDTO(
                definitiveFinancialTransaction
        );
        this.financialTransactionPlan = new FinancialTransactionPlanSummaryDTO(
                financialTransactionPlan
        );
        this.idempotentReplay = idempotentReplay;
    }

    public FinancialTransactionPlanComponentSummaryDTO getDefinitiveComponent() {
        return definitiveComponent;
    }

    public FinancialTransactionPlanSummaryDTO getFinancialTransactionPlan() {
        return financialTransactionPlan;
    }

    public boolean isIdempotentReplay() {
        return idempotentReplay;
    }
}
