package com.househost.room.adapter.out.persistence;

import com.househost.room.adapter.out.persistence.entity.RoomJpaEntity;
import com.househost.room.domain.model.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomJpaRepository extends JpaRepository<RoomJpaEntity, Long> {

    long countByStatus(RoomStatus status);

    Optional<RoomJpaEntity> findByRoomNumberIgnoreCase(String roomNumber);

    boolean existsByRoomNumber(String roomNumber);

    boolean existsByRoomNumberAndIdNot(String roomNumber, Long id);
}
