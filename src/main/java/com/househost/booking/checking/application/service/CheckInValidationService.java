package com.househost.booking.checking.application.service;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.checking.application.dto.CheckInRequestDTO;
import com.househost.booking.checking.application.port.out.CheckInPersistencePort;
import com.househost.shared.exception.BookingException;
import org.springframework.stereotype.Service;

@Service
public class CheckInValidationService {
    private final CheckInPersistencePort repository;
    public CheckInValidationService(CheckInPersistencePort repository) { this.repository = repository; }

    void validateRequest(CheckInRequestDTO request) {
        if (request == null) throw new BookingException("Dados do check-in sao obrigatorios.");
        if (request.bookingId == null) throw new BookingException("Reserva e obrigatoria para o check-in.");
    }

    void validateUnique(Booking booking, Long currentId) {
        if (booking != null) repository.findByBookingId(booking.getId()).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) throw new BookingException("Reserva ja possui check-in.");
        });
    }
}
