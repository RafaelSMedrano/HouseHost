package com.househost.finance.financialtransaction.application.port.out;

import com.househost.finance.financialtransaction.domain.model.FinancialTransactionPlan;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;

import java.util.Optional;

public interface FinancialTransactionPlanPersistencePort {

    FinancialTransactionPlan save(FinancialTransactionPlan financialTransactionPlan);

    Optional<FinancialTransactionPlan> findById(Long id);

    Optional<FinancialTransactionPlan> findByIdForUpdate(Long id);

    Optional<FinancialTransactionPlan> findBySource(
            FinancialTransactionSourceType sourceType,
            Long sourceId
    );

    Optional<FinancialTransactionPlan> findBySourceForUpdate(
            FinancialTransactionSourceType sourceType,
            Long sourceId
    );

    void delete(FinancialTransactionPlan financialTransactionPlan);
}
