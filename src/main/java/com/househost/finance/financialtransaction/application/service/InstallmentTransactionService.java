package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionPersistencePort;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentTransactionStatus;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Service;

@Service
class InstallmentTransactionService {
    private final FinancialTransactionPersistencePort transactionPersistence;

    InstallmentTransactionService(FinancialTransactionPersistencePort transactionPersistence) {
        this.transactionPersistence = transactionPersistence;
    }

    SettlementResult settle(Long planId, Integer installmentNumber) {
        InstallmentPlanTransaction plan = findPlan(planId);
        InstallmentTransaction installment;
        try {
            installment = plan.findInstallment(installmentNumber);
        } catch (IllegalArgumentException exception) {
            throw new FinanceException(exception.getMessage());
        }

        if (installment.getInstallmentStatus() == InstallmentTransactionStatus.SETTLED) {
            return new SettlementResult(plan, false);
        }

        installment.settle();
        plan.refreshStatus();
        return new SettlementResult(plan, true);
    }

    private InstallmentPlanTransaction findPlan(Long planId) {
        if (planId == null) {
            throw new FinanceException("Plano parcelado nao encontrado.");
        }
        return transactionPersistence.findById(planId)
                .filter(InstallmentPlanTransaction.class::isInstance)
                .map(InstallmentPlanTransaction.class::cast)
                .orElseThrow(() -> new FinanceException("Plano parcelado nao encontrado."));
    }

    record SettlementResult(InstallmentPlanTransaction plan, boolean changed) {
    }
}
