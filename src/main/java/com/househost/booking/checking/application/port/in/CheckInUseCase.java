package com.househost.booking.checking.application.port.in;

import com.househost.booking.checking.application.dto.CheckInRequestDTO;
import com.househost.booking.checking.application.dto.CheckInResponseDTO;

import java.util.List;

public interface CheckInUseCase {
    CheckInResponseDTO create(CheckInRequestDTO request);
    List<CheckInResponseDTO> findAll();
    CheckInResponseDTO findById(Long id);
    CheckInResponseDTO update(Long id, CheckInRequestDTO request);
    void delete(Long id);
}
