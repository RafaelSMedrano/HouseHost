package com.househost.finance.financialtransaction.application.records;

import com.househost.finance.financialtransaction.domain.model.FinancialPaymentStructure;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservationFinancialTransactionPlanCommandRecord(
        Long bookingId,
        Long guestId,
        BigDecimal bookingTotalAmount,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        String idempotencyKey,
        CurrentPaymentAllocationRecord currentPaymentAllocationRecord,
        DownPaymentAllocationRecord downPaymentAllocationRecord,
        FuturePaymentAllocationRecord checkInPaymentAllocationRecord,
        FuturePaymentAllocationRecord checkOutPaymentAllocationRecord
) {

    public record CurrentPaymentAllocationRecord(
            boolean enabled,
            BigDecimal amount,
            FinancialTransactionMethod method,
            Integer installmentsQuantity,
            boolean received
    ) {
    }

    public record DownPaymentAllocationRecord(
            boolean enabled,
            BigDecimal amount,
            boolean received,
            FinancialTransactionMethod method,
            FinancialPaymentStructure structure,
            Integer installmentsQuantity,
            Integer installmentDueDay,
            LocalDate paymentDate
    ) {
    }

    public record FuturePaymentAllocationRecord(
            boolean enabled,
            BigDecimal amount,
            boolean received
    ) {
    }
}
