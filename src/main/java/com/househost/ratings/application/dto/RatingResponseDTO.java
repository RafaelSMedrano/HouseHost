package com.househost.ratings.application.dto;

import com.househost.ratings.domain.model.Rating;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RatingResponseDTO {

    private final Long id;
    private final Long bookingId;
    private final Long guestId;
    private final String guestName;
    private final LocalDate bookingCheckInDate;
    private final LocalDate bookingCheckOutDate;
    private final Integer checkInProcedureScore;
    private final Integer checkOutProcedureScore;
    private final Integer accommodationCleanlinessScore;
    private final Integer teamCommunicationScore;
    private final Integer locationScore;
    private final Integer comfortScore;
    private final String observations;
    private final LocalDateTime evaluatedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public RatingResponseDTO(Rating rating) {
        this.id = rating.getId();
        this.bookingId = rating.getBooking().getId();
        this.guestId = rating.getBooking().getGuest().getId();
        this.guestName = rating.getBooking().getGuest().getFullName();
        this.bookingCheckInDate = rating.getBooking().getCheckInDate();
        this.bookingCheckOutDate = rating.getBooking().getCheckOutDate();
        this.checkInProcedureScore = rating.getCheckInProcedureScore();
        this.checkOutProcedureScore = rating.getCheckOutProcedureScore();
        this.accommodationCleanlinessScore = rating.getAccommodationCleanlinessScore();
        this.teamCommunicationScore = rating.getTeamCommunicationScore();
        this.locationScore = rating.getLocationScore();
        this.comfortScore = rating.getComfortScore();
        this.observations = rating.getObservations();
        this.evaluatedAt = rating.getEvaluatedAt();
        this.createdAt = rating.getCreatedAt();
        this.updatedAt = rating.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getGuestId() {
        return guestId;
    }

    public String getGuestName() {
        return guestName;
    }

    public LocalDate getBookingCheckInDate() {
        return bookingCheckInDate;
    }

    public LocalDate getBookingCheckOutDate() {
        return bookingCheckOutDate;
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
}
