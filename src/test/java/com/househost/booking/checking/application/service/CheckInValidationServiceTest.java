package com.househost.booking.checking.application.service;

import com.househost.booking.checking.application.dto.CheckInRequestDTO;
import com.househost.booking.checking.application.port.out.CheckInPersistencePort;
import com.househost.booking.checking.domain.model.CheckInStatus;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanMaterializationDTO;
import com.househost.shared.exception.BookingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class CheckInValidationServiceTest {

    private final CheckInValidationService checkInValidationService =
            new CheckInValidationService(mock(CheckInPersistencePort.class));

    @Test
    void acceptsOptionalMaterializationOnlyForCompletedCheckIn() {
        CheckInRequestDTO checkInRequestDTO = request(CheckInStatus.COMPLETED);
        checkInRequestDTO.paymentMaterialization =
                new FinancialTransactionPlanMaterializationDTO();

        assertDoesNotThrow(() -> checkInValidationService.validateRequest(checkInRequestDTO));

        checkInRequestDTO.status = CheckInStatus.PENDING;
        assertThrows(
                BookingException.class,
                () -> checkInValidationService.validateRequest(checkInRequestDTO)
        );
    }

    @Test
    void preservesPendingCheckInWithoutFinancialDefinition() {
        assertDoesNotThrow(() -> checkInValidationService.validateRequest(
                request(CheckInStatus.PENDING)
        ));
    }

    private CheckInRequestDTO request(CheckInStatus checkInStatus) {
        CheckInRequestDTO checkInRequestDTO = new CheckInRequestDTO();
        checkInRequestDTO.bookingId = 23L;
        checkInRequestDTO.status = checkInStatus;
        return checkInRequestDTO;
    }
}
