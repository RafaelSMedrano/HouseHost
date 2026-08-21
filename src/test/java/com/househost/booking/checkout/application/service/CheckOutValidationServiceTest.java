package com.househost.booking.checkout.application.service;

import com.househost.booking.checkout.application.dto.CheckOutRequestDTO;
import com.househost.booking.checkout.application.dto.CheckOutRatingRequestDTO;
import com.househost.booking.checkout.application.port.out.CheckOutPersistencePort;
import com.househost.booking.checkout.domain.model.CheckOutStatus;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanMaterializationDTO;
import com.househost.shared.exception.BookingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class CheckOutValidationServiceTest {

    private final CheckOutValidationService checkOutValidationService =
            new CheckOutValidationService(mock(CheckOutPersistencePort.class));

    @Test
    void acceptsCompleteSixCriterionRatingForCompletedCheckout() {
        CheckOutRequestDTO checkOutRequestDTO = request();
        checkOutRequestDTO.rating = completeRating();

        assertDoesNotThrow(() -> checkOutValidationService.validateRequest(checkOutRequestDTO));
    }

    @Test
    void rejectsMissingOrIncompleteRatingForCompletedCheckout() {
        CheckOutRequestDTO checkOutRequestDTO = request();
        assertThrows(
                BookingException.class,
                () -> checkOutValidationService.validateRequest(checkOutRequestDTO)
        );

        checkOutRequestDTO.rating = completeRating();
        checkOutRequestDTO.rating.comfortScore = null;
        assertThrows(
                BookingException.class,
                () -> checkOutValidationService.validateRequest(checkOutRequestDTO)
        );
    }

    @Test
    void rejectsCriterionOutsideOneToFive() {
        CheckOutRequestDTO checkOutRequestDTO = request();
        checkOutRequestDTO.rating = completeRating();
        checkOutRequestDTO.rating.locationScore = 6;

        assertThrows(
                BookingException.class,
                () -> checkOutValidationService.validateRequest(checkOutRequestDTO)
        );
    }

    @Test
    void pendingAndCancelledCheckoutDoNotRequireRating() {
        CheckOutRequestDTO checkOutRequestDTO = request();
        checkOutRequestDTO.status = CheckOutStatus.PENDING;
        assertDoesNotThrow(() -> checkOutValidationService.validateRequest(checkOutRequestDTO));

        checkOutRequestDTO.status = CheckOutStatus.CANCELLED;
        assertDoesNotThrow(() -> checkOutValidationService.validateRequest(checkOutRequestDTO));
    }

    @Test
    void rejectsMaterializationForCheckoutThatIsNotCompleted() {
        CheckOutRequestDTO checkOutRequestDTO = request();
        checkOutRequestDTO.status = CheckOutStatus.PENDING;
        checkOutRequestDTO.paymentMaterialization =
                new FinancialTransactionPlanMaterializationDTO();

        assertThrows(
                BookingException.class,
                () -> checkOutValidationService.validateRequest(checkOutRequestDTO)
        );
    }

    private CheckOutRequestDTO request() {
        CheckOutRequestDTO checkOutRequestDTO = new CheckOutRequestDTO();
        checkOutRequestDTO.bookingId = 23L;
        checkOutRequestDTO.status = CheckOutStatus.COMPLETED;
        return checkOutRequestDTO;
    }

    private CheckOutRatingRequestDTO completeRating() {
        CheckOutRatingRequestDTO checkOutRatingRequestDTO = new CheckOutRatingRequestDTO();
        checkOutRatingRequestDTO.checkInProcedureScore = 5;
        checkOutRatingRequestDTO.checkOutProcedureScore = 5;
        checkOutRatingRequestDTO.accommodationCleanlinessScore = 5;
        checkOutRatingRequestDTO.teamCommunicationScore = 5;
        checkOutRatingRequestDTO.locationScore = 5;
        checkOutRatingRequestDTO.comfortScore = 5;
        return checkOutRatingRequestDTO;
    }
}
