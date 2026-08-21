package com.househost.booking.checkout.adapter.out.persistence;

import com.househost.booking.checkout.adapter.out.persistence.entity.CheckOutJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface CheckOutJpaRepository extends JpaRepository<CheckOutJpaEntity, Long> {

    Optional<CheckOutJpaEntity> findByBooking_Id(Long bookingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select checkOut from CheckOut checkOut where checkOut.id = :id")
    Optional<CheckOutJpaEntity> findByIdForUpdate(@Param("id") Long id);
}
