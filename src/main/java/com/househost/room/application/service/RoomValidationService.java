package com.househost.room.application.service;

import com.househost.room.application.dto.RoomRequestDTO;
import com.househost.room.application.port.out.RoomPersistencePort;
import com.househost.shared.exception.RoomException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
class RoomValidationService {

    private final RoomPersistencePort roomRepository;

    RoomValidationService(RoomPersistencePort roomRepository) {
        this.roomRepository = roomRepository;
    }

    void validateCreate(RoomRequestDTO request) {
        validateRequest(request);
        validateUniqueRoomNumber(request.roomNumber.trim(), null);
    }

    void validateUpdate(Long roomId, RoomRequestDTO request) {
        validateRequest(request);
        if (roomId == null) {
            throw new RoomException("Quarto nao encontrado.");
        }
        validateUniqueRoomNumber(request.roomNumber.trim(), roomId);
    }

    private void validateRequest(RoomRequestDTO request) {
        if (request == null) {
            throw new RoomException("Dados do quarto sao obrigatorios.");
        }
        if (request.roomNumber == null || request.roomNumber.isBlank()) {
            throw new RoomException("Numero do quarto e obrigatorio.");
        }
        if (request.type == null) {
            throw new RoomException("Tipo do quarto e obrigatorio.");
        }
        if (request.capacity == null || request.capacity <= 0) {
            throw new RoomException("Capacidade do quarto deve ser maior que zero.");
        }
        if (request.dailyRate == null || request.dailyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new RoomException("Diaria do quarto nao pode ser negativa.");
        }
    }

    private void validateUniqueRoomNumber(String roomNumber, Long roomId) {
        boolean duplicated = roomId == null
                ? roomRepository.existsByRoomNumber(roomNumber)
                : roomRepository.existsByRoomNumberAndIdNot(roomNumber, roomId);
        if (duplicated) {
            throw new RoomException("Numero do quarto ja esta cadastrado.");
        }
    }
}
