package com.househost.booking.checkout.adapter.out.persistence;

import com.househost.booking.checkout.adapter.out.persistence.entity.CheckOutJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckOutJpaRepository extends JpaRepository<CheckOutJpaEntity, Long> {
    Optional<CheckOutJpaEntity> findByBooking_Id(Long bookingId);
}
