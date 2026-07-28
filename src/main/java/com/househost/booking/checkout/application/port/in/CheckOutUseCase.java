package com.househost.booking.checkout.application.port.in;

import com.househost.booking.checkout.application.dto.CheckOutRequestDTO;
import com.househost.booking.checkout.application.dto.CheckOutResponseDTO;

import java.util.List;

public interface CheckOutUseCase {
    CheckOutResponseDTO create(CheckOutRequestDTO request);
    List<CheckOutResponseDTO> findAll();
    CheckOutResponseDTO findById(Long id);
    CheckOutResponseDTO update(Long id, CheckOutRequestDTO request);
    void delete(Long id);
}
