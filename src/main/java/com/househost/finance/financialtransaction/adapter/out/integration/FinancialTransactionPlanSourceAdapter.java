package com.househost.finance.financialtransaction.adapter.out.integration;

import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionPlanParticipationUseCase;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionSource;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import org.springframework.stereotype.Component;

@Component
public class FinancialTransactionPlanSourceAdapter implements FinancialTransactionSource {

    private final FinancialTransactionPlanParticipationUseCase financialTransactionPlanParticipationUseCase;

    public FinancialTransactionPlanSourceAdapter(
            FinancialTransactionPlanParticipationUseCase financialTransactionPlanParticipationUseCase
    ) {
        this.financialTransactionPlanParticipationUseCase =
                financialTransactionPlanParticipationUseCase;
    }

    @Override
    public FinancialTransactionSourceType getType() {
        return FinancialTransactionSourceType.PLAN;
    }

    @Override
    public void onCreate(Long sourceId, FinancialTransaction transaction) {
        financialTransactionPlanParticipationUseCase.attach(sourceId, transaction);
    }

    @Override
    public void onSettle(Long sourceId, FinancialTransaction transaction) {
        financialTransactionPlanParticipationUseCase.refreshSettlement(sourceId);
    }

    @Override
    public void onDelete(Long sourceId, FinancialTransaction transaction) {
        financialTransactionPlanParticipationUseCase.detach(sourceId, transaction);
    }
}
