package com.househost.booking.service;

import com.househost.booking.dto.BookingRequestDTO;
import com.househost.booking.dto.BookingResponseDTO;
import com.househost.booking.model.Booking;
import com.househost.booking.model.BookingStatus;
import com.househost.booking.repository.BookingRepository;
import com.househost.guest.model.Guest;
import com.househost.guest.repository.GuestRepository;
import com.househost.room.model.Room;
import com.househost.room.repository.RoomRepository;
import com.househost.shared.dto.ResponseDTO;
import com.househost.shared.exception.BookingException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private static final List<BookingStatus> BLOCKING_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED
    );

    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;

    public BookingService(BookingRepository bookingRepository, GuestRepository guestRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.guestRepository = guestRepository;
        this.roomRepository = roomRepository;
    }

    public ResponseDTO create(BookingRequestDTO request) {
        validateRequest(request);

        Guest guest = findGuestById(request.guestId);
        Room room = findRoomById(request.roomId);
        BookingStatus status = parseBookingStatus(request.status);

        validateRoomAvailability(room.getId(), request, status);

        Booking booking = new Booking(
                guest,
                room,
                request.checkInDate,
                request.checkOutDate,
                status,
                calculateTotalAmount(room, request)
        );

        Booking savedBooking = bookingRepository.save(booking);
        return new ResponseDTO("success", "Reserva cadastrada com sucesso", new BookingResponseDTO(savedBooking));
    }

    public ResponseDTO findAll() {
        List<BookingResponseDTO> bookings = bookingRepository.findAll()
                .stream()
                .map(BookingResponseDTO::new)
                .toList();

        return new ResponseDTO("success", "Reservas encontradas com sucesso", bookings);
    }

    public ResponseDTO findById(Long id) {
        Booking booking = findBookingById(id);
        return new ResponseDTO("success", "Reserva encontrada com sucesso", new BookingResponseDTO(booking));
    }

    public ResponseDTO findByGuestId(Long guestId) {
        findGuestById(guestId);

        List<BookingResponseDTO> bookings = bookingRepository.findByGuestId(guestId)
                .stream()
                .map(BookingResponseDTO::new)
                .toList();

        return new ResponseDTO("success", "Reservas do hospede encontradas com sucesso", bookings);
    }

    public ResponseDTO findByRoomId(Long roomId) {
        findRoomById(roomId);

        List<BookingResponseDTO> bookings = bookingRepository.findByRoomId(roomId)
                .stream()
                .map(BookingResponseDTO::new)
                .toList();

        return new ResponseDTO("success", "Reservas do quarto encontradas com sucesso", bookings);
    }

    public ResponseDTO update(Long id, BookingRequestDTO request) {
        validateRequest(request);

        Booking booking = findBookingById(id);
        Guest guest = findGuestById(request.guestId);
        Room room = findRoomById(request.roomId);
        BookingStatus status = parseBookingStatus(request.status);

        validateRoomAvailability(room.getId(), id, request, status);

        booking.updateBooking(
                guest,
                room,
                request.checkInDate,
                request.checkOutDate,
                status,
                calculateTotalAmount(room, request)
        );

        Booking savedBooking = bookingRepository.save(booking);
        return new ResponseDTO("success", "Reserva atualizada com sucesso", new BookingResponseDTO(savedBooking));
    }

    public ResponseDTO delete(Long id) {
        Booking booking = findBookingById(id);
        bookingRepository.delete(booking);
        return new ResponseDTO("success", "Reserva removida com sucesso", null);
    }

    private Booking findBookingById(Long id) {
        if (id == null) {
            throw new BookingException("Reserva nao encontrada.");
        }

        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingException("Reserva nao encontrada."));
    }

    private Guest findGuestById(Long id) {
        if (id == null) {
            throw new BookingException("Hospede e obrigatorio.");
        }

        return guestRepository.findById(id)
                .orElseThrow(() -> new BookingException("Hospede nao encontrado."));
    }

    private Room findRoomById(Long id) {
        if (id == null) {
            throw new BookingException("Quarto e obrigatorio.");
        }

        return roomRepository.findById(id)
                .orElseThrow(() -> new BookingException("Quarto nao encontrado."));
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

    private void validateRoomAvailability(Long roomId, BookingRequestDTO request, BookingStatus status) {
        if (!BLOCKING_STATUSES.contains(status)) {
            return;
        }

        boolean hasConflict = bookingRepository.existsOverlappingBooking(
                roomId,
                request.checkInDate,
                request.checkOutDate,
                BLOCKING_STATUSES
        );

        if (hasConflict) {
            throw new BookingException("Quarto ja possui reserva no periodo informado.");
        }
    }

    private void validateRoomAvailability(Long roomId, Long bookingId, BookingRequestDTO request, BookingStatus status) {
        if (!BLOCKING_STATUSES.contains(status)) {
            return;
        }

        boolean hasConflict = bookingRepository.existsOverlappingBookingIgnoringId(
                roomId,
                bookingId,
                request.checkInDate,
                request.checkOutDate,
                BLOCKING_STATUSES
        );

        if (hasConflict) {
            throw new BookingException("Quarto ja possui reserva no periodo informado.");
        }
    }

    private BookingStatus parseBookingStatus(String status) {
        if (isBlank(status)) {
            return BookingStatus.PENDING;
        }

        try {
            return BookingStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BookingException("Status da reserva invalido. Use PENDING, CONFIRMED ou CANCELLED.");
        }
    }

    private BigDecimal calculateTotalAmount(Room room, BookingRequestDTO request) {
        long totalNights = ChronoUnit.DAYS.between(request.checkInDate, request.checkOutDate);
        return room.getDailyRate().multiply(BigDecimal.valueOf(totalNights));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
