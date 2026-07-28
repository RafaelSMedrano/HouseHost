package com.househost.finance.cashier.application.port.in;

import com.househost.finance.cashier.application.dto.CashierRequestDTO;
import com.househost.finance.cashier.application.dto.CashierResponseDTO;
import com.househost.finance.cashier.application.dto.CashierUpdateRequestDTO;
import com.househost.finance.cashier.domain.model.Cashier;

import java.util.List;

public interface CashierUseCase {
    CashierResponseDTO create(CashierRequestDTO request);
    List<CashierResponseDTO> findAll();
    CashierResponseDTO findById(Long id);
    Cashier findCashierById(Long id);
    CashierResponseDTO update(Long id, CashierUpdateRequestDTO request);
    void delete(Long id);
}
