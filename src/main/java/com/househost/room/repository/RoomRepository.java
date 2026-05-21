package com.househost.room.repository;

import com.househost.room.model.Room;
import com.househost.room.model.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    long countByStatus(RoomStatus status);

    Optional<Room> findByRoomNumberIgnoreCase(String roomNumber);

    boolean existsByRoomNumber(String roomNumber);

    boolean existsByRoomNumberAndIdNot(String roomNumber, Long id);
}
