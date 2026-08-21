package com.househost.ratings.application.dto;

import com.househost.ratings.application.records.RatingSummaryRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RatingSummaryDTO {

    private final Long bookingId;
    private final Long guestId;
    private final String guestName;
    private final LocalDate bookingCheckInDate;
    private final LocalDate bookingCheckOutDate;
    private final LocalDateTime evaluatedAt;
    private final Integer checkInProcedureScore;
    private final Integer checkOutProcedureScore;
    private final Integer accommodationCleanlinessScore;
    private final Integer teamCommunicationScore;
    private final Integer locationScore;
    private final Integer comfortScore;
    private final String observations;

    public RatingSummaryDTO(RatingSummaryRecord ratingSummaryRecord) {
        this.bookingId = ratingSummaryRecord.bookingId();
        this.guestId = ratingSummaryRecord.guestId();
        this.guestName = ratingSummaryRecord.guestName();
        this.bookingCheckInDate = ratingSummaryRecord.bookingCheckInDate();
        this.bookingCheckOutDate = ratingSummaryRecord.bookingCheckOutDate();
        this.evaluatedAt = ratingSummaryRecord.evaluatedAt();
        this.checkInProcedureScore = ratingSummaryRecord.checkInProcedureScore();
        this.checkOutProcedureScore = ratingSummaryRecord.checkOutProcedureScore();
        this.accommodationCleanlinessScore =
                ratingSummaryRecord.accommodationCleanlinessScore();
        this.teamCommunicationScore = ratingSummaryRecord.teamCommunicationScore();
        this.locationScore = ratingSummaryRecord.locationScore();
        this.comfortScore = ratingSummaryRecord.comfortScore();
        this.observations = ratingSummaryRecord.observations();
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

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
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
}
