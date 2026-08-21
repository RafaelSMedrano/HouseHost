package com.househost.ratings.application.service;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.guest.domain.model.Guest;
import com.househost.ratings.application.dto.RatingPageResponseDTO;
import com.househost.ratings.application.dto.RatingRequestDTO;
import com.househost.ratings.application.dto.RatingResponseDTO;
import com.househost.ratings.application.port.out.RatingAuditPort;
import com.househost.ratings.application.port.out.RatingPersistencePort;
import com.househost.ratings.application.records.RatingCreationContextRecord;
import com.househost.ratings.application.records.RatingPageRecord;
import com.househost.ratings.application.records.RatingSummaryRecord;
import com.househost.ratings.domain.exception.RatingConflictException;
import com.househost.ratings.domain.model.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RatingServiceTest {

    private RatingPersistencePort ratingPersistencePort;
    private RatingValidationService ratingValidationService;
    private RatingAuditPort ratingAuditPort;
    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingPersistencePort = mock(RatingPersistencePort.class);
        ratingValidationService = mock(RatingValidationService.class);
        ratingAuditPort = mock(RatingAuditPort.class);
        ratingService = new RatingService(
                ratingPersistencePort,
                ratingValidationService,
                ratingAuditPort
        );
    }

    @Test
    void createsRatingFromValidatedCheckoutContext() {
        RatingRequestDTO request = validRequest();
        Booking booking = bookingContext();
        LocalDateTime evaluatedAt = LocalDateTime.of(2026, 8, 12, 11, 30);
        when(ratingValidationService.validateCreation(request)).thenReturn(
                new RatingCreationContextRecord(booking, evaluatedAt, "Otima estadia")
        );
        when(ratingPersistencePort.save(any(Rating.class))).thenAnswer(invocation -> {
            Rating rating = invocation.getArgument(0);
            rating.restorePersistenceState(9L, evaluatedAt, evaluatedAt);
            return rating;
        });

        RatingResponseDTO ratingResponseDTO = ratingService.createForCompletedBooking(request);

        assertEquals(9L, ratingResponseDTO.getId());
        assertEquals(42L, ratingResponseDTO.getBookingId());
        assertEquals(7L, ratingResponseDTO.getGuestId());
        assertEquals("Hospede", ratingResponseDTO.getGuestName());
        assertEquals("Otima estadia", ratingResponseDTO.getObservations());
        assertEquals(evaluatedAt, ratingResponseDTO.getEvaluatedAt());
        verify(ratingAuditPort).record(
                "RATING_CREATED",
                9L,
                Map.of("bookingId", 42L, "outcome", "CREATED")
        );
    }

    @Test
    void translatesConcurrentDatabaseDuplicateIntoStableConflict() {
        RatingRequestDTO request = validRequest();
        Booking booking = bookingContext();
        when(ratingValidationService.validateCreation(request)).thenReturn(
                new RatingCreationContextRecord(booking, LocalDateTime.now(), null)
        );
        when(ratingPersistencePort.save(any(Rating.class))).thenThrow(
                new DataIntegrityViolationException("duplicate")
        );

        assertThrows(
                RatingConflictException.class,
                () -> ratingService.createForCompletedBooking(request)
        );
        verify(ratingAuditPort, never()).record(
                any(),
                any(),
                any()
        );
    }

    @Test
    void delegatesBoundedPaginationAndMapsSummaries() {
        RatingSummaryRecord ratingSummaryRecord = ratingSummaryRecord();
        when(ratingPersistencePort.findAll(1, 10)).thenReturn(
                new RatingPageRecord(List.of(ratingSummaryRecord), 1, 10, 11, 2)
        );
        when(ratingPersistencePort.findByGuestId(7L, 0, 20)).thenReturn(
                new RatingPageRecord(List.of(ratingSummaryRecord), 0, 20, 1, 1)
        );

        RatingPageResponseDTO allRatingPageResponseDTO = ratingService.findAll(1, 10);
        RatingPageResponseDTO guestRatingPageResponseDTO =
                ratingService.findByGuestId(7L, 0, 20);

        assertEquals(1, allRatingPageResponseDTO.getRatingSummaryDTOList().size());
        assertEquals(11, allRatingPageResponseDTO.getTotalElements());
        assertEquals(42L, guestRatingPageResponseDTO
                .getRatingSummaryDTOList().getFirst().getBookingId());
        verify(ratingValidationService).validatePagination(1, 10);
        verify(ratingValidationService).validateGuestId(7L);
        verify(ratingValidationService).validatePagination(0, 20);
        verify(ratingAuditPort).record(
                "RATING_LIST_VIEWED",
                null,
                Map.of(
                        "page", 1,
                        "size", 10,
                        "resultCount", 1,
                        "outcome", "READ"
                )
        );
        verify(ratingAuditPort).record(
                "GUEST_RATING_HISTORY_VIEWED",
                null,
                Map.of(
                        "guestId", 7L,
                        "page", 0,
                        "size", 20,
                        "resultCount", 1,
                        "outcome", "READ"
                )
        );
    }

    @Test
    void exposesBookingExistenceWithoutDetailOperation() {
        when(ratingPersistencePort.existsByBookingId(42L)).thenReturn(true);

        assertTrue(ratingService.existsByBookingId(42L));
        verify(ratingValidationService).validateBookingId(42L);
    }

    @Test
    void summaryContractContainsOnlyRequiredDisplayAndRatingFields() {
        Set<String> summaryFieldNameSet = Arrays.stream(
                        com.househost.ratings.application.dto.RatingSummaryDTO.class
                                .getDeclaredFields()
                )
                .map(java.lang.reflect.Field::getName)
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "bookingId",
                "guestId",
                "guestName",
                "bookingCheckInDate",
                "bookingCheckOutDate",
                "evaluatedAt",
                "checkInProcedureScore",
                "checkOutProcedureScore",
                "accommodationCleanlinessScore",
                "teamCommunicationScore",
                "locationScore",
                "comfortScore",
                "observations"
        ), summaryFieldNameSet);
    }

    private RatingSummaryRecord ratingSummaryRecord() {
        return new RatingSummaryRecord(
                42L,
                7L,
                "Hospede",
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 12),
                LocalDateTime.of(2026, 8, 12, 11, 30),
                5,
                4,
                5,
                4,
                5,
                4,
                "Otima estadia"
        );
    }

    private Booking bookingContext() {
        Guest guest = mock(Guest.class);
        Booking booking = mock(Booking.class);
        when(guest.getId()).thenReturn(7L);
        when(guest.getFullName()).thenReturn("Hospede");
        when(booking.getId()).thenReturn(42L);
        when(booking.getGuest()).thenReturn(guest);
        when(booking.getCheckInDate()).thenReturn(LocalDate.of(2026, 8, 10));
        when(booking.getCheckOutDate()).thenReturn(LocalDate.of(2026, 8, 12));
        return booking;
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
        request.observations = "Otima estadia";
        return request;
    }
}
