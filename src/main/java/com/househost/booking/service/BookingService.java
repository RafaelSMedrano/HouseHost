package com.househost.booking.service;

import com.househost.booking.dto.BookingFormCreateRequestDTO;
import com.househost.booking.dto.BookingRequestDTO;
import com.househost.booking.dto.BookingResponseDTO;
import com.househost.booking.model.Booking;
import com.househost.booking.model.BookingOrigin;
import com.househost.booking.model.BookingStatus;
import com.househost.booking.repository.BookingRepository;
import com.househost.finance.model.FinancialTransaction;
import com.househost.finance.model.FinancialTransactionMethod;
import com.househost.finance.model.FinancialPartyType;
import com.househost.finance.model.FinancialTransactionStatus;
import com.househost.finance.model.FinancialTransactionSourceType;
import com.househost.finance.model.FinancialTransactionType;
import com.househost.finance.model.InstallmentPlanStatus;
import com.househost.finance.model.InstallmentPlanTransaction;
import com.househost.finance.repository.FinancialTransactionRepository;
import com.househost.finance.service.CashierService;
import com.househost.finance.service.FinancialTransactionService;
import com.househost.guest.model.Guest;
import com.househost.guest.repository.GuestRepository;
import com.househost.room.model.Room;
import com.househost.room.repository.RoomRepository;
import com.househost.shared.dto.ResponseDTO;
import com.househost.shared.exception.BookingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class BookingService {

    private static final List<BookingStatus> BLOCKING_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED
    );

    private static final Map<String, String> ROOM_CODE_ALIASES = Map.of(
            "suitelavanda", "Suite Lavanda",
            "suitesalvia", "Suite Salvia",
            "chale", "Chale Bergamota",
            "standard", "Standard"
    );
    private static final Long DEFAULT_PAYMENT_CASHIER_ID = 1L;

    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final FinancialTransactionRepository financialTransactionRepository;
    private final CashierService cashierService;
    private final FinancialTransactionService financialTransactionService;

    public BookingService(BookingRepository bookingRepository, GuestRepository guestRepository, RoomRepository roomRepository, FinancialTransactionRepository financialTransactionRepository, CashierService cashierService, FinancialTransactionService financialTransactionService) {
        this.bookingRepository = bookingRepository;
        this.guestRepository = guestRepository;
        this.roomRepository = roomRepository;
        this.financialTransactionRepository = financialTransactionRepository;
        this.cashierService = cashierService;
        this.financialTransactionService = financialTransactionService;
    }

    @Transactional
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
                calculateTotalAmount(room, request),
                parseBookingOrigin(request.origin),
                request.adults,
                request.children,
                request.pets,
                normalizeOptional(request.paymentMethod),
                normalizeOptional(request.installments),
                request.dailyRate,
                request.discount,
                request.paidAmount,
                request.paymentDate,
                normalizeOptional(request.specialRequests),
                normalizeOptional(request.internalNotes)
        );

        Booking savedBooking = bookingRepository.save(booking);
        createPaymentTransactionIfPresent(savedBooking, request.paymentDate, request.paymentMethod, request.paidAmount, request.installments, request.paymentCompleted);
        return new ResponseDTO("success", "Reserva cadastrada com sucesso", new BookingResponseDTO(savedBooking));
    }

    @Transactional
    public ResponseDTO createFromForm(BookingFormCreateRequestDTO request) {
        validateFormRequest(request);

        Guest guest = findGuestFromForm(request.guest);
        Room room = findRoomFromForm(request.reservation);
        BookingStatus status = parseBookingFormStatus(request.status);
        validateRoomAvailability(room.getId(), request, status);

        Booking booking = new Booking(
                guest,
                room,
                request.reservation.checkInDate,
                request.reservation.checkOutDate,
                status,
                calculateTotalAmount(room, request),
                parseBookingOrigin(request.origin),
                request.reservation.adults,
                request.reservation.children,
                request.reservation.pets,
                normalizeOptional(request.payment.paymentMethod),
                normalizeOptional(request.payment.installments),
                request.payment.dailyRate,
                request.payment.discount,
                request.payment.paidAmount,
                request.payment.paymentDate,
                normalizeOptional(request.specialRequests),
                normalizeOptional(request.internalNotes)
        );

        Booking savedBooking = bookingRepository.save(booking);
        createPaymentTransactionIfPresent(
                savedBooking,
                request.payment.paymentDate,
                request.payment.paymentMethod,
                request.payment.paidAmount,
                request.payment.installments,
                request.payment.paymentCompleted
        );

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
                calculateTotalAmount(room, request),
                parseBookingOrigin(request.origin),
                request.adults,
                request.children,
                request.pets,
                normalizeOptional(request.paymentMethod),
                normalizeOptional(request.installments),
                request.dailyRate,
                request.discount,
                request.paidAmount,
                request.paymentDate,
                normalizeOptional(request.specialRequests),
                normalizeOptional(request.internalNotes)
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

    private Guest findGuestFromForm(BookingFormCreateRequestDTO.GuestData guestData) {
        String documentNumber = normalizeOptional(guestData.documentNumber);
        if (documentNumber != null) {
            return guestRepository.findByDocumentNumber(documentNumber)
                    .orElseThrow(() -> new BookingException("Hospede cadastrado nao encontrado pelo CPF informado."));
        }

        String fullName = normalizeOptional(guestData.fullName);
        if (fullName != null) {
            List<Guest> guests = guestRepository.findByFullNameIgnoreCase(fullName);
            if (guests.isEmpty()) {
                throw new BookingException("Hospede cadastrado nao encontrado pelo nome informado.");
            }

            if (guests.size() > 1) {
                throw new BookingException("Mais de um hospede cadastrado com este nome. Informe o CPF para identificar corretamente.");
            }

            return guests.get(0);
        }

        throw new BookingException("Informe o nome ou CPF de um hospede cadastrado.");
    }

    private Room findRoomFromForm(BookingFormCreateRequestDTO.ReservationData reservationData) {
        if (reservationData.roomId != null) {
            return findRoomById(reservationData.roomId);
        }

        String roomCode = normalizeRequired(reservationData.roomCode);
        String roomNumber = ROOM_CODE_ALIASES.getOrDefault(normalizeRoomCode(roomCode), roomCode);
        return roomRepository.findByRoomNumberIgnoreCase(roomNumber)
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

        if (request.payment == null || isBlank(request.payment.paymentMethod)) {
            throw new BookingException("Forma de pagamento e obrigatoria.");
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

    private void validateRoomAvailability(Long roomId, BookingFormCreateRequestDTO request, BookingStatus status) {
        if (!BLOCKING_STATUSES.contains(status)) {
            return;
        }

        boolean hasConflict = bookingRepository.existsOverlappingBooking(
                roomId,
                request.reservation.checkInDate,
                request.reservation.checkOutDate,
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
            throw new BookingException("Status da reserva invalido. Use PENDING, CONFIRMED, CANCELLED ou GOT_CHECKIN.");
        }
    }

    private BookingStatus parseBookingFormStatus(String status) {
        if (isBlank(status)) {
            return BookingStatus.PENDING;
        }

        return switch (normalizeStatus(status)) {
            case "CONFIRMADA", "CONFIRMED" -> BookingStatus.CONFIRMED;
            case "PENDENTE", "PENDING" -> BookingStatus.PENDING;
            case "CANCELADA", "CANCELLED", "CANCELED" -> BookingStatus.CANCELLED;
            case "GOT_CHECKIN", "CHECKIN", "CHECKEDIN", "CHECK_IN", "CHECK_IN_FEITO" -> BookingStatus.GOT_CHECKIN;
            default -> throw new BookingException("Status da reserva invalido. Use confirmada, pendente, cancelada ou got checkin.");
        };
    }

    private BookingOrigin parseBookingOrigin(String origin) {
        if (isBlank(origin)) {
            return BookingOrigin.DIRETO_TELEFONE;
        }

        return switch (normalizeRoomCode(origin)) {
            case "diretotelefone", "directphone", "direct", "telefone", "on" -> BookingOrigin.DIRETO_TELEFONE;
            case "whatsapp", "whats" -> BookingOrigin.WHATSAPP;
            case "instagram", "insta" -> BookingOrigin.INSTAGRAM;
            case "booking" -> BookingOrigin.BOOKING;
            case "airbnb" -> BookingOrigin.AIRBNB;
            case "indicacao", "indication", "referral" -> BookingOrigin.INDICACAO;
            default -> throw new BookingException("Origem da reserva invalida. Use DIRETO_TELEFONE, WHATSAPP, INSTAGRAM, BOOKING, AIRBNB ou INDICACAO.");
        };
    }

    private BigDecimal calculateTotalAmount(Room room, BookingRequestDTO request) {
        long totalNights = ChronoUnit.DAYS.between(request.checkInDate, request.checkOutDate);
        BigDecimal dailyRate = positiveOrDefault(request.dailyRate, room.getDailyRate());
        BigDecimal discount = positiveOrDefault(request.discount, BigDecimal.ZERO);
        BigDecimal total = dailyRate.multiply(BigDecimal.valueOf(totalNights)).subtract(discount);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return total;
    }

    private BigDecimal calculateTotalAmount(Room room, BookingFormCreateRequestDTO request) {
        long totalNights = ChronoUnit.DAYS.between(request.reservation.checkInDate, request.reservation.checkOutDate);
        BigDecimal dailyRate = positiveOrDefault(request.payment.dailyRate, room.getDailyRate());
        BigDecimal discount = positiveOrDefault(request.payment.discount, BigDecimal.ZERO);
        BigDecimal total = dailyRate.multiply(BigDecimal.valueOf(totalNights)).subtract(discount);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return total;
    }

    private void createPaymentTransactionIfPresent(Booking booking, LocalDate requestedPaymentDate, String requestedPaymentMethod, BigDecimal requestedPaidAmount, String requestedInstallments, boolean paymentCompleted) {
        LocalDate paymentDate = requestedPaymentDate == null ? LocalDate.now() : requestedPaymentDate;
        FinancialTransactionMethod method = parsePaymentMethod(requestedPaymentMethod);
        BigDecimal transactionAmount = positiveOrDefault(requestedPaidAmount, booking.getTotalAmount());
        String description = "Pagamento da reserva #" + booking.getId();
        Integer installmentsQuantity = parseInstallmentsQuantity(requestedInstallments);
        FinancialTransactionStatus transactionStatus = paymentCompleted ? FinancialTransactionStatus.PAID : FinancialTransactionStatus.WAITING;
        FinancialTransaction savedTransaction;

        if (installmentsQuantity > 1) {
            savedTransaction = financialTransactionRepository.save(new InstallmentPlanTransaction(
                    FinancialPartyType.GUEST,
                    booking.getGuest().getId(),
                    FinancialPartyType.CASHIER,
                    DEFAULT_PAYMENT_CASHIER_ID,
                    booking.getGuest(),
                    FinancialTransactionType.ENTRY,
                    transactionAmount,
                    paymentDate,
                    description,
                    method,
                    installmentsQuantity,
                    paymentCompleted ? InstallmentPlanStatus.PAID : InstallmentPlanStatus.ON_TIME,
                    transactionStatus
            ));
        } else {
            savedTransaction = financialTransactionRepository.save(new FinancialTransaction(
                    FinancialPartyType.GUEST,
                    booking.getGuest().getId(),
                    FinancialPartyType.CASHIER,
                    DEFAULT_PAYMENT_CASHIER_ID,
                    booking.getGuest(),
                    FinancialTransactionType.ENTRY,
                    transactionAmount,
                    paymentDate,
                    description,
                    method,
                    transactionStatus
            ));
        }

        savedTransaction.setSource(FinancialTransactionSourceType.BOOKING, booking.getId());
        savedTransaction = financialTransactionRepository.save(savedTransaction);
        cashierService.createMovementsForTransaction(savedTransaction);

        if (paymentCompleted) {
            financialTransactionService.toSettle(savedTransaction.getId());
            return;
        }

        booking.getGuest().refreshFinancialStatus();
        guestRepository.save(booking.getGuest());
    }

    private Integer parseInstallmentsQuantity(String installments) {
        if (isBlank(installments)) {
            return 1;
        }

        String normalized = normalizeRoomCode(installments);
        if (normalized.equals("avista")) {
            return 1;
        }

        if (normalized.equals("entradasaldonocheckin")) {
            return 2;
        }

        if (normalized.endsWith("x")) {
            String quantity = normalized.substring(0, normalized.length() - 1);
            try {
                return Math.max(1, Integer.parseInt(quantity));
            } catch (NumberFormatException exception) {
                throw new BookingException("Quantidade de parcelas invalida.");
            }
        }

        return 1;
    }

    private FinancialTransactionMethod parsePaymentMethod(String method) {
        if (isBlank(method)) {
            return null;
        }

        return switch (normalizeRoomCode(method)) {
            case "pix" -> FinancialTransactionMethod.PIX;
            case "cartaodecredito" -> FinancialTransactionMethod.CREDIT_CARD;
            case "cartaodedebito" -> FinancialTransactionMethod.DEBIT_CARD;
            case "dinheiro" -> FinancialTransactionMethod.CASH;
            case "transferenciabancaria" -> FinancialTransactionMethod.BANK_TRANSFER;
            case "booking" -> FinancialTransactionMethod.BOOKING;
            case "airbnb" -> FinancialTransactionMethod.AIRBNB;
            default -> throw new BookingException("Forma de pagamento invalida.");
        };
    }

    private BigDecimal positiveOrDefault(BigDecimal value, BigDecimal defaultValue) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return defaultValue;
        }

        return value;
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (isBlank(value)) {
            return null;
        }

        return value.trim();
    }

    private String normalizeRoomCode(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private String normalizeStatus(String value) {
        return value.trim().toUpperCase(Locale.ROOT).replace("-", "_").replace(" ", "_");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
