package com.househost.booking.checking.application.service;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.guest.domain.model.Guest;
import com.househost.guest.domain.model.GuestStatus;
import com.househost.guest.application.service.GuestService;
import com.househost.room.domain.model.Room;
import com.househost.shared.exception.BookingException;
import com.househost.booking.checking.application.dto.CheckInRequestDTO;
import com.househost.booking.checking.application.dto.CheckInResponseDTO;
import com.househost.booking.checking.application.port.in.CheckInUseCase;
import com.househost.booking.checking.application.port.out.CheckInAuditPort;
import com.househost.booking.checking.application.port.out.CheckInPersistencePort;
import com.househost.booking.checking.domain.model.CheckIn;
import com.househost.booking.checking.domain.model.CheckInStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CheckInService implements CheckInUseCase {

    private final CheckInPersistencePort checkInRepository;
    private final GuestService guestService;
    private final CheckInPartyResolverService partyResolverService;
    private final CheckInAuditPort checkInAuditPort;
    private final CheckInValidationService validationService;

    public CheckInService(CheckInPersistencePort checkInRepository, GuestService guestService,
                          CheckInPartyResolverService partyResolverService,
                          CheckInAuditPort checkInAuditPort,
                          CheckInValidationService validationService) {
        this.checkInRepository = checkInRepository;
        this.guestService = guestService;
        this.partyResolverService = partyResolverService;
        this.checkInAuditPort = checkInAuditPort;
        this.validationService = validationService;
    }

    public CheckInResponseDTO create(CheckInRequestDTO request) {
        validationService.validateRequest(request);
        CheckInStatus status = request.status == null ? CheckInStatus.COMPLETED : request.status;
        Booking booking = partyResolverService.findBooking(request.bookingId);
        Guest guest = partyResolverService.resolveGuest(request, booking);
        Room room = partyResolverService.resolveRoom(request, booking);
        validationService.validateUnique(booking, null);

        CheckIn checkIn = new CheckIn(
                booking,
                guest,
                room,
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

        CheckIn savedCheckIn = checkInRepository.save(checkIn);
        partyResolverService.resolveParties(savedCheckIn);
        checkInAuditPort.record("CHECK_IN_CREATED", savedCheckIn.getId(), Map.of("status", savedCheckIn.getStatus().name()));
        return new CheckInResponseDTO(savedCheckIn);
    }

    public List<CheckInResponseDTO> findAll() {
        List<CheckInResponseDTO> checkIns = checkInRepository.findAll()
                .stream()
                .map(CheckInResponseDTO::new)
                .toList();
        checkInAuditPort.record("CHECK_IN_LIST_VIEWED", null, Map.of("resultCount", checkIns.size()));
        return checkIns;
    }

    public List<CheckIn> findAllCheckIns() {
        return checkInRepository.findAll();
    }

    public CheckInResponseDTO findById(Long id) {
        CheckIn checkIn = findCheckInById(id);
        checkInAuditPort.record("CHECK_IN_VIEWED", checkIn.getId(), Map.of());
        return new CheckInResponseDTO(checkIn);
    }

    public CheckInResponseDTO update(Long id, CheckInRequestDTO request) {
        validationService.validateRequest(request);
        CheckIn checkIn = findCheckInById(id);
        CheckInStatus status = request.status == null ? CheckInStatus.COMPLETED : request.status;
        Booking booking = partyResolverService.findBooking(request.bookingId);
        Guest guest = partyResolverService.resolveGuest(request, booking);
        Room room = partyResolverService.resolveRoom(request, booking);
        validationService.validateUnique(booking, id);

        checkIn.updateCheckIn(
                booking,
                guest,
                room,
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
            guestService.changeStatus(guest.getId(), GuestStatus.IN_STAY);
        }

        CheckIn savedCheckIn = checkInRepository.save(checkIn);
        checkInAuditPort.record("CHECK_IN_UPDATED", savedCheckIn.getId(), Map.of("status", savedCheckIn.getStatus().name()));
        return new CheckInResponseDTO(savedCheckIn);
    }

    public void delete(Long id) {
        CheckIn checkIn = findCheckInById(id);
        checkInRepository.delete(checkIn);
        checkInAuditPort.record("CHECK_IN_DELETED", checkIn.getId(), Map.of());
    }

    private CheckIn findCheckInById(Long id) {
        if (id == null) {
            throw new BookingException("Check-in nao encontrado.");
        }
        return checkInRepository.findById(id)
                .orElseThrow(() -> new BookingException("Check-in nao encontrado."));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
