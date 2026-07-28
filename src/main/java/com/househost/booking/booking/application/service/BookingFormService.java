package com.househost.booking.booking.application.service;

import com.househost.booking.booking.application.port.in.BookingFormUseCase;
import com.househost.booking.booking.application.port.in.BookingUseCase;
import com.househost.booking.booking.application.dto.BookingFormCreateRequestDTO;
import com.househost.booking.booking.application.dto.BookingRequestDTO;
import com.househost.booking.booking.application.dto.BookingResponseDTO;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.guest.domain.model.Guest;
import com.househost.guest.application.service.GuestService;
import com.househost.room.domain.model.Room;
import com.househost.room.application.service.RoomService;
import com.househost.shared.exception.BookingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingFormService implements BookingFormUseCase {

    private final BookingUseCase bookingService;
    private final GuestService guestService;
    private final RoomService roomService;

    public BookingFormService(
            BookingUseCase bookingService,
            GuestService guestService,
            RoomService roomService
    ) {
        this.bookingService = bookingService;
        this.guestService = guestService;
        this.roomService = roomService;
    }

    @Transactional
    public BookingResponseDTO create(BookingFormCreateRequestDTO request) {
        validateFormRequest(request);

        Guest guest = findGuestFromForm(request.guest);
        Room room = findRoomFromForm(request.reservation);
        BookingRequestDTO bookingRequest = mapToBookingRequest(request, guest, room);

        return bookingService.create(bookingRequest);
    }

    private BookingRequestDTO mapToBookingRequest(
            BookingFormCreateRequestDTO request,
            Guest guest,
            Room room
    ) {
        BookingRequestDTO bookingRequest = new BookingRequestDTO();
        bookingRequest.guestId = guest.getId();
        bookingRequest.roomId = room.getId();
        bookingRequest.checkInDate = request.reservation.checkInDate;
        bookingRequest.checkOutDate = request.reservation.checkOutDate;
        bookingRequest.status = request.status == null ? BookingStatus.UNCONFIRMED : request.status;
        bookingRequest.origin = request.origin;
        bookingRequest.adults = request.reservation.adults;
        bookingRequest.children = request.reservation.children;
        bookingRequest.pets = request.reservation.pets;
        bookingRequest.paymentMethod = request.payment.paymentMethod;
        bookingRequest.installments = request.payment.installments;
        bookingRequest.dailyRate = request.payment.dailyRate;
        bookingRequest.discount = request.payment.discount;
        bookingRequest.paidAmount = request.payment.paidAmount;
        bookingRequest.paymentDate = request.payment.paymentDate;
        bookingRequest.paymentCompleted = request.payment.paymentCompleted;
        bookingRequest.specialRequests = request.specialRequests;
        bookingRequest.internalNotes = request.internalNotes;
        return bookingRequest;
    }

    private Guest findGuestFromForm(BookingFormCreateRequestDTO.GuestData guestData) {
        String documentNumber = normalizeOptional(guestData.documentNumber);
        if (documentNumber != null) {
            return guestService.findGuestByDocumentNumber(documentNumber);
        }

        return guestService.findUniqueGuestByFullName(guestData.fullName);
    }

    private Room findRoomFromForm(BookingFormCreateRequestDTO.ReservationData reservationData) {
        if (reservationData.roomId != null) {
            return roomService.findRoomById(reservationData.roomId);
        }

        String roomNumber = reservationData.roomCode.trim();
        return roomService.findRoomByNumber(roomNumber);
    }

    private void validateFormRequest(BookingFormCreateRequestDTO request) {
        if (request == null) {
            throw new BookingException("Dados do formulario de reserva sao obrigatorios.");
        }

        if (request.guest == null || (isBlank(request.guest.fullName) && isBlank(request.guest.documentNumber))) {
            throw new BookingException("Informe o nome ou CPF de um hospede cadastrado.");
        }

        if (request.reservation == null) {
            throw new BookingException("Dados da reserva sao obrigatorios.");
        }

        if (request.reservation.roomId == null && isBlank(request.reservation.roomCode)) {
            throw new BookingException("Quarto da reserva e obrigatorio.");
        }

        if (request.reservation.checkInDate == null) {
            throw new BookingException("Data de check-in e obrigatoria.");
        }

        if (request.reservation.checkOutDate == null) {
            throw new BookingException("Data de check-out e obrigatoria.");
        }

        if (!request.reservation.checkOutDate.isAfter(request.reservation.checkInDate)) {
            throw new BookingException("Data de check-out deve ser posterior a data de check-in.");
        }

        if (request.payment == null || request.payment.paymentMethod == null) {
            throw new BookingException("Forma de pagamento e obrigatoria.");
        }
    }

    private String normalizeOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
