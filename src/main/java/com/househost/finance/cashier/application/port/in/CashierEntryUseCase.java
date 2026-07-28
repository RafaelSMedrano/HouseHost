package com.househost.finance.cashier.application.port.in;

import com.househost.finance.cashier.application.dto.CashierEntryResponseDTO;

import java.util.List;

public interface CashierEntryUseCase {
    List<CashierEntryResponseDTO> findAll();
    List<CashierEntryResponseDTO> findByCashierId(Long cashierId);
    CashierEntryResponseDTO findById(Long id);
}
