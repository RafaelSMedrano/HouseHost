package com.househost.room.adapter.out.persistence.entity;

import com.househost.room.domain.model.Room;

public final class RoomPersistenceMapper {

    private RoomPersistenceMapper() {
    }

    public static Room toDomain(RoomJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Room room = new Room(entity.roomNumber, entity.type, entity.capacity, entity.dailyRate, entity.status);
        room.restorePersistenceState(entity.id, entity.createdAt, entity.updatedAt);
        return room;
    }

    public static RoomJpaEntity toEntity(Room room) {
        if (room == null) {
            return null;
        }
        if (room instanceof RoomJpaEntity entity) {
            return entity;
        }
        RoomJpaEntity entity = new RoomJpaEntity();
        entity.id = room.getId();
        entity.roomNumber = room.getRoomNumber();
        entity.type = room.getType();
        entity.capacity = room.getCapacity();
        entity.dailyRate = room.getDailyRate();
        entity.status = room.getStatus();
        entity.createdAt = room.getCreatedAt();
        entity.updatedAt = room.getUpdatedAt();
        return entity;
    }
}
