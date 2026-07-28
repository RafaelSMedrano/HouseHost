package com.househost.finance.cashier.application.port.in;

import com.househost.finance.cashier.application.dto.CashierExpenseResponseDTO;

import java.util.List;

public interface CashierExpenseUseCase {
    List<CashierExpenseResponseDTO> findAll();
    List<CashierExpenseResponseDTO> findByCashierId(Long cashierId);
    CashierExpenseResponseDTO findById(Long id);
}
