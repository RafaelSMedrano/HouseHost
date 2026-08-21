package com.househost.ratings.adapter.out.persistence.entity;

import com.househost.booking.booking.adapter.out.persistence.entity.BookingPersistenceMapper;
import com.househost.ratings.domain.model.Rating;

public final class RatingPersistenceMapper {

    private RatingPersistenceMapper() {
    }

    public static Rating toDomain(RatingJpaEntity ratingJpaEntity) {
        if (ratingJpaEntity == null) {
            return null;
        }

        Rating rating = new Rating(
                BookingPersistenceMapper.toDomain(ratingJpaEntity.booking),
                ratingJpaEntity.checkInProcedureScore,
                ratingJpaEntity.checkOutProcedureScore,
                ratingJpaEntity.accommodationCleanlinessScore,
                ratingJpaEntity.teamCommunicationScore,
                ratingJpaEntity.locationScore,
                ratingJpaEntity.comfortScore,
                ratingJpaEntity.observations,
                ratingJpaEntity.evaluatedAt
        );
        rating.restorePersistenceState(
                ratingJpaEntity.id,
                ratingJpaEntity.createdAt,
                ratingJpaEntity.updatedAt
        );
        return rating;
    }

    public static RatingJpaEntity toEntity(Rating rating) {
        if (rating == null) {
            return null;
        }

        RatingJpaEntity ratingJpaEntity = new RatingJpaEntity();
        ratingJpaEntity.id = rating.getId();
        ratingJpaEntity.booking = BookingPersistenceMapper.toEntity(rating.getBooking());
        ratingJpaEntity.checkInProcedureScore = rating.getCheckInProcedureScore();
        ratingJpaEntity.checkOutProcedureScore = rating.getCheckOutProcedureScore();
        ratingJpaEntity.accommodationCleanlinessScore =
                rating.getAccommodationCleanlinessScore();
        ratingJpaEntity.teamCommunicationScore = rating.getTeamCommunicationScore();
        ratingJpaEntity.locationScore = rating.getLocationScore();
        ratingJpaEntity.comfortScore = rating.getComfortScore();
        ratingJpaEntity.observations = rating.getObservations();
        ratingJpaEntity.evaluatedAt = rating.getEvaluatedAt();
        ratingJpaEntity.createdAt = rating.getCreatedAt();
        ratingJpaEntity.updatedAt = rating.getUpdatedAt();
        return ratingJpaEntity;
    }
}
