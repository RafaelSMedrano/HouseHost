package com.househost.finance.financialtransaction.application.port.in;

import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanComponentSummaryDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanCreationOutcomeDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanProfileDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanSummaryDTO;
import com.househost.finance.financialtransaction.application.records.ReservationFinancialTransactionPlanCommandRecord;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;

import java.time.LocalDate;
import java.util.Optional;

public interface FinancialTransactionPlanUseCase {

    Optional<FinancialTransactionPlanCreationOutcomeDTO> prepareReservationCreation(
            String idempotencyKey
    );

    FinancialTransactionPlanSummaryDTO createForReservation(
            ReservationFinancialTransactionPlanCommandRecord reservationFinancialTransactionPlanCommandRecord
    );

    FinancialTransactionPlanCreationOutcomeDTO reconcileReservationCreation(
            String idempotencyKey
    );

    FinancialTransactionPlanSummaryDTO findByBookingId(Long bookingId);

    FinancialTransactionPlanComponentSummaryDTO findScheduledComponent(
            Long planId,
            FinancialTransactionType purpose
    );

    FinancialTransactionPlanProfileDTO findProfile(Long planId);

    FinancialTransactionPlanProfileDTO extendDeadline(Long planId, LocalDate planDueDate);

    FinancialTransactionPlanProfileDTO cancel(Long planId);

    void delete(Long planId);
}
