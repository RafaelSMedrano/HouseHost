package com.househost.room.application.service;

import com.househost.room.application.dto.RoomRequestDTO;
import com.househost.room.application.dto.RoomResponseDTO;
import com.househost.room.application.port.in.RoomUseCase;
import com.househost.room.application.port.out.RoomPersistencePort;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import com.househost.shared.exception.RoomException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService implements RoomUseCase {

    private final RoomPersistencePort roomRepository;
    private final RoomValidationService roomValidationService;

    public RoomService(RoomPersistencePort roomRepository, RoomValidationService roomValidationService) {
        this.roomRepository = roomRepository;
        this.roomValidationService = roomValidationService;
    }

    public RoomResponseDTO create(RoomRequestDTO request) {
        roomValidationService.validateCreate(request);
        String roomNumber = normalizeRequired(request.roomNumber);

        Room room = new Room(
                roomNumber,
                request.type,
                request.capacity,
                request.dailyRate,
                request.status == null ? RoomStatus.AVAILABLE : request.status
        );

        Room savedRoom = roomRepository.save(room);
        return new RoomResponseDTO(savedRoom);
    }

    public List<RoomResponseDTO> findAll() {
        return roomRepository.findAll()
                .stream()
                .map(RoomResponseDTO::new)
                .toList();
    }

    public RoomResponseDTO findById(Long id) {
        Room room = findRoomById(id);
        return new RoomResponseDTO(room);
    }

    public RoomResponseDTO update(Long id, RoomRequestDTO request) {
        roomValidationService.validateUpdate(id, request);
        Room room = findRoomById(id);
        String roomNumber = normalizeRequired(request.roomNumber);

        room.updateProfile(
                roomNumber,
                request.type,
                request.capacity,
                request.dailyRate,
                request.status == null ? RoomStatus.AVAILABLE : request.status
        );

        Room savedRoom = roomRepository.save(room);
        return new RoomResponseDTO(savedRoom);
    }

    public void delete(Long id) {
        Room room = findRoomById(id);
        roomRepository.delete(room);
    }

    public Room findRoomById(Long id) {
        if (id == null) {
            throw new RoomException("Quarto nao encontrado.");
        }

        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomException("Quarto nao encontrado."));
    }

    public Room findRoomByNumber(String roomNumber) {
        String normalizedRoomNumber = normalizeRequiredRoomNumber(roomNumber);
        return roomRepository.findByRoomNumberIgnoreCase(normalizedRoomNumber)
                .orElseThrow(() -> new RoomException("Quarto nao encontrado."));
    }

    public Room changeStatus(Long id, RoomStatus status) {
        Room room = findRoomById(id);
        room.changeStatus(status);
        return roomRepository.save(room);
    }

    public List<Room> findAllRooms() {
        return roomRepository.findAll();
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeRequiredRoomNumber(String value) {
        if (isBlank(value)) {
            throw new RoomException("Numero do quarto e obrigatorio.");
        }

        return normalizeRequired(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}
