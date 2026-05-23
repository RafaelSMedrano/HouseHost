package com.househost.stay.service;

import com.househost.booking.model.Booking;
import com.househost.booking.repository.BookingRepository;
import com.househost.guest.model.Guest;
import com.househost.guest.model.GuestStatus;
import com.househost.guest.repository.GuestRepository;
import com.househost.room.model.Room;
import com.househost.room.repository.RoomRepository;
import com.househost.shared.exception.StayException;
import com.househost.stay.dto.StayRequestDTO;
import com.househost.stay.dto.StayResponseDTO;
import com.househost.stay.model.Stay;
import com.househost.stay.model.StayStatus;
import com.househost.stay.repository.StayRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class StayService {

    private static final List<StayStatus> BLOCKING_STATUSES = List.of(StayStatus.ACTIVE);

    private final StayRepository stayRepository;
    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;

    public StayService(StayRepository stayRepository, BookingRepository bookingRepository, GuestRepository guestRepository, RoomRepository roomRepository) {
        this.stayRepository = stayRepository;
        this.bookingRepository = bookingRepository;
        this.guestRepository = guestRepository;
        this.roomRepository = roomRepository;
    }

    public StayResponseDTO create(StayRequestDTO request) {
        validateRequest(request);

        Booking booking = findBookingById(request.bookingId);
        validateBookingAvailable(request.bookingId);

        Guest guest = findGuestById(request.guestId);
        Room room = findRoomById(request.roomId);
        StayStatus status = parseStayStatus(request.status);

        validateStayDates(request, status);
        validateRoomAvailability(room.getId(), request, status);

        Stay stay = new Stay(
                booking,
                guest,
                room,
                request.checkInDate,
                request.expectedCheckOutDate,
                request.actualCheckOutDate,
                status,
                calculateTotalAmount(room, request),
                normalizeOptional(request.vehiclePlate),
                normalizeOptional(request.vehicleModel)
        );

        Stay savedStay = stayRepository.save(stay);
        syncGuestStatus(guest, status);
        return new StayResponseDTO(savedStay);
    }

    public List<StayResponseDTO> findAll() {
        return stayRepository.findAll()
                .stream()
                .map(StayResponseDTO::new)
                .toList();
    }

    public StayResponseDTO findById(Long id) {
        Stay stay = findStayById(id);
        return new StayResponseDTO(stay);
    }

    public List<StayResponseDTO> findByGuestId(Long guestId) {
        findGuestById(guestId);

        return stayRepository.findByGuestId(guestId)
                .stream()
                .map(StayResponseDTO::new)
                .toList();
    }

    public List<StayResponseDTO> findByRoomId(Long roomId) {
        findRoomById(roomId);

        return stayRepository.findByRoomId(roomId)
                .stream()
                .map(StayResponseDTO::new)
                .toList();
    }

    public List<StayResponseDTO> findByBookingId(Long bookingId) {
        findBookingByIdRequired(bookingId);

        return stayRepository.findByBookingId(bookingId)
                .stream()
                .map(StayResponseDTO::new)
                .toList();
    }

    public StayResponseDTO update(Long id, StayRequestDTO request) {
        validateRequest(request);

        Stay stay = findStayById(id);
        Booking booking = findBookingById(request.bookingId);
        validateBookingAvailable(request.bookingId, id);

        Guest guest = findGuestById(request.guestId);
        Room room = findRoomById(request.roomId);
        StayStatus status = parseStayStatus(request.status);

        validateStayDates(request, status);
        validateRoomAvailability(room.getId(), id, request, status);

        stay.updateStay(
                booking,
                guest,
                room,
                request.checkInDate,
                request.expectedCheckOutDate,
                request.actualCheckOutDate,
                status,
                calculateTotalAmount(room, request),
                normalizeOptional(request.vehiclePlate),
                normalizeOptional(request.vehicleModel)
        );

        Stay savedStay = stayRepository.save(stay);
        syncGuestStatus(guest, status);
        return new StayResponseDTO(savedStay);
    }

    public void delete(Long id) {
        Stay stay = findStayById(id);
        stayRepository.delete(stay);
    }

    private Stay findStayById(Long id) {
        if (id == null) {
            throw new StayException("Estadia nao encontrada.");
        }

        return stayRepository.findById(id)
                .orElseThrow(() -> new StayException("Estadia nao encontrada."));
    }

    private Booking findBookingById(Long id) {
        if (id == null) {
            return null;
        }

        return findBookingByIdRequired(id);
    }

    private Booking findBookingByIdRequired(Long id) {
        if (id == null) {
            throw new StayException("Reserva nao encontrada.");
        }

        return bookingRepository.findById(id)
                .orElseThrow(() -> new StayException("Reserva nao encontrada."));
    }

    private Guest findGuestById(Long id) {
        if (id == null) {
            throw new StayException("Hospede e obrigatorio.");
        }

        return guestRepository.findById(id)
                .orElseThrow(() -> new StayException("Hospede nao encontrado."));
    }

    private Room findRoomById(Long id) {
        if (id == null) {
            throw new StayException("Quarto e obrigatorio.");
        }

        return roomRepository.findById(id)
                .orElseThrow(() -> new StayException("Quarto nao encontrado."));
    }

    private void validateRequest(StayRequestDTO request) {
        if (request == null) {
            throw new StayException("Dados da estadia sao obrigatorios.");
        }

        if (request.guestId == null) {
            throw new StayException("Hospede e obrigatorio.");
        }

        if (request.roomId == null) {
            throw new StayException("Quarto e obrigatorio.");
        }

        if (request.checkInDate == null) {
            throw new StayException("Data de check-in e obrigatoria.");
        }

        if (request.expectedCheckOutDate == null) {
            throw new StayException("Data prevista de check-out e obrigatoria.");
        }

        if (!request.expectedCheckOutDate.isAfter(request.checkInDate)) {
            throw new StayException("Data prevista de check-out deve ser posterior a data de check-in.");
        }
    }

    private void validateStayDates(StayRequestDTO request, StayStatus status) {
        if (request.actualCheckOutDate != null && request.actualCheckOutDate.isBefore(request.checkInDate)) {
            throw new StayException("Data real de check-out nao pode ser anterior a data de check-in.");
        }

        if (status == StayStatus.CHECKED_OUT && request.actualCheckOutDate == null) {
            throw new StayException("Data real de check-out e obrigatoria para estadias finalizadas.");
        }
    }

    private void validateBookingAvailable(Long bookingId) {
        if (bookingId != null && stayRepository.existsByBookingId(bookingId)) {
            throw new StayException("Reserva ja possui estadia cadastrada.");
        }
    }

    private void validateBookingAvailable(Long bookingId, Long stayId) {
        if (bookingId != null && stayRepository.existsByBookingIdAndIdNot(bookingId, stayId)) {
            throw new StayException("Reserva ja possui estadia cadastrada.");
        }
    }

    private void validateRoomAvailability(Long roomId, StayRequestDTO request, StayStatus status) {
        if (!BLOCKING_STATUSES.contains(status)) {
            return;
        }

        boolean hasConflict = stayRepository.existsOverlappingStay(
                roomId,
                request.checkInDate,
                request.expectedCheckOutDate,
                BLOCKING_STATUSES
        );

        if (hasConflict) {
            throw new StayException("Quarto ja possui estadia ativa no periodo informado.");
        }
    }

    private void validateRoomAvailability(Long roomId, Long stayId, StayRequestDTO request, StayStatus status) {
        if (!BLOCKING_STATUSES.contains(status)) {
            return;
        }

        boolean hasConflict = stayRepository.existsOverlappingStayIgnoringId(
                roomId,
                stayId,
                request.checkInDate,
                request.expectedCheckOutDate,
                BLOCKING_STATUSES
        );

        if (hasConflict) {
            throw new StayException("Quarto ja possui estadia ativa no periodo informado.");
        }
    }

    private StayStatus parseStayStatus(String status) {
        if (isBlank(status)) {
            return StayStatus.ACTIVE;
        }

        try {
            return StayStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new StayException("Status da estadia invalido. Use ACTIVE, CHECKED_OUT ou CANCELLED.");
        }
    }

    private void syncGuestStatus(Guest guest, StayStatus status) {
        if (status == StayStatus.ACTIVE) {
            guest.changeStatus(GuestStatus.IN_STAY);
            guestRepository.save(guest);
        }

        if (status == StayStatus.CHECKED_OUT) {
            guest.changeStatus(GuestStatus.GOT_CHECKOUT);
            guestRepository.save(guest);
        }
    }

    private BigDecimal calculateTotalAmount(Room room, StayRequestDTO request) {
        LocalDate checkOutDate = request.actualCheckOutDate == null
                ? request.expectedCheckOutDate
                : request.actualCheckOutDate;

        long totalNights = ChronoUnit.DAYS.between(request.checkInDate, checkOutDate);
        return room.getDailyRate().multiply(BigDecimal.valueOf(totalNights));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
