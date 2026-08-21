package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.records.ReservationFinancialTransactionPlanCommandRecord;
import com.househost.finance.financialtransaction.domain.model.FinancialPaymentStructure;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.shared.exception.FinanceException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinancialTransactionPlanValidationServiceTest {

    private final FinancialTransactionPlanValidationService financialTransactionPlanValidationService =
            new FinancialTransactionPlanValidationService();

    @Test
    void acceptsExactExplicitAllocation() {
        assertDoesNotThrow(() -> financialTransactionPlanValidationService
                .validateReservationCommand(command(
                        new BigDecimal("200.00"),
                        new BigDecimal("300.00"),
                        new BigDecimal("500.00")
                )));
    }

    @Test
    void rejectsIncompleteAndExcessiveAllocation() {
        assertThrows(FinanceException.class, () -> financialTransactionPlanValidationService
                .validateReservationCommand(command(
                        new BigDecimal("100.00"),
                        new BigDecimal("300.00"),
                        new BigDecimal("500.00")
                )));
        assertThrows(FinanceException.class, () -> financialTransactionPlanValidationService
                .validateReservationCommand(command(
                        new BigDecimal("300.00"),
                        new BigDecimal("300.00"),
                        new BigDecimal("500.00")
                )));
    }

    @Test
    void rejectsHiddenAmountForDisabledPurpose() {
        ReservationFinancialTransactionPlanCommandRecord commandRecord = command(
                new BigDecimal("200.00"),
                new BigDecimal("300.00"),
                new BigDecimal("500.00")
        );
        ReservationFinancialTransactionPlanCommandRecord invalidCommandRecord =
                new ReservationFinancialTransactionPlanCommandRecord(
                        commandRecord.bookingId(),
                        commandRecord.guestId(),
                        commandRecord.bookingTotalAmount(),
                        commandRecord.checkInDate(),
                        commandRecord.checkOutDate(),
                        commandRecord.idempotencyKey(),
                        commandRecord.currentPaymentAllocationRecord(),
                        commandRecord.downPaymentAllocationRecord(),
                        new ReservationFinancialTransactionPlanCommandRecord
                                .FuturePaymentAllocationRecord(false, new BigDecimal("300.00"), false),
                        commandRecord.checkOutPaymentAllocationRecord()
                );

        assertThrows(FinanceException.class, () -> financialTransactionPlanValidationService
                .validateReservationCommand(invalidCommandRecord));
    }

    @Test
    void enforcesInstallmentAndDeadlineBoundaries() {
        LocalDate checkInDate = LocalDate.now().plusMonths(1);
        LocalDate checkOutDate = LocalDate.now().plusMonths(2);
        ReservationFinancialTransactionPlanCommandRecord invalidCommandRecord =
                new ReservationFinancialTransactionPlanCommandRecord(
                        40L,
                        7L,
                        new BigDecimal("1000.00"),
                        checkInDate,
                        checkOutDate,
                        "reservation-40",
                        null,
                        new ReservationFinancialTransactionPlanCommandRecord
                                .DownPaymentAllocationRecord(
                                true,
                                new BigDecimal("1000.00"),
                                false,
                                FinancialTransactionMethod.PIX,
                                FinancialPaymentStructure.INSTALLMENT,
                                4,
                                10,
                                LocalDate.now()
                        ),
                        null,
                        null
                );

        assertThrows(FinanceException.class, () -> financialTransactionPlanValidationService
                .validateReservationCommand(invalidCommandRecord));
    }

    private ReservationFinancialTransactionPlanCommandRecord command(
            BigDecimal downPaymentAmount,
            BigDecimal checkInAmount,
            BigDecimal checkOutAmount
    ) {
        return new ReservationFinancialTransactionPlanCommandRecord(
                40L,
                7L,
                new BigDecimal("1000.00"),
                LocalDate.now().plusMonths(2),
                LocalDate.now().plusMonths(4),
                "reservation-40",
                null,
                new ReservationFinancialTransactionPlanCommandRecord.DownPaymentAllocationRecord(
                        true,
                        downPaymentAmount,
                        true,
                        FinancialTransactionMethod.PIX,
                        FinancialPaymentStructure.SIMPLE,
                        null,
                        null,
                        LocalDate.now()
                ),
                new ReservationFinancialTransactionPlanCommandRecord.FuturePaymentAllocationRecord(
                        true,
                        checkInAmount,
                        true
                ),
                new ReservationFinancialTransactionPlanCommandRecord.FuturePaymentAllocationRecord(
                        true,
                        checkOutAmount,
                        true
                )
        );
    }
}
