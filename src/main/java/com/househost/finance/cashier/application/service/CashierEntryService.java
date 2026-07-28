package com.househost.finance.cashier.application.service;

import com.househost.finance.cashier.application.dto.CashierEntryResponseDTO;
import com.househost.finance.cashier.application.port.in.CashierEntryUseCase;
import com.househost.finance.cashier.application.port.in.CashierUseCase;
import com.househost.finance.cashier.application.port.out.CashierEntryPersistencePort;
import com.househost.finance.cashier.domain.model.CashierEntry;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CashierEntryService implements CashierEntryUseCase {

    private final CashierEntryPersistencePort cashierEntryRepository;
    private final CashierUseCase cashierService;

    public CashierEntryService(CashierEntryPersistencePort cashierEntryRepository, CashierUseCase cashierService) {
        this.cashierEntryRepository = cashierEntryRepository;
        this.cashierService = cashierService;
    }

    public List<CashierEntryResponseDTO> findAll() {
        return cashierEntryRepository.findAll()
                .stream()
                .map(CashierEntryResponseDTO::new)
                .toList();
    }

    public List<CashierEntryResponseDTO> findByCashierId(Long cashierId) {
        cashierService.findCashierById(cashierId);

        return cashierEntryRepository.findByCashierId(cashierId)
                .stream()
                .map(CashierEntryResponseDTO::new)
                .toList();
    }

    public CashierEntryResponseDTO findById(Long id) {
        CashierEntry entry = findEntryById(id);
        return new CashierEntryResponseDTO(entry);
    }

    public CashierEntry findEntryById(Long id) {
        if (id == null) {
            throw new FinanceException("Entrada nao encontrada.");
        }

        return cashierEntryRepository.findById(id)
                .orElseThrow(() -> new FinanceException("Entrada nao encontrada."));
    }

}
