package com.househost.booking.booking.application.service;

import com.househost.booking.booking.application.dto.BookingFormCreateRequestDTO;
import com.househost.booking.booking.application.dto.BookingFormCreateResponseDTO;
import com.househost.booking.booking.application.dto.BookingFormGuestDTO;
import com.househost.booking.booking.application.dto.BookingFormReservationDTO;
import com.househost.booking.booking.application.dto.BookingResponseDTO;
import com.househost.booking.booking.application.port.in.BookingUseCase;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanAllocationDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanCreationOutcomeDTO;
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
import com.househost.room.domain.model.RoomStatus;
import com.househost.room.domain.model.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BookingFormFinancialTransactionPlanTest {

    private BookingUseCase bookingUseCase;
    private FinancialTransactionPlanUseCase financialTransactionPlanUseCase;
    private GuestService guestService;
    private RoomService roomService;
    private BookingFormService bookingFormService;

    @BeforeEach
    void setUp() {
        bookingUseCase = mock(BookingUseCase.class);
        financialTransactionPlanUseCase = mock(FinancialTransactionPlanUseCase.class);
        guestService = mock(GuestService.class);
        roomService = mock(RoomService.class);
        bookingFormService = new BookingFormService(
                bookingUseCase,
                financialTransactionPlanUseCase,
                guestService,
                roomService
        );
    }

    @Test
    void keepsBookingOnlyCreationWhenAllocationDoesNotApply() {
        BookingFormCreateRequestDTO bookingFormCreateRequestDTO = request(false);
        configureNewBooking();

        BookingFormCreateResponseDTO bookingFormCreateResponseDTO =
                bookingFormService.create(bookingFormCreateRequestDTO);

        assertFalse(bookingFormCreateResponseDTO.isIdempotentReplay());
        verify(bookingUseCase).create(any());
        verifyNoInteractions(financialTransactionPlanUseCase);
    }

    @Test
    void createsBookingThenOneTrustedFinancialPlanCommand() {
        BookingFormCreateRequestDTO bookingFormCreateRequestDTO = request(true);
        configureNewBooking();
        when(financialTransactionPlanUseCase.prepareReservationCreation("reservation-command"))
                .thenReturn(Optional.empty());
        FinancialTransactionPlanSummaryDTO financialTransactionPlanSummaryDTO =
                mock(FinancialTransactionPlanSummaryDTO.class);
        when(financialTransactionPlanUseCase.createForReservation(any()))
                .thenReturn(financialTransactionPlanSummaryDTO);

        BookingFormCreateResponseDTO bookingFormCreateResponseDTO =
                bookingFormService.create(bookingFormCreateRequestDTO);

        ArgumentCaptor<ReservationFinancialTransactionPlanCommandRecord> commandRecordCaptor =
                ArgumentCaptor.forClass(ReservationFinancialTransactionPlanCommandRecord.class);
        verify(financialTransactionPlanUseCase).createForReservation(commandRecordCaptor.capture());
        assertTrue(commandRecordCaptor.getValue().downPaymentAllocationRecord().enabled());
        assertTrue(commandRecordCaptor.getValue().checkInPaymentAllocationRecord().enabled());
        assertFalse(bookingFormCreateResponseDTO.isIdempotentReplay());
    }

    @Test
    void replayReturnsAuthoritativeBookingAndPlanWithoutCreatingAnotherBooking() {
        BookingFormCreateRequestDTO bookingFormCreateRequestDTO = request(true);
        BookingResponseDTO bookingResponseDTO = bookingResponseDTO();
        FinancialTransactionPlanSummaryDTO financialTransactionPlanSummaryDTO =
                mock(FinancialTransactionPlanSummaryDTO.class);
        when(financialTransactionPlanUseCase.prepareReservationCreation("reservation-command"))
                .thenReturn(Optional.of(new FinancialTransactionPlanCreationOutcomeDTO(
                        40L,
                        financialTransactionPlanSummaryDTO
                )));
        when(bookingUseCase.findById(40L)).thenReturn(bookingResponseDTO);

        BookingFormCreateResponseDTO bookingFormCreateResponseDTO =
                bookingFormService.create(bookingFormCreateRequestDTO);

        assertTrue(bookingFormCreateResponseDTO.isIdempotentReplay());
        verify(bookingUseCase, never()).create(any());
        verify(financialTransactionPlanUseCase, never()).createForReservation(any());
        verifyNoInteractions(guestService, roomService);
    }

    @Test
    void bookingAndFinancialPlanShareOneTransactionalBoundary() throws Exception {
        Method createMethod = BookingFormService.class.getMethod(
                "create",
                BookingFormCreateRequestDTO.class
        );

        Transactional transactional = createMethod.getAnnotation(Transactional.class);

        assertNotNull(transactional);
    }

    @Test
    void propagatesFinancialPlanFailureFromTheBookingTransaction() {
        BookingFormCreateRequestDTO bookingFormCreateRequestDTO = request(true);
        configureNewBooking();
        when(financialTransactionPlanUseCase.prepareReservationCreation("reservation-command"))
                .thenReturn(Optional.empty());
        when(financialTransactionPlanUseCase.createForReservation(any()))
                .thenThrow(new IllegalStateException("financial plan failed"));

        assertThrows(
                IllegalStateException.class,
                () -> bookingFormService.create(bookingFormCreateRequestDTO)
        );

        verify(bookingUseCase).create(any());
        verify(financialTransactionPlanUseCase).createForReservation(any());
    }

    private void configureNewBooking() {
        Guest guest = new Guest("Maria Silva", null, "+5512999999999", null, null, null);
        guest.restorePersistenceState(7L, null, List.of(), null, null);
        Room room = new Room(
                "101",
                RoomType.DOUBLE,
                2,
                new BigDecimal("500.00"),
                RoomStatus.AVAILABLE
        );
        room.restorePersistenceState(1L, null, null);
        when(guestService.findUniqueGuestByFullName("Maria Silva")).thenReturn(guest);
        when(roomService.findRoomById(1L)).thenReturn(room);
        BookingResponseDTO bookingResponseDTO = bookingResponseDTO();
        when(bookingUseCase.create(any())).thenReturn(bookingResponseDTO);
    }

    private BookingResponseDTO bookingResponseDTO() {
        BookingResponseDTO bookingResponseDTO = mock(BookingResponseDTO.class);
        when(bookingResponseDTO.getId()).thenReturn(40L);
        when(bookingResponseDTO.getGuestId()).thenReturn(7L);
        when(bookingResponseDTO.getTotalAmount()).thenReturn(new BigDecimal("1000.00"));
        when(bookingResponseDTO.getCheckInDate()).thenReturn(LocalDate.now().plusMonths(1));
        when(bookingResponseDTO.getCheckOutDate()).thenReturn(LocalDate.now().plusMonths(2));
        return bookingResponseDTO;
    }

    private BookingFormCreateRequestDTO request(boolean withAllocation) {
        BookingFormCreateRequestDTO bookingFormCreateRequestDTO =
                new BookingFormCreateRequestDTO();
        bookingFormCreateRequestDTO.guest = new BookingFormGuestDTO();
        bookingFormCreateRequestDTO.guest.fullName = "Maria Silva";
        bookingFormCreateRequestDTO.reservation = new BookingFormReservationDTO();
        bookingFormCreateRequestDTO.reservation.roomId = 1L;
        bookingFormCreateRequestDTO.reservation.checkInDate = LocalDate.now().plusMonths(1);
        bookingFormCreateRequestDTO.reservation.checkOutDate = LocalDate.now().plusMonths(2);
        bookingFormCreateRequestDTO.reservation.dailyRate = new BigDecimal("500.00");
        if (!withAllocation) {
            return bookingFormCreateRequestDTO;
        }
        bookingFormCreateRequestDTO.idempotencyKey = "reservation-command";
        bookingFormCreateRequestDTO.paymentAllocation = new FinancialTransactionPlanAllocationDTO();
        bookingFormCreateRequestDTO.paymentAllocation.downPayment =
                new FinancialTransactionPlanDownPaymentDTO();
        bookingFormCreateRequestDTO.paymentAllocation.downPayment.enabled = true;
        bookingFormCreateRequestDTO.paymentAllocation.downPayment.amount = new BigDecimal("200.00");
        bookingFormCreateRequestDTO.paymentAllocation.downPayment.received = false;
        bookingFormCreateRequestDTO.paymentAllocation.downPayment.method =
                FinancialTransactionMethod.PIX;
        bookingFormCreateRequestDTO.paymentAllocation.downPayment.structure =
                FinancialPaymentStructure.SIMPLE;
        bookingFormCreateRequestDTO.paymentAllocation.downPayment.paymentDate =
                LocalDate.now().plusDays(2);
        bookingFormCreateRequestDTO.paymentAllocation.checkInPayment =
                futurePayment(new BigDecimal("300.00"));
        bookingFormCreateRequestDTO.paymentAllocation.checkOutPayment =
                futurePayment(new BigDecimal("500.00"));
        return bookingFormCreateRequestDTO;
    }

    private FinancialTransactionPlanFuturePaymentDTO futurePayment(BigDecimal amount) {
        FinancialTransactionPlanFuturePaymentDTO financialTransactionPlanFuturePaymentDTO =
                new FinancialTransactionPlanFuturePaymentDTO();
        financialTransactionPlanFuturePaymentDTO.enabled = true;
        financialTransactionPlanFuturePaymentDTO.amount = amount;
        return financialTransactionPlanFuturePaymentDTO;
    }
}
