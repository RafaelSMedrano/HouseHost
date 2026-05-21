package com.househost.finance.service;

import com.househost.finance.dto.CashierEntryRequestDTO;
import com.househost.finance.dto.CashierEntryResponseDTO;
import com.househost.finance.model.Cashier;
import com.househost.finance.model.CashierEntry;
import com.househost.finance.model.FinancialTransaction;
import com.househost.finance.model.FinancialTransactionStatus;
import com.househost.finance.repository.CashierEntryRepository;
import com.househost.finance.repository.FinancialTransactionRepository;
import com.househost.shared.dto.ResponseDTO;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CashierEntryService {

    private final CashierEntryRepository cashierEntryRepository;
    private final CashierService cashierService;
    private final FinancialTransactionRepository financialTransactionRepository;

    public CashierEntryService(CashierEntryRepository cashierEntryRepository, CashierService cashierService, FinancialTransactionRepository financialTransactionRepository) {
        this.cashierEntryRepository = cashierEntryRepository;
        this.cashierService = cashierService;
        this.financialTransactionRepository = financialTransactionRepository;
    }

    public ResponseDTO create(CashierEntryRequestDTO request) {
        validateRequest(request);

        Cashier cashier = cashierService.findCashierById(request.cashierId);
        FinancialTransaction sourceTransaction = findSourceTransactionIfPresent(request.sourceTransactionId);
        CashierEntry entry = new CashierEntry(
                cashier,
                null,
                normalizeRequired(request.description),
                request.amount,
                normalizeDate(request.entryDate),
                normalizeOptional(request.source),
                parseStatus(request.status),
                sourceTransaction
        );

        CashierEntry savedEntry = cashierEntryRepository.save(entry);

        return new ResponseDTO("success", "Entrada cadastrada com sucesso", new CashierEntryResponseDTO(savedEntry));
    }

    public ResponseDTO findAll() {
        List<CashierEntryResponseDTO> entries = cashierEntryRepository.findAll()
                .stream()
                .map(CashierEntryResponseDTO::new)
                .toList();

        return new ResponseDTO("success", "Entradas encontradas com sucesso", entries);
    }

    public ResponseDTO findByCashierId(Long cashierId) {
        cashierService.findCashierById(cashierId);

        List<CashierEntryResponseDTO> entries = cashierEntryRepository.findByCashierId(cashierId)
                .stream()
                .map(CashierEntryResponseDTO::new)
                .toList();

        return new ResponseDTO("success", "Entradas do caixa encontradas com sucesso", entries);
    }

    public ResponseDTO findById(Long id) {
        CashierEntry entry = findEntryById(id);
        return new ResponseDTO("success", "Entrada encontrada com sucesso", new CashierEntryResponseDTO(entry));
    }

    public ResponseDTO update(Long id, CashierEntryRequestDTO request) {
        validateRequest(request);

        CashierEntry entry = findEntryById(id);
        Cashier cashier = cashierService.findCashierById(request.cashierId);
        FinancialTransaction sourceTransaction = findSourceTransactionIfPresent(request.sourceTransactionId);
        entry.updateEntry(
                cashier,
                normalizeRequired(request.description),
                request.amount,
                normalizeDate(request.entryDate),
                normalizeOptional(request.source),
                parseStatus(request.status),
                sourceTransaction
        );

        CashierEntry savedEntry = cashierEntryRepository.save(entry);
        return new ResponseDTO("success", "Entrada atualizada com sucesso", new CashierEntryResponseDTO(savedEntry));
    }

    public ResponseDTO delete(Long id) {
        CashierEntry entry = findEntryById(id);
        cashierEntryRepository.delete(entry);
        return new ResponseDTO("success", "Entrada removida com sucesso", null);
    }

    public CashierEntry findEntryById(Long id) {
        if (id == null) {
            throw new FinanceException("Entrada nao encontrada.");
        }

        return cashierEntryRepository.findById(id)
                .orElseThrow(() -> new FinanceException("Entrada nao encontrada."));
    }

    private FinancialTransaction findSourceTransactionIfPresent(Long sourceTransactionId) {
        if (sourceTransactionId == null) {
            return null;
        }

        return financialTransactionRepository.findById(sourceTransactionId)
                .orElseThrow(() -> new FinanceException("Transacao financeira de origem nao encontrada."));
    }

    private void validateRequest(CashierEntryRequestDTO request) {
        if (request == null) {
            throw new FinanceException("Dados da entrada sao obrigatorios.");
        }

        if (request.cashierId == null) {
            throw new FinanceException("Caixa da entrada e obrigatorio.");
        }

        if (isBlank(request.description)) {
            throw new FinanceException("Descricao da entrada e obrigatoria.");
        }

        if (request.amount == null || request.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new FinanceException("Valor da entrada deve ser maior que zero.");
        }
    }

    private LocalDate normalizeDate(LocalDate date) {
        if (date == null) {
            return LocalDate.now();
        }

        return date;
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

    private FinancialTransactionStatus parseStatus(String status) {
        if (isBlank(status)) {
            return FinancialTransactionStatus.SETTLED;
        }

        try {
            return FinancialTransactionStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new FinanceException("Status da entrada invalido.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
