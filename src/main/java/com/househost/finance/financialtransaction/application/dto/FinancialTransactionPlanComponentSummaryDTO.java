package com.househost.finance.financialtransaction.application.dto;

import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FinancialTransactionPlanComponentSummaryDTO {

    private final Long id;
    private final String type;
    private final BigDecimal amount;
    private final String status;
    private final LocalDate dueDate;
    private final String structure;
    private final Integer installmentsQuantity;

    public FinancialTransactionPlanComponentSummaryDTO(
            FinancialTransaction financialTransaction
    ) {
        id = financialTransaction.getId();
        type = financialTransaction.getType().name();
        amount = financialTransaction.getAmount();
        status = financialTransaction.getStatus().name();
        dueDate = financialTransaction.getDueDate();
        structure = financialTransaction instanceof InstallmentPlanTransaction
                ? "INSTALLMENT"
                : "SIMPLE";
        installmentsQuantity = financialTransaction
                instanceof InstallmentPlanTransaction installmentPlanTransaction
                ? installmentPlanTransaction.getInstallmentsQuantity()
                : null;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getStructure() {
        return structure;
    }

    public Integer getInstallmentsQuantity() {
        return installmentsQuantity;
    }
}
