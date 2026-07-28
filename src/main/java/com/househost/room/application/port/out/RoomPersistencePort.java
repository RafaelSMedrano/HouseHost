package com.househost.room.application.port.out;

import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;

import java.util.List;
import java.util.Optional;

public interface RoomPersistencePort {

    Room save(Room room);

    List<Room> findAll();

    Optional<Room> findById(Long id);

    Optional<Room> findByRoomNumberIgnoreCase(String roomNumber);

    void delete(Room room);

    long countByStatus(RoomStatus status);

    boolean existsByRoomNumber(String roomNumber);

    boolean existsByRoomNumberAndIdNot(String roomNumber, Long id);
}
