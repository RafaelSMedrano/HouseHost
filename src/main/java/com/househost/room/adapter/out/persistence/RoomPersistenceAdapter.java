package com.househost.room.adapter.out.persistence;

import com.househost.room.adapter.out.persistence.entity.RoomPersistenceMapper;
import com.househost.room.application.port.out.RoomPersistencePort;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RoomPersistenceAdapter implements RoomPersistencePort {

    private final RoomJpaRepository repository;

    public RoomPersistenceAdapter(RoomJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Room save(Room room) {
        return RoomPersistenceMapper.toDomain(repository.save(RoomPersistenceMapper.toEntity(room)));
    }

    @Override
    public List<Room> findAll() {
        return repository.findAll().stream().map(RoomPersistenceMapper::toDomain).toList();
    }

    @Override
    public Optional<Room> findById(Long id) {
        return repository.findById(id).map(RoomPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Room> findByRoomNumberIgnoreCase(String roomNumber) {
        return repository.findByRoomNumberIgnoreCase(roomNumber).map(RoomPersistenceMapper::toDomain);
    }

    @Override
    public void delete(Room room) {
        repository.deleteById(room.getId());
    }

    @Override
    public long countByStatus(RoomStatus status) {
        return repository.countByStatus(status);
    }

    @Override
    public boolean existsByRoomNumber(String roomNumber) {
        return repository.existsByRoomNumber(roomNumber);
    }

    @Override
    public boolean existsByRoomNumberAndIdNot(String roomNumber, Long id) {
        return repository.existsByRoomNumberAndIdNot(roomNumber, id);
    }
}
