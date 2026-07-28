package com.househost.finance.cashier.application.service;

import com.househost.finance.cashier.application.dto.CashierExpenseResponseDTO;
import com.househost.finance.cashier.application.port.in.CashierExpenseUseCase;
import com.househost.finance.cashier.application.port.in.CashierUseCase;
import com.househost.finance.cashier.application.port.out.CashierExpensePersistencePort;
import com.househost.finance.cashier.domain.model.CashierExpense;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CashierExpenseService implements CashierExpenseUseCase {

    private final CashierExpensePersistencePort cashierExpenseRepository;
    private final CashierUseCase cashierService;

    public CashierExpenseService(CashierExpensePersistencePort cashierExpenseRepository, CashierUseCase cashierService) {
        this.cashierExpenseRepository = cashierExpenseRepository;
        this.cashierService = cashierService;
    }

    public List<CashierExpenseResponseDTO> findAll() {
        return cashierExpenseRepository.findAll()
                .stream()
                .map(CashierExpenseResponseDTO::new)
                .toList();
    }

    public List<CashierExpenseResponseDTO> findByCashierId(Long cashierId) {
        cashierService.findCashierById(cashierId);

        return cashierExpenseRepository.findByCashierId(cashierId)
                .stream()
                .map(CashierExpenseResponseDTO::new)
                .toList();
    }

    public CashierExpenseResponseDTO findById(Long id) {
        CashierExpense expense = findExpenseById(id);
        return new CashierExpenseResponseDTO(expense);
    }

    public CashierExpense findExpenseById(Long id) {
        if (id == null) {
            throw new FinanceException("Saida nao encontrada.");
        }

        return cashierExpenseRepository.findById(id)
                .orElseThrow(() -> new FinanceException("Saida nao encontrada."));
    }

}
