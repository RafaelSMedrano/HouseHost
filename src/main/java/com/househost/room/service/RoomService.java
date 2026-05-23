package com.househost.room.service;

import com.househost.room.dto.RoomRequestDTO;
import com.househost.room.dto.RoomResponseDTO;
import com.househost.room.model.Room;
import com.househost.room.model.RoomStatus;
import com.househost.room.model.RoomType;
import com.househost.room.repository.RoomRepository;
import com.househost.shared.exception.RoomException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public RoomResponseDTO create(RoomRequestDTO request) {
        validateRequest(request);

        String roomNumber = normalizeRequired(request.roomNumber);
        validateUniqueRoomNumber(roomNumber);

        Room room = new Room(
                roomNumber,
                parseRoomType(request.type),
                request.capacity,
                request.dailyRate,
                parseRoomStatus(request.status)
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
        validateRequest(request);

        Room room = findRoomById(id);
        String roomNumber = normalizeRequired(request.roomNumber);
        validateUniqueRoomNumber(roomNumber, id);

        room.updateProfile(
                roomNumber,
                parseRoomType(request.type),
                request.capacity,
                request.dailyRate,
                parseRoomStatus(request.status)
        );

        Room savedRoom = roomRepository.save(room);
        return new RoomResponseDTO(savedRoom);
    }

    public void delete(Long id) {
        Room room = findRoomById(id);
        roomRepository.delete(room);
    }

    private Room findRoomById(Long id) {
        if (id == null) {
            throw new RoomException("Quarto nao encontrado.");
        }

        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomException("Quarto nao encontrado."));
    }

    private void validateRequest(RoomRequestDTO request) {
        if (request == null) {
            throw new RoomException("Dados do quarto sao obrigatorios.");
        }

        if (isBlank(request.roomNumber)) {
            throw new RoomException("Numero do quarto e obrigatorio.");
        }

        if (isBlank(request.type)) {
            throw new RoomException("Tipo do quarto e obrigatorio.");
        }

        if (request.capacity == null || request.capacity <= 0) {
            throw new RoomException("Capacidade do quarto deve ser maior que zero.");
        }

        if (request.dailyRate == null || request.dailyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new RoomException("Diaria do quarto nao pode ser negativa.");
        }
    }

    private void validateUniqueRoomNumber(String roomNumber) {
        if (roomRepository.existsByRoomNumber(roomNumber)) {
            throw new RoomException("Numero do quarto ja esta cadastrado.");
        }
    }

    private void validateUniqueRoomNumber(String roomNumber, Long id) {
        if (roomRepository.existsByRoomNumberAndIdNot(roomNumber, id)) {
            throw new RoomException("Numero do quarto ja esta cadastrado.");
        }
    }

    private RoomType parseRoomType(String type) {
        try {
            return RoomType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new RoomException("Tipo do quarto invalido. Use SINGLE, DOUBLE, SUITE, FAMILY ou STANDARD.");
        }
    }

    private RoomStatus parseRoomStatus(String status) {
        if (isBlank(status)) {
            return RoomStatus.AVAILABLE;
        }

        try {
            return RoomStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new RoomException("Status do quarto invalido. Use AVAILABLE, OCCUPIED, MAINTENANCE ou INACTIVE.");
        }
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
