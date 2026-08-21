package com.househost.finance.financialtransaction.application.dto;

public class FinancialTransactionPlanCreationOutcomeDTO {

    private final Long bookingId;
    private final FinancialTransactionPlanSummaryDTO financialTransactionPlanSummaryDTO;

    public FinancialTransactionPlanCreationOutcomeDTO(
            Long bookingId,
            FinancialTransactionPlanSummaryDTO financialTransactionPlanSummaryDTO
    ) {
        this.bookingId = bookingId;
        this.financialTransactionPlanSummaryDTO = financialTransactionPlanSummaryDTO;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public FinancialTransactionPlanSummaryDTO getFinancialTransactionPlanSummaryDTO() {
        return financialTransactionPlanSummaryDTO;
    }
}
