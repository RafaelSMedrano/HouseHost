package com.househost.finance.cashier.application.service;

import com.househost.finance.cashier.application.dto.CashierRequestDTO;
import com.househost.finance.cashier.application.dto.CashierResponseDTO;
import com.househost.finance.cashier.application.dto.CashierUpdateRequestDTO;
import com.househost.finance.cashier.application.port.in.CashierUseCase;
import com.househost.finance.cashier.application.port.out.CashierPersistencePort;
import com.househost.finance.cashier.domain.model.Cashier;
import com.househost.finance.cashier.domain.model.CashierStatus;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CashierService implements CashierUseCase {

    private final CashierPersistencePort cashierRepository;
    private final CashierValidationService validationService;

    public CashierService(
            CashierPersistencePort cashierRepository,
            CashierValidationService validationService
    ) {
        this.cashierRepository = cashierRepository;
        this.validationService = validationService;
    }

    @Override
    @Transactional
    public CashierResponseDTO create(CashierRequestDTO request) {
        validationService.validateCreate(request);

        String name = normalizeRequired(request.name);

        Cashier cashier = new Cashier(
                name,
                normalizeOptional(request.description),
                request.openingBalance,
                request.status == null ? CashierStatus.OPEN : request.status
        );

        Cashier savedCashier = cashierRepository.save(cashier);
        return new CashierResponseDTO(savedCashier);
    }

    @Override
    public List<CashierResponseDTO> findAll() {
        return cashierRepository.findAll()
                .stream()
                .map(CashierResponseDTO::new)
                .toList();
    }

    @Override
    public CashierResponseDTO findById(Long id) {
        Cashier cashier = findCashierById(id);
        return new CashierResponseDTO(cashier);
    }

    @Override
    @Transactional
    public CashierResponseDTO update(Long id, CashierUpdateRequestDTO request) {
        validationService.validateUpdate(id, request);

        Cashier cashier = findCashierById(id);
        String name = normalizeRequired(request.name);

        cashier.update(
                name,
                normalizeOptional(request.description),
                request.status == null ? cashier.getStatus() : request.status
        );

        Cashier savedCashier = cashierRepository.save(cashier);
        return new CashierResponseDTO(savedCashier);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Cashier cashier = findCashierById(id);
        validationService.validateCanDelete(cashier);
        cashierRepository.delete(cashier);
    }

    @Override
    public Cashier findCashierById(Long id) {
        if (id == null) {
            throw new FinanceException("Caixa nao encontrado.");
        }

        return cashierRepository.findById(id)
                .orElseThrow(() -> new FinanceException("Caixa nao encontrado."));
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (isBlank(value)) {
            return null;
        }

        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
