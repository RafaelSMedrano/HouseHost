package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionPlanParticipationUseCase;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionPlanPersistencePort;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionPlan;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class FinancialTransactionPlanParticipationService
        implements FinancialTransactionPlanParticipationUseCase {

    private final FinancialTransactionPlanPersistencePort financialTransactionPlanPersistencePort;
    private final FinancialTransactionPlanValidationService financialTransactionPlanValidationService;

    public FinancialTransactionPlanParticipationService(
            FinancialTransactionPlanPersistencePort financialTransactionPlanPersistencePort,
            FinancialTransactionPlanValidationService financialTransactionPlanValidationService
    ) {
        this.financialTransactionPlanPersistencePort = financialTransactionPlanPersistencePort;
        this.financialTransactionPlanValidationService = financialTransactionPlanValidationService;
    }

    @Override
    @Transactional
    public void attach(Long planId, FinancialTransaction financialTransaction) {
        financialTransactionPlanValidationService.validatePlanId(planId);
        if (financialTransaction == null || financialTransaction.getId() == null) {
            throw new FinanceException("Transacao persistida e obrigatoria para associacao ao plano.");
        }
        FinancialTransactionPlan financialTransactionPlan = findRequiredPlanForUpdate(planId);
        if (financialTransactionPlan.containsFinancialTransaction(financialTransaction.getId())) {
            return;
        }
        financialTransactionPlan.addFinancialTransaction(financialTransaction);
        financialTransactionPlanPersistencePort.save(financialTransactionPlan);
    }

    @Override
    @Transactional
    public void detach(Long planId, FinancialTransaction financialTransaction) {
        financialTransactionPlanValidationService.validatePlanId(planId);
        if (financialTransaction == null || financialTransaction.getId() == null) {
            throw new FinanceException("Transacao persistida e obrigatoria para remocao do plano.");
        }
        FinancialTransactionPlan financialTransactionPlan = findRequiredPlanForUpdate(planId);
        if (!financialTransactionPlan.containsFinancialTransaction(financialTransaction.getId())) {
            return;
        }
        financialTransactionPlan.removeFinancialTransaction(financialTransaction.getId());
        financialTransactionPlanPersistencePort.save(financialTransactionPlan);
    }

    @Override
    @Transactional
    public void refreshSettlement(Long planId) {
        financialTransactionPlanValidationService.validatePlanId(planId);
        FinancialTransactionPlan financialTransactionPlan = findRequiredPlanForUpdate(planId);
        financialTransactionPlan.refreshDerivedState(LocalDate.now());
        financialTransactionPlanPersistencePort.save(financialTransactionPlan);
    }

    private FinancialTransactionPlan findRequiredPlanForUpdate(Long planId) {
        return financialTransactionPlanPersistencePort.findByIdForUpdate(planId)
                .orElseThrow(() -> new FinanceException("Plano financeiro nao encontrado."));
    }
}
