package com.househost.ratings.domain.model;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.ratings.domain.exception.RatingException;

import java.time.LocalDateTime;

public class Rating {

    public static final int MAX_OBSERVATIONS_LENGTH = 4_000;

    private Long id;
    private final Booking booking;
    private final Integer checkInProcedureScore;
    private final Integer checkOutProcedureScore;
    private final Integer accommodationCleanlinessScore;
    private final Integer teamCommunicationScore;
    private final Integer locationScore;
    private final Integer comfortScore;
    private final String observations;
    private final LocalDateTime evaluatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Rating(
            Booking booking,
            Integer checkInProcedureScore,
            Integer checkOutProcedureScore,
            Integer accommodationCleanlinessScore,
            Integer teamCommunicationScore,
            Integer locationScore,
            Integer comfortScore,
            String observations,
            LocalDateTime evaluatedAt
    ) {
        if (booking == null) {
            throw new RatingException("A reserva avaliada e obrigatoria.");
        }
        if (evaluatedAt == null) {
            throw new RatingException("A data da avaliacao e obrigatoria.");
        }

        this.booking = booking;
        this.checkInProcedureScore = validateScore(
                checkInProcedureScore,
                "procedimento de check-in"
        );
        this.checkOutProcedureScore = validateScore(
                checkOutProcedureScore,
                "procedimento de checkout"
        );
        this.accommodationCleanlinessScore = validateScore(
                accommodationCleanlinessScore,
                "limpeza das acomodacoes"
        );
        this.teamCommunicationScore = validateScore(
                teamCommunicationScore,
                "comunicacao da equipe"
        );
        this.locationScore = validateScore(locationScore, "localizacao");
        this.comfortScore = validateScore(comfortScore, "conforto");
        this.observations = normalizeObservations(observations);
        this.evaluatedAt = evaluatedAt;
    }

    public void prepareForSave(LocalDateTime persistenceTime) {
        if (persistenceTime == null) {
            throw new RatingException("A data de persistencia da avaliacao e obrigatoria.");
        }
        if (createdAt == null) {
            createdAt = persistenceTime;
        }
        updatedAt = persistenceTime;
    }

    public void restorePersistenceState(
            Long id,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public Integer getCheckInProcedureScore() {
        return checkInProcedureScore;
    }

    public Integer getCheckOutProcedureScore() {
        return checkOutProcedureScore;
    }

    public Integer getAccommodationCleanlinessScore() {
        return accommodationCleanlinessScore;
    }

    public Integer getTeamCommunicationScore() {
        return teamCommunicationScore;
    }

    public Integer getLocationScore() {
        return locationScore;
    }

    public Integer getComfortScore() {
        return comfortScore;
    }

    public String getObservations() {
        return observations;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private Integer validateScore(Integer score, String criterionName) {
        if (score == null || score < 1 || score > 5) {
            throw new RatingException(
                    "A nota de " + criterionName + " deve ser um inteiro entre 1 e 5."
            );
        }
        return score;
    }

    private String normalizeObservations(String observations) {
        if (observations == null || observations.isBlank()) {
            return null;
        }

        String normalizedObservations = observations.trim();
        if (normalizedObservations.length() > MAX_OBSERVATIONS_LENGTH) {
            throw new RatingException(
                    "As observacoes da avaliacao devem ter no maximo 4000 caracteres."
            );
        }
        return normalizedObservations;
    }
}
