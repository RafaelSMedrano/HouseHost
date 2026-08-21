package com.househost.booking.checkout.application.service;

import com.househost.booking.checkout.application.dto.CheckOutRatingRequestDTO;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.booking.checkout.domain.model.CheckOutStatus;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanMaterializationDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanReplacementOutcomeDTO;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CheckOutParticipantNotifier {

    private final CheckOutBookingResolver checkOutBookingResolver;
    private final CheckOutRoomResolver checkOutRoomResolver;
    private final CheckOutGuestResolver checkOutGuestResolver;
    private final CheckOutRatingResolver checkOutRatingResolver;
    private final CheckOutFinancialResolver checkOutFinancialResolver;

    public CheckOutParticipantNotifier(
            CheckOutBookingResolver checkOutBookingResolver,
            CheckOutRoomResolver checkOutRoomResolver,
            CheckOutGuestResolver checkOutGuestResolver,
            CheckOutRatingResolver checkOutRatingResolver,
            CheckOutFinancialResolver checkOutFinancialResolver
    ) {
        this.checkOutBookingResolver = checkOutBookingResolver;
        this.checkOutRoomResolver = checkOutRoomResolver;
        this.checkOutGuestResolver = checkOutGuestResolver;
        this.checkOutRatingResolver = checkOutRatingResolver;
        this.checkOutFinancialResolver = checkOutFinancialResolver;
    }

    public void notifyCompletion(
            CheckOut checkOut,
            CheckOutRatingRequestDTO checkOutRatingRequestDTO
    ) {
        notifyCompletion(checkOut, checkOutRatingRequestDTO, null);
    }

    public Optional<FinancialTransactionPlanReplacementOutcomeDTO> notifyCompletion(
            CheckOut checkOut,
            CheckOutRatingRequestDTO checkOutRatingRequestDTO,
            FinancialTransactionPlanMaterializationDTO
                    financialTransactionPlanMaterializationDTO
    ) {
        if (checkOut.getStatus() != CheckOutStatus.COMPLETED) {
            return Optional.empty();
        }
        Optional<FinancialTransactionPlanReplacementOutcomeDTO>
                paymentMaterializationOutcomeDTOOptional =
                checkOutFinancialResolver.resolvePayment(
                        checkOut,
                        financialTransactionPlanMaterializationDTO
                );
        checkOutBookingResolver.resolveBookingStatus(checkOut.getBooking());
        checkOutRoomResolver.resolveRoomStatus(
                checkOut.getRoom(),
                checkOut.isRoomInspected()
        );
        if (checkOut.shouldApplyGuestHistory()) {
            checkOutGuestResolver.resolveGuestHistory(checkOut);
            checkOutRatingResolver.resolveRating(checkOut, checkOutRatingRequestDTO);
        }
        return paymentMaterializationOutcomeDTOOptional;
    }
}
