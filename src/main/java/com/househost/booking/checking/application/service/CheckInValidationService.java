package com.househost.booking.checking.application.service;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.checking.application.dto.CheckInRequestDTO;
import com.househost.booking.checking.application.port.out.CheckInPersistencePort;
import com.househost.booking.checking.domain.model.CheckInStatus;
import com.househost.shared.exception.BookingException;
import org.springframework.stereotype.Service;

@Service
public class CheckInValidationService {

    private final CheckInPersistencePort checkInPersistencePort;

    public CheckInValidationService(CheckInPersistencePort checkInPersistencePort) {
        this.checkInPersistencePort = checkInPersistencePort;
    }

    void validateRequest(CheckInRequestDTO request) {
        if (request == null) {
            throw new BookingException("Dados do check-in sao obrigatorios.");
        }
        if (request.bookingId == null) {
            throw new BookingException("Reserva e obrigatoria para o check-in.");
        }
        CheckInStatus checkInStatus = request.status == null
                ? CheckInStatus.COMPLETED
                : request.status;
        if (checkInStatus != CheckInStatus.COMPLETED
                && request.paymentMaterialization != null) {
            throw new BookingException(
                    "Pagamento agendado somente pode ser materializado no check-in concluido."
            );
        }
    }

    void validateUnique(Booking booking, Long currentId) {
        if (booking == null) {
            return;
        }
        checkInPersistencePort.findByBookingId(booking.getId()).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw new BookingException("Reserva ja possui check-in.");
            }
        });
    }
}
