package com.househost.booking.booking.application.service;

import com.househost.booking.booking.application.dto.BookingRequestDTO;
import com.househost.booking.booking.application.dto.BookingResponseDTO;
import com.househost.booking.booking.application.port.in.BookingUseCase;
import com.househost.booking.booking.application.port.out.BookingPersistencePort;
import com.househost.booking.booking.application.port.out.BookingAuditPort;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingOrigin;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionRequestDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionResponseDTO;
import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionUseCase;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import com.househost.guest.domain.model.Guest;
import com.househost.guest.application.service.GuestService;
import com.househost.room.domain.model.Room;
import com.househost.room.application.service.RoomService;
import com.househost.shared.exception.BookingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class BookingService implements BookingUseCase {

    private final BookingPersistencePort bookingRepository;
    private final GuestService guestService;
    private final RoomService roomService;
    private final FinancialTransactionUseCase financialTransactionUseCase;
    private final BookingAuditPort bookingAuditPort;
    private final BookingValidationService bookingValidationService;

    public BookingService(
            BookingPersistencePort bookingRepository,
            GuestService guestService,
            RoomService roomService,
            FinancialTransactionUseCase financialTransactionUseCase,
            BookingAuditPort bookingAuditPort,
            BookingValidationService bookingValidationService
    ) {
        this.bookingRepository = bookingRepository;
        this.guestService = guestService;
        this.roomService = roomService;
        this.financialTransactionUseCase = financialTransactionUseCase;
        this.bookingAuditPort = bookingAuditPort;
        this.bookingValidationService = bookingValidationService;
    }

    @Transactional
    public BookingResponseDTO create(BookingRequestDTO request) {
        bookingValidationService.validateCreate(request);
        BookingStatus status = request.status == null ? BookingStatus.UNCONFIRMED : request.status;

        Guest guest = guestService.findGuestById(request.guestId);
        Room room = roomService.findRoomById(request.roomId);

        Booking booking = new Booking(
                guest,
                room,
                request.checkInDate,
                request.checkOutDate,
                status,
                calculateTotalAmount(room, request),
                request.origin == null ? BookingOrigin.DIRETO_TELEFONE : request.origin,
                request.adults,
                request.children,
                request.pets,
                request.paymentMethod == null ? null : request.paymentMethod.name(),
                normalizeOptional(request.installments),
                request.dailyRate,
                request.discount,
                BigDecimal.ZERO,
                null,
                normalizeOptional(request.specialRequests),
                normalizeOptional(request.internalNotes)
        );

        Booking savedBooking = bookingRepository.save(booking);
        FinancialTransactionRequestDTO paymentRequest = new FinancialTransactionRequestDTO();
        paymentRequest.senderType = FinancialPartyType.GUEST;
        paymentRequest.senderId = savedBooking.getGuest().getId();
        paymentRequest.receiverType = FinancialPartyType.CASHIER;
        paymentRequest.receiverId = 1L;
        paymentRequest.sourceType = FinancialTransactionSourceType.BOOKING;
        paymentRequest.sourceId = savedBooking.getId();
        paymentRequest.type = FinancialTransactionType.ENTRY;
        paymentRequest.amount = request.paidAmount != null && request.paidAmount.compareTo(BigDecimal.ZERO) > 0
                ? request.paidAmount
                : savedBooking.getTotalAmount();
        paymentRequest.transactionDate = request.paymentDate;
        paymentRequest.description = "Pagamento da reserva #" + savedBooking.getId();
        paymentRequest.status = FinancialTransactionStatus.WAITING;
        paymentRequest.method = request.paymentMethod;

        FinancialTransactionResponseDTO paymentTransaction = financialTransactionUseCase.create(paymentRequest);
        if (request.paymentCompleted) {
            financialTransactionUseCase.toSettle(paymentTransaction.getId());
        }
        bookingAuditPort.record("BOOKING_CREATED", "BOOKING", savedBooking.getId(), Map.of(
                "status", savedBooking.getStatus().name(),
                "origin", savedBooking.getOrigin().name()
        ));
        return new BookingResponseDTO(savedBooking);
    }

    public List<BookingResponseDTO> findAll() {
        List<BookingResponseDTO> bookings = bookingRepository.findAll()
                .stream()
                .map(BookingResponseDTO::new)
                .toList();
        bookingAuditPort.record("BOOKING_LIST_VIEWED", "BOOKING", null, Map.of("resultCount", bookings.size()));
        return bookings;
    }

    public List<Booking> findAllBookings() {
        return bookingRepository.findAll();
    }

    public BookingResponseDTO findById(Long id) {
        if (id == null) {
            throw new BookingException("Reserva nao encontrada.");
        }

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingException("Reserva nao encontrada."));
        bookingAuditPort.record("BOOKING_VIEWED", "BOOKING", booking.getId(), Map.of());
        return new BookingResponseDTO(booking);
    }

    public List<BookingResponseDTO> findByGuestId(Long guestId) {
        guestService.findGuestById(guestId);

        List<BookingResponseDTO> bookings = bookingRepository.findByGuestId(guestId)
                .stream()
                .map(BookingResponseDTO::new)
                .toList();
        bookingAuditPort.record("GUEST_BOOKINGS_VIEWED", "GUEST", guestId, Map.of("resultCount", bookings.size()));
        return bookings;
    }

    public List<BookingResponseDTO> findByRoomId(Long roomId) {
        roomService.findRoomById(roomId);

        List<BookingResponseDTO> bookings = bookingRepository.findByRoomId(roomId)
                .stream()
                .map(BookingResponseDTO::new)
                .toList();
        bookingAuditPort.record("ROOM_BOOKINGS_VIEWED", "ROOM", roomId, Map.of("resultCount", bookings.size()));
        return bookings;
    }

    public BookingResponseDTO update(Long id, BookingRequestDTO request) {
        bookingValidationService.validateUpdate(id, request);
        BookingStatus status = request.status == null ? BookingStatus.UNCONFIRMED : request.status;

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingException("Reserva nao encontrada."));
        Guest guest = guestService.findGuestById(request.guestId);
        Room room = roomService.findRoomById(request.roomId);

        booking.updateBooking(
                guest,
                room,
                request.checkInDate,
                request.checkOutDate,
                status,
                calculateTotalAmount(room, request),
                request.origin == null ? BookingOrigin.DIRETO_TELEFONE : request.origin,
                request.adults,
                request.children,
                request.pets,
                request.paymentMethod == null ? null : request.paymentMethod.name(),
                normalizeOptional(request.installments),
                request.dailyRate,
                request.discount,
                request.paidAmount,
                request.paymentDate,
                normalizeOptional(request.specialRequests),
                normalizeOptional(request.internalNotes)
        );

        Booking savedBooking = bookingRepository.save(booking);
        bookingAuditPort.record("BOOKING_UPDATED", "BOOKING", savedBooking.getId(), Map.of(
                "status", savedBooking.getStatus().name()
        ));
        return new BookingResponseDTO(savedBooking);
    }

    public void delete(Long id) {
        if (id == null) {
            throw new BookingException("Reserva nao encontrada.");
        }

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingException("Reserva nao encontrada."));
        bookingRepository.delete(booking);
        bookingAuditPort.record("BOOKING_DELETED", "BOOKING", booking.getId(), Map.of());
    }

    public Booking findBooking(Long id) {
        if (id == null) {
            return null;
        }
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingException("Reserva nao encontrada."));
    }

    public Booking changeStatus(Long id, BookingStatus status) {
        Booking booking = findBooking(id);
        booking.changeStatus(status);
        return bookingRepository.save(booking);
    }

    private BigDecimal calculateTotalAmount(Room room, BookingRequestDTO request) {
        long totalNights = ChronoUnit.DAYS.between(request.checkInDate, request.checkOutDate);
        BigDecimal dailyRate = positiveOrDefault(request.dailyRate, room.getDailyRate());
        BigDecimal discount = positiveOrDefault(request.discount, BigDecimal.ZERO);
        BigDecimal total = dailyRate.multiply(BigDecimal.valueOf(totalNights)).subtract(discount);

        return total.max(BigDecimal.ZERO);
    }

    private BigDecimal positiveOrDefault(BigDecimal value, BigDecimal defaultValue) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return defaultValue;
        }

        return value;
    }

    private String normalizeOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
