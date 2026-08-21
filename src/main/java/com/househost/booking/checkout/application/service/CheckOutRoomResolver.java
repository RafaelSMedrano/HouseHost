package com.househost.booking.checkout.application.service;

import com.househost.room.application.service.RoomService;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import org.springframework.stereotype.Service;

@Service
public class CheckOutRoomResolver {

    private final RoomService roomService;

    public CheckOutRoomResolver(RoomService roomService) {
        this.roomService = roomService;
    }

    void resolveRoomStatus(Room room, boolean roomInspected) {
        if (room == null || room.getStatus() == RoomStatus.INACTIVE) {
            return;
        }
        roomService.changeStatus(
                room.getId(),
                roomInspected ? RoomStatus.AVAILABLE : RoomStatus.MAINTENANCE
        );
    }
}
