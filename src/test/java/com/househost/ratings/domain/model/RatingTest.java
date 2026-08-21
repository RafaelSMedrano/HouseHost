package com.househost.ratings.domain.model;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.ratings.domain.exception.RatingException;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RatingTest {

    private static final LocalDateTime EVALUATED_AT = LocalDateTime.of(2026, 8, 12, 11, 30);

    @Test
    void createsCompleteRatingAndNormalizesObservations() {
        Rating rating = createRating(1, 2, 3, 4, 5, 4, "  Atendimento excelente.  ");

        assertEquals(1, rating.getCheckInProcedureScore());
        assertEquals(2, rating.getCheckOutProcedureScore());
        assertEquals(3, rating.getAccommodationCleanlinessScore());
        assertEquals(4, rating.getTeamCommunicationScore());
        assertEquals(5, rating.getLocationScore());
        assertEquals(4, rating.getComfortScore());
        assertEquals("Atendimento excelente.", rating.getObservations());
        assertEquals(EVALUATED_AT, rating.getEvaluatedAt());
    }

    @ParameterizedTest
    @MethodSource("invalidScoreArguments")
    void rejectsMissingOrOutOfRangeScore(int scoreIndex, Integer invalidScore) {
        Integer[] scoreArray = {3, 3, 3, 3, 3, 3};
        scoreArray[scoreIndex] = invalidScore;

        assertThrows(
                RatingException.class,
                () -> createRating(
                        scoreArray[0],
                        scoreArray[1],
                        scoreArray[2],
                        scoreArray[3],
                        scoreArray[4],
                        scoreArray[5],
                        null
                )
        );
    }

    @Test
    void rejectsOversizedObservationsWithoutTruncation() {
        String oversizedObservations = "a".repeat(Rating.MAX_OBSERVATIONS_LENGTH + 1);

        assertThrows(
                RatingException.class,
                () -> createRating(5, 5, 5, 5, 5, 5, oversizedObservations)
        );
    }

    @Test
    void convertsBlankObservationsToNull() {
        Rating rating = createRating(5, 5, 5, 5, 5, 5, "   ");

        assertNull(rating.getObservations());
    }

    @Test
    void keepsDomainFreeFromJpaEntityAnnotation() {
        assertFalse(Rating.class.isAnnotationPresent(Entity.class));
    }

    private static Stream<Arguments> invalidScoreArguments() {
        return Stream.of(
                Arguments.of(0, null),
                Arguments.of(0, 0),
                Arguments.of(0, 6),
                Arguments.of(1, null),
                Arguments.of(1, 0),
                Arguments.of(1, 6),
                Arguments.of(2, null),
                Arguments.of(2, 0),
                Arguments.of(2, 6),
                Arguments.of(3, null),
                Arguments.of(3, 0),
                Arguments.of(3, 6),
                Arguments.of(4, null),
                Arguments.of(4, 0),
                Arguments.of(4, 6),
                Arguments.of(5, null),
                Arguments.of(5, 0),
                Arguments.of(5, 6)
        );
    }

    private Rating createRating(
            Integer checkInProcedureScore,
            Integer checkOutProcedureScore,
            Integer accommodationCleanlinessScore,
            Integer teamCommunicationScore,
            Integer locationScore,
            Integer comfortScore,
            String observations
    ) {
        return new Rating(
                new Booking(),
                checkInProcedureScore,
                checkOutProcedureScore,
                accommodationCleanlinessScore,
                teamCommunicationScore,
                locationScore,
                comfortScore,
                observations,
                EVALUATED_AT
        );
    }
}
