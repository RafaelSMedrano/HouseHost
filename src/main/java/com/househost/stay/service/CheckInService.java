package com.househost.stay.service;

import com.househost.booking.model.Booking;
import com.househost.booking.model.BookingStatus;
import com.househost.booking.repository.BookingRepository;
import com.househost.guest.model.Guest;
import com.househost.guest.model.GuestStatus;
import com.househost.guest.repository.GuestRepository;
import com.househost.room.model.Room;
import com.househost.room.repository.RoomRepository;
import com.househost.shared.exception.StayException;
import com.househost.stay.dto.CheckInRequestDTO;
import com.househost.stay.dto.CheckInResponseDTO;
import com.househost.stay.model.CheckIn;
import com.househost.stay.model.CheckInStatus;
import com.househost.stay.model.Stay;
import com.househost.stay.model.StayStatus;
import com.househost.stay.repository.CheckInRepository;
import com.househost.stay.repository.StayRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final StayRepository stayRepository;
    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;

    public CheckInService(CheckInRepository checkInRepository, StayRepository stayRepository, BookingRepository bookingRepository, GuestRepository guestRepository, RoomRepository roomRepository) {
        this.checkInRepository = checkInRepository;
        this.stayRepository = stayRepository;
        this.bookingRepository = bookingRepository;
        this.guestRepository = guestRepository;
        this.roomRepository = roomRepository;
    }

    public CheckInResponseDTO create(CheckInRequestDTO request) {
        validateRequest(request);
        Booking booking = findBookingById(request.bookingId);
        Stay stay = resolveStay(request, booking);
        Guest guest = stay == null ? resolveGuest(request, booking) : stay.getGuest();
        Room room = stay == null ? resolveRoom(request, booking) : stay.getRoom();
        LocalDate checkInDate = resolveCheckInDate(request, booking);
        LocalDate expectedCheckOutDate = resolveExpectedCheckOutDate(request, booking);
        CheckInStatus status = parseStatus(request.status);

        if (stay == null && status == CheckInStatus.COMPLETED) {
            stay = createStay(booking, guest, room, checkInDate, expectedCheckOutDate, request);
        }

        validateUnique(stay, booking);

        CheckIn checkIn = new CheckIn(
                stay,
                booking,
                guest,
                room,
                request.expectedArrivalAt,
                request.actualCheckInAt,
                expectedCheckOutDate,
                request.adults,
                request.children,
                request.pets,
                request.documentVerified,
                request.paymentVerified,
                request.registrationFormSigned,
                request.rulesAccepted,
                request.keysDelivered,
                normalizeOptional(request.vehiclePlate),
                normalizeOptional(request.vehicleModel),
                normalizeOptional(request.performedBy),
                normalizeOptional(request.notes),
                status
        );

        if (booking != null && status == CheckInStatus.COMPLETED) {
            booking.changeStatus(BookingStatus.GOT_CHECKIN);
            bookingRepository.save(booking);
        }

        if (status == CheckInStatus.COMPLETED) {
            guest.changeStatus(GuestStatus.IN_STAY);
            guestRepository.save(guest);
        }

        CheckIn savedCheckIn = checkInRepository.save(checkIn);
        return new CheckInResponseDTO(savedCheckIn);
    }

    public List<CheckInResponseDTO> findAll() {
        return checkInRepository.findAll()
                .stream()
                .map(CheckInResponseDTO::new)
                .toList();
    }

    public CheckInResponseDTO findById(Long id) {
        return new CheckInResponseDTO(findCheckInById(id));
    }

    public CheckInResponseDTO update(Long id, CheckInRequestDTO request) {
        validateRequest(request);
        CheckIn checkIn = findCheckInById(id);
        Booking booking = findBookingById(request.bookingId);
        Stay stay = resolveStay(request, booking);
        Guest guest = stay == null ? resolveGuest(request, booking) : stay.getGuest();
        Room room = stay == null ? resolveRoom(request, booking) : stay.getRoom();
        LocalDate expectedCheckOutDate = resolveExpectedCheckOutDate(request, booking);
        CheckInStatus status = parseStatus(request.status);

        validateUnique(stay, booking, id);

        checkIn.updateCheckIn(
                stay,
                booking,
                guest,
                room,
                request.expectedArrivalAt,
                request.actualCheckInAt,
                expectedCheckOutDate,
                request.adults,
                request.children,
                request.pets,
                request.documentVerified,
                request.paymentVerified,
                request.registrationFormSigned,
                request.rulesAccepted,
                request.keysDelivered,
                normalizeOptional(request.vehiclePlate),
                normalizeOptional(request.vehicleModel),
                normalizeOptional(request.performedBy),
                normalizeOptional(request.notes),
                status
        );

        if (status == CheckInStatus.COMPLETED) {
            guest.changeStatus(GuestStatus.IN_STAY);
            guestRepository.save(guest);
        }

        CheckIn savedCheckIn = checkInRepository.save(checkIn);
        return new CheckInResponseDTO(savedCheckIn);
    }

    public void delete(Long id) {
        CheckIn checkIn = findCheckInById(id);
        checkInRepository.delete(checkIn);
    }

    private CheckIn findCheckInById(Long id) {
        if (id == null) {
            throw new StayException("Check-in nao encontrado.");
        }
        return checkInRepository.findById(id)
                .orElseThrow(() -> new StayException("Check-in nao encontrado."));
    }

    private Stay resolveStay(CheckInRequestDTO request, Booking booking) {
        if (request.stayId != null) {
            return stayRepository.findById(request.stayId)
                    .orElseThrow(() -> new StayException("Estadia nao encontrada."));
        }
        if (booking != null) {
            List<Stay> stays = stayRepository.findByBookingId(booking.getId());
            return stays.isEmpty() ? null : stays.get(0);
        }
        return null;
    }

    private Stay createStay(Booking booking, Guest guest, Room room, LocalDate checkInDate, LocalDate expectedCheckOutDate, CheckInRequestDTO request) {
        Stay stay = new Stay(
                booking,
                guest,
                room,
                checkInDate,
                expectedCheckOutDate,
                null,
                StayStatus.ACTIVE,
                booking == null ? BigDecimal.ZERO : booking.getTotalAmount(),
                normalizeOptional(request.vehiclePlate),
                normalizeOptional(request.vehicleModel)
        );
        return stayRepository.save(stay);
    }

    private Booking findBookingById(Long id) {
        if (id == null) {
            return null;
        }
        return bookingRepository.findById(id)
                .orElseThrow(() -> new StayException("Reserva nao encontrada."));
    }

    private Guest resolveGuest(CheckInRequestDTO request, Booking booking) {
        if (booking != null) {
            return booking.getGuest();
        }
        if (request.guestId == null) {
            throw new StayException("Hospede e obrigatorio.");
        }
        return guestRepository.findById(request.guestId)
                .orElseThrow(() -> new StayException("Hospede nao encontrado."));
    }

    private Room resolveRoom(CheckInRequestDTO request, Booking booking) {
        if (booking != null) {
            return booking.getRoom();
        }
        if (request.roomId == null) {
            throw new StayException("Quarto e obrigatorio.");
        }
        return roomRepository.findById(request.roomId)
                .orElseThrow(() -> new StayException("Quarto nao encontrado."));
    }

    private LocalDate resolveCheckInDate(CheckInRequestDTO request, Booking booking) {
        if (request.checkInDate != null) {
            return request.checkInDate;
        }
        if (booking != null) {
            return booking.getCheckInDate();
        }
        return LocalDate.now();
    }

    private LocalDate resolveExpectedCheckOutDate(CheckInRequestDTO request, Booking booking) {
        if (request.expectedCheckOutDate != null) {
            return request.expectedCheckOutDate;
        }
        if (booking != null) {
            return booking.getCheckOutDate();
        }
        throw new StayException("Data prevista de check-out e obrigatoria.");
    }

    private void validateRequest(CheckInRequestDTO request) {
        if (request == null) {
            throw new StayException("Dados do check-in sao obrigatorios.");
        }
        if (request.stayId == null && request.bookingId == null && (request.guestId == null || request.roomId == null)) {
            throw new StayException("Informe uma estadia, uma reserva ou hospede e quarto para o check-in.");
        }
    }

    private void validateUnique(Stay stay, Booking booking) {
        validateUnique(stay, booking, null);
    }

    private void validateUnique(Stay stay, Booking booking, Long id) {
        if (stay != null) {
            checkInRepository.findByStayId(stay.getId()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new StayException("Estadia ja possui check-in.");
                }
            });
        }
        if (booking != null) {
            checkInRepository.findByBookingId(booking.getId()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new StayException("Reserva ja possui check-in.");
                }
            });
        }
    }

    private CheckInStatus parseStatus(String status) {
        if (isBlank(status)) {
            return CheckInStatus.COMPLETED;
        }
        try {
            return CheckInStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new StayException("Status do check-in invalido. Use PENDING, COMPLETED, CANCELLED ou NO_SHOW.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
