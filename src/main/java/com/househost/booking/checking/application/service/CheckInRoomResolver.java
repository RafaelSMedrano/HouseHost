package com.househost.booking.checking.application.service;

import com.househost.room.application.service.RoomService;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import org.springframework.stereotype.Service;

@Service
public class CheckInRoomResolver {

    private final RoomService roomService;

    public CheckInRoomResolver(RoomService roomService) {
        this.roomService = roomService;
    }

    void resolveRoomStatus(Room room) {
        roomService.changeStatus(room.getId(), RoomStatus.OCCUPIED);
    }
}
