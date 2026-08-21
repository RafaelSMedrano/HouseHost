package com.househost.booking.booking.application.service;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.booking.checking.application.dto.CheckInRequestDTO;
import com.househost.booking.checking.application.dto.CheckInResponseDTO;
import com.househost.booking.checking.application.port.out.CheckInAuditPort;
import com.househost.booking.checking.application.port.out.CheckInPersistencePort;
import com.househost.booking.checking.application.service.CheckInBookingResolver;
import com.househost.booking.checking.application.service.CheckInFinancialResolver;
import com.househost.booking.checking.application.service.CheckInGuestResolver;
import com.househost.booking.checking.application.service.CheckInParticipantNotifier;
import com.househost.booking.checking.application.service.CheckInRoomResolver;
import com.househost.booking.checking.application.service.CheckInService;
import com.househost.booking.checking.application.service.CheckInValidationService;
import com.househost.booking.checking.domain.model.CheckIn;
import com.househost.booking.checking.domain.model.CheckInStatus;
import com.househost.booking.checkout.application.dto.CheckOutRatingRequestDTO;
import com.househost.booking.checkout.application.dto.CheckOutRequestDTO;
import com.househost.booking.checkout.application.dto.CheckOutResponseDTO;
import com.househost.booking.checkout.application.port.out.CheckOutAuditPort;
import com.househost.booking.checkout.application.port.out.CheckOutPersistencePort;
import com.househost.booking.checkout.application.service.CheckOutBookingResolver;
import com.househost.booking.checkout.application.service.CheckOutFinancialResolver;
import com.househost.booking.checkout.application.service.CheckOutGuestResolver;
import com.househost.booking.checkout.application.service.CheckOutParticipantNotifier;
import com.househost.booking.checkout.application.service.CheckOutRatingResolver;
import com.househost.booking.checkout.application.service.CheckOutRoomResolver;
import com.househost.booking.checkout.application.service.CheckOutService;
import com.househost.booking.checkout.application.service.CheckOutValidationService;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.booking.checkout.domain.model.CheckOutStatus;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanMaterializationDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanReplacementOutcomeDTO;
import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionPlanReplacementUseCase;
import com.househost.finance.financialtransaction.application.records.FinancialTransactionPlanMaterializationCommandRecord;
import com.househost.finance.financialtransaction.domain.model.FinancialPaymentStructure;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import com.househost.guest.application.service.GuestService;
import com.househost.guest.domain.model.Guest;
import com.househost.room.application.service.RoomService;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import com.househost.room.domain.model.RoomType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StayOperationalFinancialMaterializationTest {

    @Test
    void completedCheckInMaterializesOnlyItsBookingPurposeAndReturnsOutcome() {
        Booking booking = booking(BookingStatus.CONFIRMED);
        FinancialTransactionPlanReplacementUseCase financialTransactionPlanReplacementUseCase =
                mock(FinancialTransactionPlanReplacementUseCase.class);
        FinancialTransactionPlanReplacementOutcomeDTO
                financialTransactionPlanReplacementOutcomeDTO =
                mock(FinancialTransactionPlanReplacementOutcomeDTO.class);
        when(financialTransactionPlanReplacementUseCase.materializeForBooking(any()))
                .thenReturn(Optional.of(financialTransactionPlanReplacementOutcomeDTO));
        CheckInPersistencePort checkInPersistencePort = mock(CheckInPersistencePort.class);
        when(checkInPersistencePort.save(any(CheckIn.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(checkInPersistencePort.findByBookingId(23L)).thenReturn(Optional.empty());
        BookingService bookingService = mock(BookingService.class);
        when(bookingService.findBooking(23L)).thenReturn(booking);
        CheckInParticipantNotifier checkInParticipantNotifier = new CheckInParticipantNotifier(
                mock(CheckInBookingResolver.class),
                mock(CheckInGuestResolver.class),
                mock(CheckInRoomResolver.class),
                new CheckInFinancialResolver(financialTransactionPlanReplacementUseCase)
        );
        CheckInService checkInService = new CheckInService(
                checkInPersistencePort,
                bookingService,
                mock(GuestService.class),
                mock(RoomService.class),
                checkInParticipantNotifier,
                mock(CheckInAuditPort.class),
                new CheckInValidationService(checkInPersistencePort)
        );
        CheckInRequestDTO checkInRequestDTO = checkInRequest();

        CheckInResponseDTO checkInResponseDTO = checkInService.create(checkInRequestDTO);

        assertSame(
                financialTransactionPlanReplacementOutcomeDTO,
                checkInResponseDTO.getPaymentMaterialization()
        );
        ArgumentCaptor<FinancialTransactionPlanMaterializationCommandRecord>
                financialTransactionPlanMaterializationCommandRecordCaptor =
                ArgumentCaptor.forClass(FinancialTransactionPlanMaterializationCommandRecord.class);
        verify(financialTransactionPlanReplacementUseCase).materializeForBooking(
                financialTransactionPlanMaterializationCommandRecordCaptor.capture()
        );
        FinancialTransactionPlanMaterializationCommandRecord
                financialTransactionPlanMaterializationCommandRecord =
                financialTransactionPlanMaterializationCommandRecordCaptor.getValue();
        assertEquals(23L, financialTransactionPlanMaterializationCommandRecord.bookingId());
        assertEquals(
                FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                financialTransactionPlanMaterializationCommandRecord.purpose()
        );
    }

    @Test
    void completedCheckoutKeepsExtraRatingAndScheduledPaymentIndependent() {
        Booking booking = booking(BookingStatus.IN_STAY);
        FinancialTransactionPlanReplacementUseCase financialTransactionPlanReplacementUseCase =
                mock(FinancialTransactionPlanReplacementUseCase.class);
        FinancialTransactionPlanReplacementOutcomeDTO
                financialTransactionPlanReplacementOutcomeDTO =
                mock(FinancialTransactionPlanReplacementOutcomeDTO.class);
        when(financialTransactionPlanReplacementUseCase.materializeForBooking(any()))
                .thenReturn(Optional.of(financialTransactionPlanReplacementOutcomeDTO));
        CheckOutPersistencePort checkOutPersistencePort = mock(CheckOutPersistencePort.class);
        when(checkOutPersistencePort.save(any(CheckOut.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        BookingService bookingService = mock(BookingService.class);
        when(bookingService.findBooking(23L)).thenReturn(booking);
        CheckOutRatingResolver checkOutRatingResolver = mock(CheckOutRatingResolver.class);
        CheckOutParticipantNotifier checkOutParticipantNotifier =
                new CheckOutParticipantNotifier(
                        mock(CheckOutBookingResolver.class),
                        mock(CheckOutRoomResolver.class),
                        mock(CheckOutGuestResolver.class),
                        checkOutRatingResolver,
                        new CheckOutFinancialResolver(financialTransactionPlanReplacementUseCase)
                );
        CheckOutService checkOutService = new CheckOutService(
                checkOutPersistencePort,
                bookingService,
                checkOutParticipantNotifier,
                mock(CheckOutAuditPort.class),
                mock(CheckOutValidationService.class)
        );
        CheckOutRequestDTO checkOutRequestDTO = checkOutRequest();

        CheckOutResponseDTO checkOutResponseDTO = checkOutService.create(checkOutRequestDTO);

        assertSame(
                financialTransactionPlanReplacementOutcomeDTO,
                checkOutResponseDTO.getPaymentMaterialization()
        );
        assertEquals(new BigDecimal("75.00"), checkOutResponseDTO.getExtraCharges());
        assertEquals(new BigDecimal("20.00"), checkOutResponseDTO.getPendingAmount());
        ArgumentCaptor<FinancialTransactionPlanMaterializationCommandRecord>
                financialTransactionPlanMaterializationCommandRecordCaptor =
                ArgumentCaptor.forClass(FinancialTransactionPlanMaterializationCommandRecord.class);
        verify(financialTransactionPlanReplacementUseCase).materializeForBooking(
                financialTransactionPlanMaterializationCommandRecordCaptor.capture()
        );
        assertEquals(
                FinancialTransactionType.PLAN_CHECK_OUT_PAYMENT,
                financialTransactionPlanMaterializationCommandRecordCaptor
                        .getValue().purpose()
        );
    }

    @Test
    void financialFailurePreventsCheckInSuccessAudit() {
        Booking booking = booking(BookingStatus.CONFIRMED);
        FinancialTransactionPlanReplacementUseCase financialTransactionPlanReplacementUseCase =
                mock(FinancialTransactionPlanReplacementUseCase.class);
        when(financialTransactionPlanReplacementUseCase.materializeForBooking(any()))
                .thenThrow(new IllegalStateException("financial replacement failed"));
        CheckInPersistencePort checkInPersistencePort = mock(CheckInPersistencePort.class);
        when(checkInPersistencePort.save(any(CheckIn.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(checkInPersistencePort.findByBookingId(23L)).thenReturn(Optional.empty());
        BookingService bookingService = mock(BookingService.class);
        when(bookingService.findBooking(23L)).thenReturn(booking);
        CheckInAuditPort checkInAuditPort = mock(CheckInAuditPort.class);
        CheckInService checkInService = new CheckInService(
                checkInPersistencePort,
                bookingService,
                mock(GuestService.class),
                mock(RoomService.class),
                new CheckInParticipantNotifier(
                        mock(CheckInBookingResolver.class),
                        mock(CheckInGuestResolver.class),
                        mock(CheckInRoomResolver.class),
                        new CheckInFinancialResolver(
                                financialTransactionPlanReplacementUseCase
                        )
                ),
                checkInAuditPort,
                new CheckInValidationService(checkInPersistencePort)
        );

        assertThrows(
                IllegalStateException.class,
                () -> checkInService.create(checkInRequest())
        );

        verify(checkInAuditPort, never()).record(any(), any(), anyMap());
    }

    private CheckInRequestDTO checkInRequest() {
        CheckInRequestDTO checkInRequestDTO = new CheckInRequestDTO();
        checkInRequestDTO.bookingId = 23L;
        checkInRequestDTO.adults = 2;
        checkInRequestDTO.children = 0;
        checkInRequestDTO.pets = 0;
        checkInRequestDTO.status = CheckInStatus.COMPLETED;
        checkInRequestDTO.paymentMaterialization = materialization();
        return checkInRequestDTO;
    }

    private CheckOutRequestDTO checkOutRequest() {
        CheckOutRequestDTO checkOutRequestDTO = new CheckOutRequestDTO();
        checkOutRequestDTO.bookingId = 23L;
        checkOutRequestDTO.actualCheckOutAt = LocalDateTime.now();
        checkOutRequestDTO.roomInspected = true;
        checkOutRequestDTO.extraCharges = new BigDecimal("75.00");
        checkOutRequestDTO.pendingAmount = new BigDecimal("20.00");
        checkOutRequestDTO.status = CheckOutStatus.COMPLETED;
        checkOutRequestDTO.rating = rating();
        checkOutRequestDTO.paymentMaterialization = materialization();
        return checkOutRequestDTO;
    }

    private FinancialTransactionPlanMaterializationDTO materialization() {
        FinancialTransactionPlanMaterializationDTO financialTransactionPlanMaterializationDTO =
                new FinancialTransactionPlanMaterializationDTO();
        financialTransactionPlanMaterializationDTO.structure = FinancialPaymentStructure.SIMPLE;
        financialTransactionPlanMaterializationDTO.method = FinancialTransactionMethod.PIX;
        financialTransactionPlanMaterializationDTO.idempotencyKey = "stay-payment";
        return financialTransactionPlanMaterializationDTO;
    }

    private CheckOutRatingRequestDTO rating() {
        CheckOutRatingRequestDTO checkOutRatingRequestDTO = new CheckOutRatingRequestDTO();
        checkOutRatingRequestDTO.checkInProcedureScore = 5;
        checkOutRatingRequestDTO.checkOutProcedureScore = 5;
        checkOutRatingRequestDTO.accommodationCleanlinessScore = 5;
        checkOutRatingRequestDTO.teamCommunicationScore = 5;
        checkOutRatingRequestDTO.locationScore = 5;
        checkOutRatingRequestDTO.comfortScore = 5;
        return checkOutRatingRequestDTO;
    }

    private Booking booking(BookingStatus bookingStatus) {
        Guest guest = new Guest("Maria Silva", null, "11999999999", null);
        guest.restorePersistenceState(7L, null, List.of(), null, null);
        Room room = new Room(
                "101",
                RoomType.DOUBLE,
                2,
                new BigDecimal("350.00"),
                RoomStatus.OCCUPIED
        );
        room.restorePersistenceState(1L, null, null);
        Booking booking = new Booking(
                guest,
                room,
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                bookingStatus,
                new BigDecimal("700.00")
        );
        booking.restorePersistenceState(
                23L,
                booking.getPaymentStatus(),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null
        );
        return booking;
    }
}
