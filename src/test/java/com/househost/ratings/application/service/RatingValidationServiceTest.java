package com.househost.ratings.application.service;

import com.househost.booking.booking.application.service.BookingService;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.checkout.application.service.CheckOutService;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.booking.checkout.domain.model.CheckOutStatus;
import com.househost.ratings.application.dto.RatingRequestDTO;
import com.househost.ratings.application.port.out.RatingPersistencePort;
import com.househost.ratings.application.records.RatingCreationContextRecord;
import com.househost.ratings.domain.exception.RatingConflictException;
import com.househost.ratings.domain.exception.RatingEligibilityException;
import com.househost.ratings.domain.exception.RatingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RatingValidationServiceTest {

    private BookingService bookingService;
    private CheckOutService checkOutService;
    private RatingPersistencePort ratingPersistencePort;
    private RatingValidationService ratingValidationService;

    @BeforeEach
    void setUp() {
        bookingService = mock(BookingService.class);
        checkOutService = mock(CheckOutService.class);
        ratingPersistencePort = mock(RatingPersistencePort.class);
        ratingValidationService = new RatingValidationService(
                bookingService,
                checkOutService,
                ratingPersistencePort
        );
    }

    @Test
    void resolvesCompletedCheckoutAndNormalizesObservations() {
        RatingRequestDTO request = validRequest();
        Booking booking = mock(Booking.class);
        CheckOut checkOut = mock(CheckOut.class);
        LocalDateTime actualCheckOutAt = LocalDateTime.of(2026, 8, 12, 11, 30);
        when(booking.getId()).thenReturn(42L);
        when(bookingService.findBooking(42L)).thenReturn(booking);
        when(checkOutService.findCheckOutByBookingId(42L)).thenReturn(checkOut);
        when(checkOut.getStatus()).thenReturn(CheckOutStatus.COMPLETED);
        when(checkOut.getActualCheckOutAt()).thenReturn(actualCheckOutAt);

        RatingCreationContextRecord ratingCreationContextRecord =
                ratingValidationService.validateCreation(request);

        assertEquals(booking, ratingCreationContextRecord.booking());
        assertEquals(actualCheckOutAt, ratingCreationContextRecord.evaluatedAt());
        assertEquals("Muito bom", ratingCreationContextRecord.normalizedObservations());
    }

    @Test
    void rejectsDuplicateRatingWithStableConflict() {
        RatingRequestDTO request = validRequest();
        Booking booking = mock(Booking.class);
        when(booking.getId()).thenReturn(42L);
        when(bookingService.findBooking(42L)).thenReturn(booking);
        when(ratingPersistencePort.existsByBookingId(42L)).thenReturn(true);

        assertThrows(
                RatingConflictException.class,
                () -> ratingValidationService.validateCreation(request)
        );
    }

    @Test
    void rejectsBookingWithoutCompletedCheckout() {
        RatingRequestDTO request = validRequest();
        Booking booking = mock(Booking.class);
        CheckOut checkOut = mock(CheckOut.class);
        when(booking.getId()).thenReturn(42L);
        when(bookingService.findBooking(42L)).thenReturn(booking);
        when(checkOutService.findCheckOutByBookingId(42L)).thenReturn(checkOut);
        when(checkOut.getStatus()).thenReturn(CheckOutStatus.PENDING);

        assertThrows(
                RatingEligibilityException.class,
                () -> ratingValidationService.validateCreation(request)
        );
    }

    @Test
    void rejectsEveryInvalidRequestBoundary() {
        RatingRequestDTO missingScoreRequest = validRequest();
        missingScoreRequest.comfortScore = null;
        RatingRequestDTO oversizedObservationRequest = validRequest();
        oversizedObservationRequest.observations = "a".repeat(4_001);

        assertThrows(
                RatingException.class,
                () -> ratingValidationService.validateCreation(null)
        );
        assertThrows(
                RatingException.class,
                () -> ratingValidationService.validateCreation(missingScoreRequest)
        );
        assertThrows(
                RatingException.class,
                () -> ratingValidationService.validateCreation(oversizedObservationRequest)
        );
        assertThrows(RatingException.class, () -> ratingValidationService.validatePagination(-1, 20));
        assertThrows(RatingException.class, () -> ratingValidationService.validatePagination(0, 0));
        assertThrows(RatingException.class, () -> ratingValidationService.validatePagination(0, 101));
        assertThrows(RatingException.class, () -> ratingValidationService.validateGuestId(0L));
        assertThrows(RatingException.class, () -> ratingValidationService.validateBookingId(null));
    }

    private RatingRequestDTO validRequest() {
        RatingRequestDTO request = new RatingRequestDTO();
        request.bookingId = 42L;
        request.checkInProcedureScore = 5;
        request.checkOutProcedureScore = 4;
        request.accommodationCleanlinessScore = 5;
        request.teamCommunicationScore = 4;
        request.locationScore = 5;
        request.comfortScore = 4;
        request.observations = "  Muito bom  ";
        return request;
    }
}
