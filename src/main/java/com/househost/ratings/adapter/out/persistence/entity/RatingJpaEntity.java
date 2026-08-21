package com.househost.ratings.adapter.out.persistence.entity;

import com.househost.booking.booking.adapter.out.persistence.entity.BookingJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;

@Entity(name = "Rating")
@Table(
        name = "ratings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ratings_booking",
                columnNames = "booking_id"
        )
)
@Check(constraints = "check_in_procedure_score between 1 and 5 "
        + "and check_out_procedure_score between 1 and 5 "
        + "and accommodation_cleanliness_score between 1 and 5 "
        + "and team_communication_score between 1 and 5 "
        + "and location_score between 1 and 5 "
        + "and comfort_score between 1 and 5")
public class RatingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    BookingJpaEntity booking;

    @Column(name = "check_in_procedure_score", nullable = false)
    Integer checkInProcedureScore;

    @Column(name = "check_out_procedure_score", nullable = false)
    Integer checkOutProcedureScore;

    @Column(name = "accommodation_cleanliness_score", nullable = false)
    Integer accommodationCleanlinessScore;

    @Column(name = "team_communication_score", nullable = false)
    Integer teamCommunicationScore;

    @Column(name = "location_score", nullable = false)
    Integer locationScore;

    @Column(name = "comfort_score", nullable = false)
    Integer comfortScore;

    @Column(name = "observations", columnDefinition = "TEXT")
    String observations;

    @Column(name = "evaluated_at", nullable = false)
    LocalDateTime evaluatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    protected RatingJpaEntity() {
    }

    @PrePersist
    void prePersist() {
        LocalDateTime persistenceTime = LocalDateTime.now();
        createdAt = createdAt == null ? persistenceTime : createdAt;
        updatedAt = updatedAt == null ? persistenceTime : updatedAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
