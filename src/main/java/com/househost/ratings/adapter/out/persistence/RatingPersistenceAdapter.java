package com.househost.ratings.adapter.out.persistence;

import com.househost.ratings.adapter.out.persistence.entity.RatingJpaEntity;
import com.househost.ratings.adapter.out.persistence.entity.RatingPersistenceMapper;
import com.househost.ratings.application.port.out.RatingPersistencePort;
import com.househost.ratings.application.records.RatingPageRecord;
import com.househost.ratings.application.records.RatingSummaryRecord;
import com.househost.ratings.domain.model.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class RatingPersistenceAdapter implements RatingPersistencePort {

    private final RatingJpaRepository ratingJpaRepository;

    public RatingPersistenceAdapter(RatingJpaRepository ratingJpaRepository) {
        this.ratingJpaRepository = ratingJpaRepository;
    }

    @Override
    public Rating save(Rating rating) {
        rating.prepareForSave(LocalDateTime.now());
        return RatingPersistenceMapper.toDomain(
                ratingJpaRepository.save(RatingPersistenceMapper.toEntity(rating))
        );
    }

    @Override
    public Optional<Rating> findById(Long id) {
        return ratingJpaRepository.findById(id).map(RatingPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Rating> findByBookingId(Long bookingId) {
        return ratingJpaRepository.findByBookingId(bookingId)
                .map(RatingPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByBookingId(Long bookingId) {
        return ratingJpaRepository.existsByBookingId(bookingId);
    }

    @Override
    public RatingPageRecord findAll(int page, int size) {
        Page<RatingSummaryRecord> ratingSummaryRecordPage = ratingJpaRepository
                .findSummaryPage(PageRequest.of(page, size));
        return toPageRecord(ratingSummaryRecordPage);
    }

    @Override
    public RatingPageRecord findByGuestId(Long guestId, int page, int size) {
        Page<RatingSummaryRecord> ratingSummaryRecordPage = ratingJpaRepository
                .findSummaryPageByGuestId(
                        guestId,
                        PageRequest.of(page, size)
                );
        return toPageRecord(ratingSummaryRecordPage);
    }

    private RatingPageRecord toPageRecord(
            Page<RatingSummaryRecord> ratingSummaryRecordPage
    ) {
        return new RatingPageRecord(
                ratingSummaryRecordPage.getContent(),
                ratingSummaryRecordPage.getNumber(),
                ratingSummaryRecordPage.getSize(),
                ratingSummaryRecordPage.getTotalElements(),
                ratingSummaryRecordPage.getTotalPages()
        );
    }
}
