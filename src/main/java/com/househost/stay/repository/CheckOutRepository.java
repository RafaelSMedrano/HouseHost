package com.househost.stay.repository;

import com.househost.stay.model.CheckOut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckOutRepository extends JpaRepository<CheckOut, Long> {

    Optional<CheckOut> findByStayId(Long stayId);

    boolean existsByStayId(Long stayId);
}
