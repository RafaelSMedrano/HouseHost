package com.househost.publicapi.application.service;

import com.househost.audit.domain.model.AuditEventContext;
import com.househost.publicapi.application.port.in.PublicBookingUseCase;
import com.househost.publicapi.application.port.out.PublicBookingAuditPort;
import com.househost.booking.booking.application.port.out.BookingPersistencePort;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingOrigin;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.guest.domain.model.Guest;
import com.househost.privacy.policy.application.port.in.PublicPrivacyPolicyUseCase;
import com.househost.privacy.policy.application.records.PublishedPrivacyPolicyRecord;
import com.househost.publicapi.application.dto.PublicAvailabilityResponseDTO;
import com.househost.publicapi.application.dto.PublicBookingRequestDTO;
import com.househost.publicapi.application.dto.PublicBookingResponseDTO;
import com.househost.publicapi.application.dto.PublicQuoteRequestDTO;
import com.househost.publicapi.application.dto.PublicQuoteResponseDTO;
import com.househost.publicapi.application.dto.PublicRoomResponseDTO;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import com.househost.room.application.service.RoomService;
import com.househost.shared.exception.BookingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class PublicBookingService implements PublicBookingUseCase {

    private static final List<BookingStatus> BLOCKING_STATUSES = List.of(
            BookingStatus.UNCONFIRMED,
            BookingStatus.CONFIRMED,
            BookingStatus.IN_STAY
    );
    private static final Pattern CPF_LIKE_PATTERN = Pattern.compile(
            "(?<!\\d)\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}(?!\\d)"
    );
    private static final Pattern CARD_LIKE_PATTERN = Pattern.compile(
            "(?<!\\d)(?:\\d[ -]?){13,19}(?!\\d)"
    );
    private static final Pattern HUMAN_NAME_PATTERN = Pattern.compile("[\\p{L}][\\p{L} .'-]*");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
    );
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 80;
    private static final int MAX_CITY_LENGTH = 120;
    private static final int MAX_EMAIL_LENGTH = 255;
    private static final int MAX_NOTES_LENGTH = 500;
    private static final int MAX_VERSION_LENGTH = 100;
    private static final int MAX_PUBLIC_GUESTS = 20;
    private static final int MAX_PUBLIC_PETS = 5;

    private final RoomService roomService;
    private final PublicBookingParticipantNotifier publicBookingParticipantNotifier;
    private final BookingPersistencePort bookingRepository;
    private final PublicBookingAuditPort publicBookingAuditPort;
    private final PublicPrivacyPolicyUseCase publicPrivacyPolicyUseCase;

    public PublicBookingService(
            RoomService roomService,
            PublicBookingParticipantNotifier publicBookingParticipantNotifier,
            BookingPersistencePort bookingRepository,
            PublicBookingAuditPort publicBookingAuditPort,
            PublicPrivacyPolicyUseCase publicPrivacyPolicyUseCase
    ) {
        this.roomService = roomService;
        this.publicBookingParticipantNotifier = publicBookingParticipantNotifier;
        this.bookingRepository = bookingRepository;
        this.publicBookingAuditPort = publicBookingAuditPort;
        this.publicPrivacyPolicyUseCase = publicPrivacyPolicyUseCase;
    }

    public List<PublicRoomResponseDTO> findPublicRooms() {
        return findAvailablePublicRooms()
                .stream()
                .map(this::toPublicRoomResponse)
                .toList();
    }

    public PublicAvailabilityResponseDTO checkAvailability(
            Long roomId,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer guests
    ) {
        validateDates(checkIn, checkOut);
        if (guests != null && (guests < 1 || guests > MAX_PUBLIC_GUESTS)) {
            throw new BookingException("Quantidade de hospedes deve estar entre 1 e 20.");
        }
        Room room = findPublicRoom(roomId);

        if (guests != null && guests > room.getCapacity()) {
            throw new BookingException("Quantidade de hospedes excede a capacidade da casa.");
        }

        List<Booking> conflicts = findConflicts(room.getId(), checkIn, checkOut);
        return new PublicAvailabilityResponseDTO(
                conflicts.isEmpty(),
                room.getId(),
                checkIn,
                checkOut,
                ChronoUnit.DAYS.between(checkIn, checkOut),
                conflicts.stream()
                        .map(booking -> new PublicAvailabilityResponseDTO.BlockedDateRangeDTO(
                                booking.getCheckInDate(),
                                booking.getCheckOutDate()
                        ))
                        .toList()
        );
    }

    public PublicQuoteResponseDTO quote(PublicQuoteRequestDTO request) {
        if (request == null) {
            throw new BookingException("Dados da cotacao sao obrigatorios.");
        }

        validateDates(request.checkIn, request.checkOut);
        validateGuestComposition(request.adults, request.children, request.pets);
        Room room = findPublicRoom(request.roomId);
        ensureGuestCapacity(room, request.adults, request.children);

        long nights = ChronoUnit.DAYS.between(request.checkIn, request.checkOut);
        BigDecimal subtotal = room.getDailyRate().multiply(BigDecimal.valueOf(nights));
        BigDecimal fees = BigDecimal.ZERO;
        boolean available = findConflicts(room.getId(), request.checkIn, request.checkOut).isEmpty();

        return new PublicQuoteResponseDTO(
                available,
                room.getId(),
                room.getDailyRate(),
                nights,
                subtotal,
                fees,
                subtotal.add(fees),
                "BRL"
        );
    }

    @Transactional
    public PublicBookingResponseDTO createBooking(PublicBookingRequestDTO request) {
        return createBooking(request, null);
    }

    @Transactional
    public PublicBookingResponseDTO createBooking(
            PublicBookingRequestDTO request,
            AuditEventContext auditContext
    ) {
        validateBookingRequest(request);
        PublishedPrivacyPolicyRecord publishedPrivacyPolicyRecord =
                publicPrivacyPolicyUseCase.requireCurrentPublishedForAcceptance(
                        request.privacyPolicyId
                );
        Room room = findPublicRoom(request.roomId);
        ensureGuestCapacity(room, request.adults, request.children);

        if (!findConflicts(room.getId(), request.checkIn, request.checkOut).isEmpty()) {
            throw new BookingException("Casa ja possui reserva no periodo informado.");
        }

        Guest guest = publicBookingParticipantNotifier.notifyGuestCreation(request.guest);
        BigDecimal total = calculateTotal(room, request.checkIn, request.checkOut);
        Booking booking = new Booking(
                guest,
                room,
                request.checkIn,
                request.checkOut,
                BookingStatus.UNCONFIRMED,
                total,
                BookingOrigin.DIRETO_TELEFONE,
                request.adults,
                request.children,
                request.pets,
                null,
                null,
                room.getDailyRate(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                normalizeOptional(request.notes),
                "Reserva criada pelo site publico"
        );
        booking.registerPrivacyAcceptance(
                Integer.toString(publishedPrivacyPolicyRecord.version()),
                publishedPrivacyPolicyRecord.contentHash(),
                normalizeOptional(request.termsVersion)
        );

        Booking savedBooking = bookingRepository.save(booking);
        publicBookingParticipantNotifier.notifyBookingCreation(savedBooking);
        publicBookingParticipantNotifier.notifyReservationRequest(savedBooking);
        recordPublicBookingAuditEvents(savedBooking, request, auditContext);
        return new PublicBookingResponseDTO(
                "CL-" + savedBooking.getId(),
                savedBooking.getId(),
                savedBooking.getStatus(),
                savedBooking.getTotalAmount(),
                "Solicitacao recebida. Entraremos em contato pelo WhatsApp informado."
        );
    }

    private void recordPublicBookingAuditEvents(
            Booking booking,
            PublicBookingRequestDTO request,
            AuditEventContext auditContext
    ) {
        Map<String, Object> bookingMetadataMap = Map.of(
                "source", "PUBLIC_SITE",
                "status", booking.getStatus().name(),
                "checkIn", booking.getCheckInDate().toString(),
                "checkOut", booking.getCheckOutDate().toString(),
                "adults", request.adults,
                "children", request.children,
                "pets", request.pets
        );

        publicBookingAuditPort.recordBookingEvent(
                "PUBLIC_BOOKING_CREATED",
                "BOOKING",
                booking.getId(),
                "PUBLIC_GUEST",
                booking.getGuest().getId(),
                booking.getGuest().getFullName(),
                auditContext,
                bookingMetadataMap
        );

        publicBookingAuditPort.recordBookingEvent(
                "PRIVACY_ACCEPTED",
                "BOOKING",
                booking.getId(),
                "PUBLIC_GUEST",
                booking.getGuest().getId(),
                booking.getGuest().getFullName(),
                auditContext,
                Map.of(
                        "privacyPolicyVersion", emptyIfNull(booking.getPrivacyPolicyVersion()),
                        "privacyPolicyContentHash", emptyIfNull(booking.getPrivacyPolicyContentHash()),
                        "termsVersion", emptyIfNull(booking.getTermsVersion())
                )
        );
    }

    private PublicRoomResponseDTO toPublicRoomResponse(Room room) {
        return new PublicRoomResponseDTO(
                room.getId(),
                "Casa privativa",
                room.getRoomNumber(),
                room.getCapacity(),
                room.getDailyRate(),
                room.getStatus(),
                List.of("Casa inteira", "2 quartos", "Sala", "Cozinha", "Banheiro", "Area externa")
        );
    }

    private List<Room> findAvailablePublicRooms() {
        return roomService.findAllRooms()
                .stream()
                .filter(room -> room.getStatus() != RoomStatus.INACTIVE)
                .toList();
    }

    private Room findPublicRoom(Long roomId) {
        Optional<Room> roomOptional = roomId == null
                ? findAvailablePublicRooms().stream().findFirst()
                : Optional.of(roomService.findRoomById(roomId));

        return roomOptional
                .filter(room -> room.getStatus() != RoomStatus.INACTIVE)
                .orElseThrow(() -> new BookingException("Casa nao encontrada para reserva publica."));
    }

    private List<Booking> findConflicts(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return bookingRepository.findOverlappingBookings(roomId, checkIn, checkOut, BLOCKING_STATUSES);
    }

    private void validateBookingRequest(PublicBookingRequestDTO request) {
        if (request == null) {
            throw new BookingException("Dados da reserva sao obrigatorios.");
        }

        validateDates(request.checkIn, request.checkOut);
        validateGuestComposition(request.adults, request.children, request.pets);

        if (request.guest == null) {
            throw new BookingException("Dados do hospede sao obrigatorios.");
        }

        request.guest.firstName = normalizeAndValidateName(request.guest.firstName, "Nome");
        request.guest.lastName = normalizeAndValidateName(request.guest.lastName, "Sobrenome");
        request.guest.email = normalizeAndValidateEmail(request.guest.email);
        request.guest.phone = normalizeBrazilianPhone(request.guest.phone);
        request.guest.city = normalizeOptionalWithLimit(request.guest.city, MAX_CITY_LENGTH, "Cidade");
        request.notes = normalizeOptionalWithLimit(request.notes, MAX_NOTES_LENGTH, "Observacoes");
        request.termsVersion = normalizeOptionalWithLimit(
                request.termsVersion,
                MAX_VERSION_LENGTH,
                "Versao dos termos"
        );

        if (Boolean.TRUE != request.privacyAccepted) {
            throw new BookingException("Aceite dos termos e da politica de privacidade e obrigatorio.");
        }
        if (request.privacyPolicyId == null) {
            throw new BookingException("Politica de privacidade e obrigatoria.");
        }

        rejectOutOfScopeData(request);
    }

    private void rejectOutOfScopeData(PublicBookingRequestDTO request) {
        if (looksLikeCpf(request.notes) || looksLikeCard(request.notes)) {
            throw new BookingException("Nao envie CPF, cartao ou dados financeiros nas observacoes.");
        }

        if (looksLikeCpf(request.guest.firstName)
                || looksLikeCpf(request.guest.lastName)
                || looksLikeCpf(request.guest.city)) {
            throw new BookingException("CPF nao deve ser enviado pelo site publico nesta etapa.");
        }

        if (looksLikeCard(request.guest.firstName)
                || looksLikeCard(request.guest.lastName)
                || looksLikeCard(request.guest.city)) {
            throw new BookingException("Dados financeiros nao devem ser enviados pelo site publico nesta etapa.");
        }
    }

    private boolean looksLikeCpf(String value) {
        return value != null && CPF_LIKE_PATTERN.matcher(value).find();
    }

    private boolean looksLikeCard(String value) {
        return value != null && CARD_LIKE_PATTERN.matcher(value.replaceAll("\\D", "")).find();
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null) {
            throw new BookingException("Data de check-in e obrigatoria.");
        }

        if (checkOut == null) {
            throw new BookingException("Data de check-out e obrigatoria.");
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new BookingException("Data de check-out deve ser posterior a data de check-in.");
        }
    }

    private void ensureGuestCapacity(Room room, Integer adults, Integer children) {
        int totalGuests = adults + children;
        if (totalGuests > room.getCapacity()) {
            throw new BookingException("Quantidade de hospedes excede a capacidade da casa.");
        }
    }

    private BigDecimal calculateTotal(Room room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return room.getDailyRate().multiply(BigDecimal.valueOf(nights));
    }

    private String normalizeOptional(String value) {
        if (isBlank(value)) {
            return null;
        }

        return value.trim();
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private void validateGuestComposition(Integer adults, Integer children, Integer pets) {
        if (adults == null || adults < 1 || adults > MAX_PUBLIC_GUESTS) {
            throw new BookingException("Quantidade de adultos deve estar entre 1 e 20.");
        }
        if (children == null || children < 0 || children > MAX_PUBLIC_GUESTS) {
            throw new BookingException("Quantidade de criancas deve estar entre 0 e 20.");
        }
        if (adults + children > MAX_PUBLIC_GUESTS) {
            throw new BookingException("A solicitacao publica aceita no maximo 20 hospedes.");
        }
        if (pets == null || pets < 0 || pets > MAX_PUBLIC_PETS) {
            throw new BookingException("Quantidade de pets deve estar entre 0 e 5.");
        }
    }

    private String normalizeAndValidateName(String value, String fieldName) {
        if (isBlank(value)) {
            throw new BookingException(fieldName + " e obrigatorio.");
        }
        String normalizedValue = value.trim().replaceAll("\\s+", " ");
        if (normalizedValue.length() < MIN_NAME_LENGTH || normalizedValue.length() > MAX_NAME_LENGTH) {
            throw new BookingException(fieldName + " deve conter entre 2 e 80 caracteres.");
        }
        if (!HUMAN_NAME_PATTERN.matcher(normalizedValue).matches()) {
            throw new BookingException(fieldName + " contem caracteres invalidos.");
        }
        return normalizedValue;
    }

    private String normalizeBrazilianPhone(String value) {
        if (isBlank(value)) {
            throw new BookingException("Telefone do hospede e obrigatorio.");
        }
        if (value.length() > 32) {
            throw new BookingException("Telefone excede 32 caracteres.");
        }
        String digits = value.replaceAll("\\D", "");
        if ((digits.length() == 12 || digits.length() == 13) && digits.startsWith("55")) {
            digits = digits.substring(2);
        }
        if (digits.length() != 10 && digits.length() != 11) {
            throw new BookingException("Telefone deve conter DDD e 10 ou 11 digitos.");
        }
        return "+55" + digits;
    }

    private String normalizeAndValidateEmail(String value) {
        if (isBlank(value)) {
            throw new BookingException("Email do hospede e obrigatorio.");
        }
        String normalizedEmail = value.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalizedEmail.length() > MAX_EMAIL_LENGTH
                || normalizedEmail.contains("\r")
                || normalizedEmail.contains("\n")
                || !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new BookingException("Email do hospede deve ser valido.");
        }
        return normalizedEmail;
    }

    private String normalizeOptionalWithLimit(String value, int maxLength, String fieldName) {
        String normalizedValue = normalizeOptional(value);
        if (normalizedValue != null && normalizedValue.length() > maxLength) {
            throw new BookingException(fieldName + " excede " + maxLength + " caracteres.");
        }
        return normalizedValue;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
