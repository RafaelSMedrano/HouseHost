package com.househost.ratings.adapter.out.persistence.entity;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingPaymentStatus;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.guest.domain.model.Guest;
import com.househost.ratings.domain.model.Rating;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import com.househost.room.domain.model.RoomType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RatingPersistenceMapperTest {

    @Test
    void mapsEveryPersistedPropertyInBothDirections() {
        LocalDateTime evaluatedAt = LocalDateTime.of(2026, 8, 12, 10, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 12, 10, 1);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 8, 12, 10, 2);
        Booking booking = bookingReference(91L);
        Rating rating = new Rating(booking, 1, 2, 3, 4, 5, 4, "Observacao", evaluatedAt);
        rating.restorePersistenceState(31L, createdAt, updatedAt);

        RatingJpaEntity ratingJpaEntity = RatingPersistenceMapper.toEntity(rating);
        Rating restoredRating = RatingPersistenceMapper.toDomain(ratingJpaEntity);

        assertEquals(31L, restoredRating.getId());
        assertEquals(91L, restoredRating.getBooking().getId());
        assertEquals(1, restoredRating.getCheckInProcedureScore());
        assertEquals(2, restoredRating.getCheckOutProcedureScore());
        assertEquals(3, restoredRating.getAccommodationCleanlinessScore());
        assertEquals(4, restoredRating.getTeamCommunicationScore());
        assertEquals(5, restoredRating.getLocationScore());
        assertEquals(4, restoredRating.getComfortScore());
        assertEquals("Observacao", restoredRating.getObservations());
        assertEquals(evaluatedAt, restoredRating.getEvaluatedAt());
        assertEquals(createdAt, restoredRating.getCreatedAt());
        assertEquals(updatedAt, restoredRating.getUpdatedAt());
    }

    private Booking bookingReference(Long bookingId) {
        Guest guest = new Guest("Hospede", null, "11999999999", "12345678900");
        Room room = new Room(
                "1",
                RoomType.STANDARD,
                2,
                BigDecimal.valueOf(300),
                RoomStatus.AVAILABLE
        );
        Booking booking = new Booking(
                guest,
                room,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 12),
                BookingStatus.FINISHED,
                BigDecimal.valueOf(600)
        );
        booking.restorePersistenceState(
                bookingId,
                BookingPaymentStatus.WAITING,
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
