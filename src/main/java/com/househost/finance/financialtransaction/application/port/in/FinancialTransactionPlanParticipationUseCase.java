package com.househost.finance.financialtransaction.application.port.in;

import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;

public interface FinancialTransactionPlanParticipationUseCase {

    void attach(Long planId, FinancialTransaction financialTransaction);

    void detach(Long planId, FinancialTransaction financialTransaction);

    void refreshSettlement(Long planId);
}
