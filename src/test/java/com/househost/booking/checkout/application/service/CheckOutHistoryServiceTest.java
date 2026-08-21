package com.househost.booking.checkout.application.service;

import com.househost.booking.booking.application.service.BookingService;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.checkout.application.dto.CheckOutRequestDTO;
import com.househost.booking.checkout.application.dto.CheckOutRatingRequestDTO;
import com.househost.booking.checkout.application.port.out.CheckOutAuditPort;
import com.househost.booking.checkout.application.port.out.CheckOutPersistencePort;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.booking.checkout.domain.model.CheckOutStatus;
import com.househost.guest.domain.model.Guest;
import com.househost.room.domain.model.Room;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckOutHistoryServiceTest {

    @Test
    void firstCompletedCheckoutPersistsHistoryEvidenceAfterParticipantEffects() {
        CheckOutTestContextRecord checkOutTestContextRecord = checkOutTestContext();
        ArgumentCaptor<CheckOut> checkOutCaptor = ArgumentCaptor.forClass(CheckOut.class);
        ArgumentCaptor<Map<String, Object>> metadataMapCaptor = ArgumentCaptor.forClass(Map.class);

        CheckOutRequestDTO checkOutRequestDTO = request(CheckOutStatus.COMPLETED);
        checkOutTestContextRecord.checkOutService.create(checkOutRequestDTO);

        verify(checkOutTestContextRecord.checkOutRepository, times(2))
                .save(checkOutCaptor.capture());
        verify(checkOutTestContextRecord.checkOutParticipantNotifier)
                .notifyCompletion(
                        any(CheckOut.class),
                        eq(checkOutRequestDTO.rating),
                        isNull()
                );
        assertTrue(checkOutCaptor.getAllValues().get(1).isGuestHistoryApplied());
        verify(checkOutTestContextRecord.checkOutAuditPort).record(
                eq("CHECK_OUT_CREATED"),
                eq(null),
                metadataMapCaptor.capture()
        );
        assertFalse(metadataMapCaptor.getValue().containsKey("rating"));
    }

    @Test
    void pendingCheckoutDoesNotPersistHistoryEvidence() {
        CheckOutTestContextRecord checkOutTestContextRecord = checkOutTestContext();
        ArgumentCaptor<CheckOut> checkOutCaptor = ArgumentCaptor.forClass(CheckOut.class);

        checkOutTestContextRecord.checkOutService.create(request(CheckOutStatus.PENDING));

        verify(checkOutTestContextRecord.checkOutRepository).save(checkOutCaptor.capture());
        assertFalse(checkOutCaptor.getValue().isGuestHistoryApplied());
    }

    @Test
    void completedCheckoutUpdateDoesNotPersistHistoryEvidenceAgain() {
        CheckOutTestContextRecord checkOutTestContextRecord = checkOutTestContext();
        CheckOut completedCheckOut = completedCheckOut(checkOutTestContextRecord.booking);
        completedCheckOut.markGuestHistoryApplied();
        when(checkOutTestContextRecord.checkOutRepository.findByIdForUpdate(12L))
                .thenReturn(Optional.of(completedCheckOut));

        checkOutTestContextRecord.checkOutService.update(
                12L,
                request(CheckOutStatus.COMPLETED)
        );

        verify(checkOutTestContextRecord.checkOutRepository).save(any(CheckOut.class));
        assertTrue(completedCheckOut.isGuestHistoryApplied());
    }

    @Test
    void participantFailureLeavesEvidenceUnappliedAndSkipsSuccessAudit() {
        CheckOutTestContextRecord checkOutTestContextRecord = checkOutTestContext();
        ArgumentCaptor<CheckOut> checkOutCaptor = ArgumentCaptor.forClass(CheckOut.class);
        doThrow(new IllegalStateException("guest persistence failure"))
                .when(checkOutTestContextRecord.checkOutParticipantNotifier)
                .notifyCompletion(
                        any(CheckOut.class),
                        any(CheckOutRatingRequestDTO.class),
                        isNull()
                );

        assertThrows(
                IllegalStateException.class,
                () -> checkOutTestContextRecord.checkOutService.create(
                        request(CheckOutStatus.COMPLETED)
                )
        );

        verify(checkOutTestContextRecord.checkOutRepository).save(checkOutCaptor.capture());
        assertFalse(checkOutCaptor.getValue().isGuestHistoryApplied());
        verify(checkOutTestContextRecord.checkOutAuditPort, never()).record(
                eq("CHECK_OUT_CREATED"),
                eq(null),
                anyMap()
        );
    }

    private CheckOutTestContextRecord checkOutTestContext() {
        CheckOutPersistencePort checkOutRepository = mock(CheckOutPersistencePort.class);
        BookingService bookingService = mock(BookingService.class);
        CheckOutParticipantNotifier checkOutParticipantNotifier =
                mock(CheckOutParticipantNotifier.class);
        when(checkOutParticipantNotifier.notifyCompletion(
                any(CheckOut.class),
                any(CheckOutRatingRequestDTO.class),
                isNull()
        )).thenReturn(Optional.empty());
        CheckOutAuditPort checkOutAuditPort = mock(CheckOutAuditPort.class);
        CheckOutValidationService checkOutValidationService = mock(CheckOutValidationService.class);
        Guest guest = mock(Guest.class);
        Room room = mock(Room.class);
        Booking booking = mock(Booking.class);
        when(booking.getId()).thenReturn(23L);
        when(booking.getGuest()).thenReturn(guest);
        when(booking.getRoom()).thenReturn(room);
        when(bookingService.findBooking(23L)).thenReturn(booking);
        when(checkOutRepository.save(any(CheckOut.class))).thenAnswer(
                invocation -> invocation.getArgument(0)
        );
        CheckOutService checkOutService = new CheckOutService(
                checkOutRepository,
                bookingService,
                checkOutParticipantNotifier,
                checkOutAuditPort,
                checkOutValidationService
        );
        return new CheckOutTestContextRecord(
                checkOutService,
                checkOutRepository,
                checkOutParticipantNotifier,
                checkOutAuditPort,
                booking
        );
    }

    private CheckOut completedCheckOut(Booking booking) {
        CheckOut checkOut = new CheckOut(
                booking,
                booking.getGuest(),
                booking.getRoom(),
                LocalDateTime.of(2026, 8, 12, 11, 30),
                true,
                true,
                true,
                true,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "reception",
                null,
                CheckOutStatus.COMPLETED
        );
        checkOut.restorePersistenceState(12L, false, null, null);
        return checkOut;
    }

    private CheckOutRequestDTO request(CheckOutStatus checkOutStatus) {
        CheckOutRequestDTO checkOutRequestDTO = new CheckOutRequestDTO();
        checkOutRequestDTO.bookingId = 23L;
        checkOutRequestDTO.actualCheckOutAt = LocalDateTime.of(2026, 8, 12, 11, 30);
        checkOutRequestDTO.roomInspected = true;
        checkOutRequestDTO.rating = completeRating();
        checkOutRequestDTO.status = checkOutStatus;
        return checkOutRequestDTO;
    }

    private CheckOutRatingRequestDTO completeRating() {
        CheckOutRatingRequestDTO checkOutRatingRequestDTO = new CheckOutRatingRequestDTO();
        checkOutRatingRequestDTO.checkInProcedureScore = 5;
        checkOutRatingRequestDTO.checkOutProcedureScore = 5;
        checkOutRatingRequestDTO.accommodationCleanlinessScore = 5;
        checkOutRatingRequestDTO.teamCommunicationScore = 5;
        checkOutRatingRequestDTO.locationScore = 5;
        checkOutRatingRequestDTO.comfortScore = 5;
        return checkOutRatingRequestDTO;
    }

    private record CheckOutTestContextRecord(
            CheckOutService checkOutService,
            CheckOutPersistencePort checkOutRepository,
            CheckOutParticipantNotifier checkOutParticipantNotifier,
            CheckOutAuditPort checkOutAuditPort,
            Booking booking
    ) {
    }
}
