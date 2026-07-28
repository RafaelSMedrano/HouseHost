package com.househost.room.application.dto;

import com.househost.room.domain.model.RoomStatus;
import com.househost.room.domain.model.RoomType;

import java.math.BigDecimal;

public class RoomRequestDTO {

    public String roomNumber;
    public RoomType type;
    public Integer capacity;
    public BigDecimal dailyRate;
    public RoomStatus status;
}
