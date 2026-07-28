package com.househost.finance.financialtransaction.application.port.in;

import com.househost.finance.financialtransaction.application.dto.FinancialTransactionRequestDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionResponseDTO;

import java.util.List;

public interface FinancialTransactionUseCase {
    FinancialTransactionResponseDTO create(FinancialTransactionRequestDTO request);
    List<FinancialTransactionResponseDTO> findAll();
    FinancialTransactionResponseDTO findById(Long id);
    FinancialTransactionResponseDTO update(Long id, FinancialTransactionRequestDTO request);
    FinancialTransactionResponseDTO toSettle(Long id);
    void delete(Long id);
}
