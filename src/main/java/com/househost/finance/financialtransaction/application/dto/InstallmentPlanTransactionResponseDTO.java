package com.househost.finance.financialtransaction.application.dto;

import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;

import java.util.List;

public class InstallmentPlanTransactionResponseDTO {
    private final FinancialTransactionResponseDTO plan;
    private final List<FinancialTransactionResponseDTO> installments;

    public InstallmentPlanTransactionResponseDTO(InstallmentPlanTransaction plan) {
        this.plan = new FinancialTransactionResponseDTO(plan);
        this.installments = plan.getInstallments().stream()
                .map(FinancialTransactionResponseDTO::new)
                .toList();
    }

    public FinancialTransactionResponseDTO getPlan() {
        return plan;
    }

    public List<FinancialTransactionResponseDTO> getInstallments() {
        return installments;
    }
}
