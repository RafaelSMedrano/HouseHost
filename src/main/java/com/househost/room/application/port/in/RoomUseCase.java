package com.househost.room.application.port.in;

import com.househost.room.application.dto.RoomRequestDTO;
import com.househost.room.application.dto.RoomResponseDTO;

import java.util.List;

public interface RoomUseCase {

    RoomResponseDTO create(RoomRequestDTO request);

    List<RoomResponseDTO> findAll();

    RoomResponseDTO findById(Long id);

    RoomResponseDTO update(Long id, RoomRequestDTO request);

    void delete(Long id);
}
