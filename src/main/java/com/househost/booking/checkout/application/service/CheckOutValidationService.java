package com.househost.booking.checkout.application.service;

import com.househost.booking.checkout.application.dto.CheckOutRequestDTO;
import com.househost.booking.checkout.application.port.out.CheckOutPersistencePort;
import com.househost.shared.exception.BookingException;
import org.springframework.stereotype.Service;

@Service
public class CheckOutValidationService {
    private final CheckOutPersistencePort repository;

    public CheckOutValidationService(CheckOutPersistencePort repository) {
        this.repository = repository;
    }

    void validateRequest(CheckOutRequestDTO request) {
        if (request == null) throw new BookingException("Dados do check-out sao obrigatorios.");
        if (request.bookingId == null) throw new BookingException("Reserva e obrigatoria para o check-out.");
    }

    void validateUnique(Long bookingId, Long currentId) {
        repository.findByBookingId(bookingId).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw new BookingException("Reserva ja possui check-out.");
            }
        });
    }
}
