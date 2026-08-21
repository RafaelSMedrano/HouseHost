package com.househost.ratings.adapter.out.persistence;

import com.househost.ratings.adapter.out.persistence.entity.RatingJpaEntity;
import com.househost.ratings.application.records.RatingSummaryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RatingJpaRepository extends JpaRepository<RatingJpaEntity, Long> {

    Optional<RatingJpaEntity> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);

    @Query(
            value = """
                    select new com.househost.ratings.application.records.RatingSummaryRecord(
                        booking.id,
                        guest.id,
                        guest.fullName,
                        booking.checkInDate,
                        booking.checkOutDate,
                        rating.evaluatedAt,
                        rating.checkInProcedureScore,
                        rating.checkOutProcedureScore,
                        rating.accommodationCleanlinessScore,
                        rating.teamCommunicationScore,
                        rating.locationScore,
                        rating.comfortScore,
                        rating.observations
                    )
                    from Rating rating
                    join rating.booking booking
                    join booking.guest guest
                    order by rating.evaluatedAt desc, rating.id desc
                    """,
            countQuery = "select count(rating.id) from Rating rating"
    )
    Page<RatingSummaryRecord> findSummaryPage(Pageable pageable);

    @Query(
            value = """
                    select new com.househost.ratings.application.records.RatingSummaryRecord(
                        booking.id,
                        guest.id,
                        guest.fullName,
                        booking.checkInDate,
                        booking.checkOutDate,
                        rating.evaluatedAt,
                        rating.checkInProcedureScore,
                        rating.checkOutProcedureScore,
                        rating.accommodationCleanlinessScore,
                        rating.teamCommunicationScore,
                        rating.locationScore,
                        rating.comfortScore,
                        rating.observations
                    )
                    from Rating rating
                    join rating.booking booking
                    join booking.guest guest
                    where guest.id = :guestId
                    order by rating.evaluatedAt desc, rating.id desc
                    """,
            countQuery = """
                    select count(rating.id)
                    from Rating rating
                    join rating.booking booking
                    join booking.guest guest
                    where guest.id = :guestId
                    """
    )
    Page<RatingSummaryRecord> findSummaryPageByGuestId(
            Long guestId,
            Pageable pageable
    );
}
