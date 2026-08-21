package com.househost.booking.checking.application.service;

import com.househost.booking.checking.domain.model.CheckIn;
import com.househost.booking.checking.domain.model.CheckInStatus;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanMaterializationDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanReplacementOutcomeDTO;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CheckInParticipantNotifier {

    private final CheckInBookingResolver checkInBookingResolver;
    private final CheckInGuestResolver checkInGuestResolver;
    private final CheckInRoomResolver checkInRoomResolver;
    private final CheckInFinancialResolver checkInFinancialResolver;

    public CheckInParticipantNotifier(
            CheckInBookingResolver checkInBookingResolver,
            CheckInGuestResolver checkInGuestResolver,
            CheckInRoomResolver checkInRoomResolver,
            CheckInFinancialResolver checkInFinancialResolver
    ) {
        this.checkInBookingResolver = checkInBookingResolver;
        this.checkInGuestResolver = checkInGuestResolver;
        this.checkInRoomResolver = checkInRoomResolver;
        this.checkInFinancialResolver = checkInFinancialResolver;
    }

    public void notifyCompletion(CheckIn checkIn) {
        notifyCompletion(checkIn, null);
    }

    public Optional<FinancialTransactionPlanReplacementOutcomeDTO> notifyCompletion(
            CheckIn checkIn,
            FinancialTransactionPlanMaterializationDTO
                    financialTransactionPlanMaterializationDTO
    ) {
        if (checkIn.getStatus() != CheckInStatus.COMPLETED) {
            return Optional.empty();
        }
        Optional<FinancialTransactionPlanReplacementOutcomeDTO>
                paymentMaterializationOutcomeDTOOptional =
                checkIn.getBooking() == null
                        ? Optional.empty()
                        : checkInFinancialResolver.resolvePayment(
                                checkIn,
                                financialTransactionPlanMaterializationDTO
                        );
        if (checkIn.getBooking() == null) {
            checkInGuestResolver.resolveGuestStatus(checkIn.getGuest());
        } else {
            checkInBookingResolver.resolveBookingStatus(checkIn.getBooking());
        }
        checkInRoomResolver.resolveRoomStatus(checkIn.getRoom());
        return paymentMaterializationOutcomeDTOOptional;
    }
}
