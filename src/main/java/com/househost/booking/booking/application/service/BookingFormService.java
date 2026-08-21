package com.househost.booking.booking.application.service;

import com.househost.booking.booking.application.dto.BookingFormCreateRequestDTO;
import com.househost.booking.booking.application.dto.BookingFormCreateResponseDTO;
import com.househost.booking.booking.application.dto.BookingFormGuestDTO;
import com.househost.booking.booking.application.dto.BookingFormReservationDTO;
import com.househost.booking.booking.application.dto.BookingRequestDTO;
import com.househost.booking.booking.application.dto.BookingResponseDTO;
import com.househost.booking.booking.application.port.in.BookingFormUseCase;
import com.househost.booking.booking.application.port.in.BookingUseCase;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanAllocationDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanCreationOutcomeDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanCurrentPaymentDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanDownPaymentDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanFuturePaymentDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanSummaryDTO;
import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionPlanUseCase;
import com.househost.finance.financialtransaction.application.records.ReservationFinancialTransactionPlanCommandRecord;
import com.househost.finance.financialtransaction.domain.model.FinancialPaymentStructure;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.guest.application.service.GuestService;
import com.househost.guest.domain.model.Guest;
import com.househost.room.application.service.RoomService;
import com.househost.room.domain.model.Room;
import com.househost.shared.exception.BookingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class BookingFormService implements BookingFormUseCase {

    private final BookingUseCase bookingUseCase;
    private final FinancialTransactionPlanUseCase financialTransactionPlanUseCase;
    private final GuestService guestService;
    private final RoomService roomService;

    public BookingFormService(
            BookingUseCase bookingUseCase,
            FinancialTransactionPlanUseCase financialTransactionPlanUseCase,
            GuestService guestService,
            RoomService roomService
    ) {
        this.bookingUseCase = bookingUseCase;
        this.financialTransactionPlanUseCase = financialTransactionPlanUseCase;
        this.guestService = guestService;
        this.roomService = roomService;
    }

    @Override
    @Transactional
    public BookingFormCreateResponseDTO create(BookingFormCreateRequestDTO request) {
        validateFormRequest(request);
        if (request.paymentAllocation != null) {
            Optional<FinancialTransactionPlanCreationOutcomeDTO>
                    financialTransactionPlanCreationOutcomeDTOOptional =
                    financialTransactionPlanUseCase.prepareReservationCreation(
                            request.idempotencyKey
                    );
            if (financialTransactionPlanCreationOutcomeDTOOptional.isPresent()) {
                return replay(financialTransactionPlanCreationOutcomeDTOOptional.get());
            }
        }

        Guest guest = findGuestFromForm(request.guest);
        Room room = findRoomFromForm(request.reservation);
        BookingRequestDTO bookingRequestDTO = mapToBookingRequest(request, guest, room);
        BookingResponseDTO bookingResponseDTO = bookingUseCase.create(bookingRequestDTO);
        if (request.paymentAllocation == null) {
            return new BookingFormCreateResponseDTO(bookingResponseDTO, null, false);
        }

        FinancialTransactionPlanSummaryDTO financialTransactionPlanSummaryDTO =
                financialTransactionPlanUseCase.createForReservation(
                        mapToFinancialCommandRecord(
                                request,
                                bookingResponseDTO
                        )
                );
        return new BookingFormCreateResponseDTO(
                bookingResponseDTO,
                financialTransactionPlanSummaryDTO,
                false
        );
    }

    private BookingFormCreateResponseDTO replay(
            FinancialTransactionPlanCreationOutcomeDTO financialTransactionPlanCreationOutcomeDTO
    ) {
        BookingResponseDTO bookingResponseDTO = bookingUseCase.findById(
                financialTransactionPlanCreationOutcomeDTO.getBookingId()
        );
        return new BookingFormCreateResponseDTO(
                bookingResponseDTO,
                financialTransactionPlanCreationOutcomeDTO
                        .getFinancialTransactionPlanSummaryDTO(),
                true
        );
    }

    private BookingRequestDTO mapToBookingRequest(
            BookingFormCreateRequestDTO request,
            Guest guest,
            Room room
    ) {
        BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
        bookingRequestDTO.guestId = guest.getId();
        bookingRequestDTO.roomId = room.getId();
        bookingRequestDTO.checkInDate = request.reservation.checkInDate;
        bookingRequestDTO.checkOutDate = request.reservation.checkOutDate;
        bookingRequestDTO.status = request.status == null
                ? BookingStatus.UNCONFIRMED
                : request.status;
        bookingRequestDTO.origin = request.origin;
        bookingRequestDTO.adults = request.reservation.adults;
        bookingRequestDTO.children = request.reservation.children;
        bookingRequestDTO.pets = request.reservation.pets;
        bookingRequestDTO.dailyRate = request.reservation.dailyRate;
        bookingRequestDTO.discount = request.reservation.discount;
        applyPaymentProjection(bookingRequestDTO, request.paymentAllocation);
        bookingRequestDTO.specialRequests = request.specialRequests;
        bookingRequestDTO.internalNotes = request.internalNotes;
        return bookingRequestDTO;
    }

    private void applyPaymentProjection(
            BookingRequestDTO bookingRequestDTO,
            FinancialTransactionPlanAllocationDTO financialTransactionPlanAllocationDTO
    ) {
        if (financialTransactionPlanAllocationDTO == null) {
            return;
        }
        FinancialTransactionPlanCurrentPaymentDTO currentPaymentDTO =
                financialTransactionPlanAllocationDTO.currentPayment;
        if (currentPaymentDTO != null && Boolean.TRUE.equals(currentPaymentDTO.enabled)) {
            bookingRequestDTO.paymentMethod = currentPaymentDTO.method;
            bookingRequestDTO.paidAmount = currentPaymentDTO.amount;
            bookingRequestDTO.installments = currentPaymentDTO.installmentsQuantity == null
                    ? null
                    : currentPaymentDTO.installmentsQuantity.toString();
            bookingRequestDTO.paymentCompleted = Boolean.TRUE.equals(currentPaymentDTO.received);
            bookingRequestDTO.paymentDate = LocalDate.now();
            return;
        }
        if (financialTransactionPlanAllocationDTO.downPayment == null
                || !Boolean.TRUE.equals(financialTransactionPlanAllocationDTO.downPayment.enabled)) {
            return;
        }
        FinancialTransactionPlanDownPaymentDTO financialTransactionPlanDownPaymentDTO =
                financialTransactionPlanAllocationDTO.downPayment;
        bookingRequestDTO.paymentMethod = financialTransactionPlanDownPaymentDTO.method;
        bookingRequestDTO.installments = financialTransactionPlanDownPaymentDTO
                .installmentsQuantity == null
                ? null
                : financialTransactionPlanDownPaymentDTO.installmentsQuantity.toString();
        bookingRequestDTO.paidAmount = Boolean.TRUE.equals(
                financialTransactionPlanDownPaymentDTO.received
        )
                ? financialTransactionPlanDownPaymentDTO.amount
                : BigDecimal.ZERO;
        bookingRequestDTO.paymentDate = LocalDate.now();
        bookingRequestDTO.paymentCompleted = Boolean.TRUE.equals(
                financialTransactionPlanDownPaymentDTO.received
        );
    }

    private ReservationFinancialTransactionPlanCommandRecord mapToFinancialCommandRecord(
            BookingFormCreateRequestDTO request,
            BookingResponseDTO bookingResponseDTO
    ) {
        FinancialTransactionPlanAllocationDTO financialTransactionPlanAllocationDTO =
                request.paymentAllocation;
        return new ReservationFinancialTransactionPlanCommandRecord(
                bookingResponseDTO.getId(),
                bookingResponseDTO.getGuestId(),
                bookingResponseDTO.getTotalAmount(),
                bookingResponseDTO.getCheckInDate(),
                bookingResponseDTO.getCheckOutDate(),
                request.idempotencyKey,
                mapCurrentPaymentRecord(financialTransactionPlanAllocationDTO.currentPayment),
                mapDownPaymentRecord(financialTransactionPlanAllocationDTO.downPayment),
                mapFuturePaymentRecord(financialTransactionPlanAllocationDTO.checkInPayment),
                mapFuturePaymentRecord(financialTransactionPlanAllocationDTO.checkOutPayment)
        );
    }

    private ReservationFinancialTransactionPlanCommandRecord.CurrentPaymentAllocationRecord
            mapCurrentPaymentRecord(
                    FinancialTransactionPlanCurrentPaymentDTO financialTransactionPlanCurrentPaymentDTO
            ) {
        if (financialTransactionPlanCurrentPaymentDTO == null) {
            return null;
        }
        return new ReservationFinancialTransactionPlanCommandRecord.CurrentPaymentAllocationRecord(
                Boolean.TRUE.equals(financialTransactionPlanCurrentPaymentDTO.enabled),
                financialTransactionPlanCurrentPaymentDTO.amount,
                financialTransactionPlanCurrentPaymentDTO.method,
                financialTransactionPlanCurrentPaymentDTO.installmentsQuantity,
                Boolean.TRUE.equals(financialTransactionPlanCurrentPaymentDTO.received)
        );
    }

    private ReservationFinancialTransactionPlanCommandRecord.DownPaymentAllocationRecord
            mapDownPaymentRecord(
                    FinancialTransactionPlanDownPaymentDTO financialTransactionPlanDownPaymentDTO
            ) {
        if (financialTransactionPlanDownPaymentDTO == null) {
            return null;
        }
        return new ReservationFinancialTransactionPlanCommandRecord.DownPaymentAllocationRecord(
                Boolean.TRUE.equals(financialTransactionPlanDownPaymentDTO.enabled),
                financialTransactionPlanDownPaymentDTO.amount,
                Boolean.TRUE.equals(financialTransactionPlanDownPaymentDTO.received),
                financialTransactionPlanDownPaymentDTO.method,
                financialTransactionPlanDownPaymentDTO.method == FinancialTransactionMethod.CREDIT_CARD
                        && financialTransactionPlanDownPaymentDTO.installmentsQuantity != null
                        ? FinancialPaymentStructure.INSTALLMENT
                        : FinancialPaymentStructure.SIMPLE,
                financialTransactionPlanDownPaymentDTO.installmentsQuantity,
                financialTransactionPlanDownPaymentDTO.installmentsQuantity == null
                        ? null
                        : LocalDate.now().getDayOfMonth(),
                LocalDate.now()
        );
    }

    private ReservationFinancialTransactionPlanCommandRecord.FuturePaymentAllocationRecord
            mapFuturePaymentRecord(
                    FinancialTransactionPlanFuturePaymentDTO financialTransactionPlanFuturePaymentDTO
            ) {
        if (financialTransactionPlanFuturePaymentDTO == null) {
            return null;
        }
        return new ReservationFinancialTransactionPlanCommandRecord.FuturePaymentAllocationRecord(
                Boolean.TRUE.equals(financialTransactionPlanFuturePaymentDTO.enabled),
                financialTransactionPlanFuturePaymentDTO.amount,
                Boolean.TRUE.equals(financialTransactionPlanFuturePaymentDTO.received)
        );
    }

    private Guest findGuestFromForm(BookingFormGuestDTO bookingFormGuestDTO) {
        String documentNumber = normalizeOptional(bookingFormGuestDTO.documentNumber);
        if (documentNumber != null) {
            return guestService.findGuestByDocumentNumber(documentNumber);
        }
        return guestService.findUniqueGuestByFullName(bookingFormGuestDTO.fullName);
    }

    private Room findRoomFromForm(BookingFormReservationDTO bookingFormReservationDTO) {
        if (bookingFormReservationDTO.roomId != null) {
            return roomService.findRoomById(bookingFormReservationDTO.roomId);
        }
        return roomService.findRoomByNumber(bookingFormReservationDTO.roomCode.trim());
    }

    private void validateFormRequest(BookingFormCreateRequestDTO request) {
        if (request == null) {
            throw new BookingException("Dados do formulario de reserva sao obrigatorios.");
        }
        if (request.guest == null
                || isBlank(request.guest.fullName)
                && isBlank(request.guest.documentNumber)) {
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
        if (request.paymentAllocation != null && isBlank(request.idempotencyKey)) {
            throw new BookingException("Chave de idempotencia financeira e obrigatoria.");
        }
    }

    private String normalizeOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
