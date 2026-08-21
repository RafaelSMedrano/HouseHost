package com.househost.finance.financialtransaction.application.dto;

import com.househost.finance.financialtransaction.domain.model.FinancialTransactionPlan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FinancialTransactionPlanSummaryDTO {

    private final Long id;
    private final Long bookingId;
    private final BigDecimal totalAmount;
    private final String status;
    private final LocalDate planDueDate;
    private final LocalDate planSettlementDate;
    private final BigDecimal settledAmount;
    private final BigDecimal outstandingAmount;
    private final List<FinancialTransactionPlanComponentSummaryDTO> componentSummaryDTOList;

    public FinancialTransactionPlanSummaryDTO(FinancialTransactionPlan financialTransactionPlan) {
        id = financialTransactionPlan.getId();
        bookingId = financialTransactionPlan.getSourceId();
        totalAmount = financialTransactionPlan.getTotalAmount();
        status = financialTransactionPlan.getStatus().name();
        planDueDate = financialTransactionPlan.getPlanDueDate();
        planSettlementDate = financialTransactionPlan.getPlanSettlementDate();
        settledAmount = financialTransactionPlan.calculateSettledAmount();
        outstandingAmount = financialTransactionPlan.calculateOutstandingAmount();
        componentSummaryDTOList = financialTransactionPlan.getFinancialTransactionList().stream()
                .map(FinancialTransactionPlanComponentSummaryDTO::new)
                .toList();
    }

    public Long getId() {
        return id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getPlanDueDate() {
        return planDueDate;
    }

    public LocalDate getPlanSettlementDate() {
        return planSettlementDate;
    }

    public BigDecimal getSettledAmount() {
        return settledAmount;
    }

    public BigDecimal getOutstandingAmount() {
        return outstandingAmount;
    }

    public List<FinancialTransactionPlanComponentSummaryDTO> getComponentSummaryDTOList() {
        return componentSummaryDTOList;
    }
}
