package com.househost.booking.checking.adapter.out.persistence;

import com.househost.booking.checking.adapter.out.persistence.entity.CheckInJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckInJpaRepository extends JpaRepository<CheckInJpaEntity, Long> {

    Optional<CheckInJpaEntity> findByBooking_Id(Long bookingId);

    boolean existsByBooking_Id(Long bookingId);
}
