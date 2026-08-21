package com.househost.booking.checking.application.service;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.application.service.BookingService;
import com.househost.guest.application.service.GuestService;
import com.househost.guest.domain.model.Guest;
import com.househost.room.application.service.RoomService;
import com.househost.room.domain.model.Room;
import com.househost.shared.exception.BookingException;
import com.househost.booking.checking.application.dto.CheckInRequestDTO;
import com.househost.booking.checking.application.dto.CheckInResponseDTO;
import com.househost.booking.checking.application.port.in.CheckInUseCase;
import com.househost.booking.checking.application.port.out.CheckInAuditPort;
import com.househost.booking.checking.application.port.out.CheckInPersistencePort;
import com.househost.booking.checking.domain.model.CheckIn;
import com.househost.booking.checking.domain.model.CheckInStatus;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanReplacementOutcomeDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CheckInService implements CheckInUseCase {

    private final CheckInPersistencePort checkInRepository;
    private final BookingService bookingService;
    private final GuestService guestService;
    private final RoomService roomService;
    private final CheckInParticipantNotifier checkInParticipantNotifier;
    private final CheckInAuditPort checkInAuditPort;
    private final CheckInValidationService checkInValidationService;

    public CheckInService(
            CheckInPersistencePort checkInRepository,
            BookingService bookingService,
            GuestService guestService,
            RoomService roomService,
            CheckInParticipantNotifier checkInParticipantNotifier,
            CheckInAuditPort checkInAuditPort,
            CheckInValidationService checkInValidationService
    ) {
        this.checkInRepository = checkInRepository;
        this.bookingService = bookingService;
        this.guestService = guestService;
        this.roomService = roomService;
        this.checkInParticipantNotifier = checkInParticipantNotifier;
        this.checkInAuditPort = checkInAuditPort;
        this.checkInValidationService = checkInValidationService;
    }

    @Transactional
    public CheckInResponseDTO create(CheckInRequestDTO request) {
        checkInValidationService.validateRequest(request);
        CheckInStatus status = request.status == null ? CheckInStatus.COMPLETED : request.status;
        Booking booking = bookingService.findBooking(request.bookingId);
        Guest guest = resolveGuest(request, booking);
        Room room = resolveRoom(request, booking);
        checkInValidationService.validateUnique(booking, null);

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
        Optional<FinancialTransactionPlanReplacementOutcomeDTO>
                paymentMaterializationOutcomeDTOOptional =
                checkInParticipantNotifier.notifyCompletion(
                        savedCheckIn,
                        request.paymentMaterialization
                );
        checkInAuditPort.record(
                "CHECK_IN_CREATED",
                savedCheckIn.getId(),
                Map.of("status", savedCheckIn.getStatus().name())
        );
        return new CheckInResponseDTO(
                savedCheckIn,
                paymentMaterializationOutcomeDTOOptional.orElse(null)
        );
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

    @Transactional
    public CheckInResponseDTO update(Long id, CheckInRequestDTO request) {
        checkInValidationService.validateRequest(request);
        CheckIn checkIn = findCheckInById(id);
        CheckInStatus status = request.status == null ? CheckInStatus.COMPLETED : request.status;
        Booking booking = bookingService.findBooking(request.bookingId);
        Guest guest = resolveGuest(request, booking);
        Room room = resolveRoom(request, booking);
        checkInValidationService.validateUnique(booking, id);

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

        CheckIn savedCheckIn = checkInRepository.save(checkIn);
        Optional<FinancialTransactionPlanReplacementOutcomeDTO>
                paymentMaterializationOutcomeDTOOptional =
                checkInParticipantNotifier.notifyCompletion(
                        savedCheckIn,
                        request.paymentMaterialization
                );
        checkInAuditPort.record(
                "CHECK_IN_UPDATED",
                savedCheckIn.getId(),
                Map.of("status", savedCheckIn.getStatus().name())
        );
        return new CheckInResponseDTO(
                savedCheckIn,
                paymentMaterializationOutcomeDTOOptional.orElse(null)
        );
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

    private Guest resolveGuest(CheckInRequestDTO request, Booking booking) {
        if (booking != null) {
            return booking.getGuest();
        }
        if (request.guestId == null) {
            throw new BookingException("Hospede e obrigatorio.");
        }
        return guestService.findGuestById(request.guestId);
    }

    private Room resolveRoom(CheckInRequestDTO request, Booking booking) {
        if (booking != null) {
            return booking.getRoom();
        }
        if (request.roomId == null) {
            throw new BookingException("Quarto e obrigatorio.");
        }
        return roomService.findRoomById(request.roomId);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
