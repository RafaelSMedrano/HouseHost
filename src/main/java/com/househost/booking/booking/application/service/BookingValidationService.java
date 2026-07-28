package com.househost.booking.booking.application.service;

import com.househost.booking.booking.application.dto.BookingRequestDTO;
import com.househost.booking.booking.application.port.out.BookingPersistencePort;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.shared.exception.BookingException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class BookingValidationService {

    private static final List<BookingStatus> BLOCKING_STATUSES = List.of(
            BookingStatus.UNCONFIRMED,
            BookingStatus.CONFIRMED,
            BookingStatus.IN_STAY
    );

    private final BookingPersistencePort bookingRepository;

    BookingValidationService(BookingPersistencePort bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    void validateCreate(BookingRequestDTO request) {
        validateRequest(request);
        BookingStatus status = request.status == null ? BookingStatus.UNCONFIRMED : request.status;
        validateRoomAvailability(request.roomId, null, request, status);
    }

    void validateUpdate(Long bookingId, BookingRequestDTO request) {
        validateRequest(request);
        if (bookingId == null) {
            throw new BookingException("Reserva nao encontrada.");
        }
        BookingStatus status = request.status == null ? BookingStatus.UNCONFIRMED : request.status;
        validateRoomAvailability(request.roomId, bookingId, request, status);
    }

    private void validateRequest(BookingRequestDTO request) {
        if (request == null) {
            throw new BookingException("Dados da reserva sao obrigatorios.");
        }
        if (request.guestId == null) {
            throw new BookingException("Hospede e obrigatorio.");
        }
        if (request.roomId == null) {
            throw new BookingException("Quarto e obrigatorio.");
        }
        if (request.checkInDate == null) {
            throw new BookingException("Data de check-in e obrigatoria.");
        }
        if (request.checkOutDate == null) {
            throw new BookingException("Data de check-out e obrigatoria.");
        }
        if (!request.checkOutDate.isAfter(request.checkInDate)) {
            throw new BookingException("Data de check-out deve ser posterior a data de check-in.");
        }
    }

    private void validateRoomAvailability(
            Long roomId,
            Long bookingId,
            BookingRequestDTO request,
            BookingStatus status
    ) {
        if (!BLOCKING_STATUSES.contains(status)) {
            return;
        }

        boolean hasConflict = bookingId == null
                ? bookingRepository.existsOverlappingBooking(
                        roomId, request.checkInDate, request.checkOutDate, BLOCKING_STATUSES
                )
                : bookingRepository.existsOverlappingBookingIgnoringId(
                        roomId, bookingId, request.checkInDate, request.checkOutDate, BLOCKING_STATUSES
                );

        if (hasConflict) {
            throw new BookingException("Quarto ja possui reserva no periodo informado.");
        }
    }
}
