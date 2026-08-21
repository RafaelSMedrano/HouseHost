package com.househost.booking.booking.application.dto;

import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanSummaryDTO;

public class BookingFormCreateResponseDTO {

    private final BookingResponseDTO booking;
    private final FinancialTransactionPlanSummaryDTO financialTransactionPlan;
    private final boolean idempotentReplay;

    public BookingFormCreateResponseDTO(
            BookingResponseDTO booking,
            FinancialTransactionPlanSummaryDTO financialTransactionPlan,
            boolean idempotentReplay
    ) {
        this.booking = booking;
        this.financialTransactionPlan = financialTransactionPlan;
        this.idempotentReplay = idempotentReplay;
    }

    public BookingResponseDTO getBooking() {
        return booking;
    }

    public FinancialTransactionPlanSummaryDTO getFinancialTransactionPlan() {
        return financialTransactionPlan;
    }

    public boolean isIdempotentReplay() {
        return idempotentReplay;
    }
}
