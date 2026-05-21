package com.househost.finance.service;

import com.househost.finance.dto.CashierExpenseRequestDTO;
import com.househost.finance.dto.CashierExpenseResponseDTO;
import com.househost.finance.model.Cashier;
import com.househost.finance.model.CashierExpense;
import com.househost.finance.model.FinancialTransaction;
import com.househost.finance.model.FinancialTransactionStatus;
import com.househost.finance.repository.CashierExpenseRepository;
import com.househost.finance.repository.FinancialTransactionRepository;
import com.househost.shared.dto.ResponseDTO;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CashierExpenseService {

    private final CashierExpenseRepository cashierExpenseRepository;
    private final CashierService cashierService;
    private final FinancialTransactionRepository financialTransactionRepository;

    public CashierExpenseService(CashierExpenseRepository cashierExpenseRepository, CashierService cashierService, FinancialTransactionRepository financialTransactionRepository) {
        this.cashierExpenseRepository = cashierExpenseRepository;
        this.cashierService = cashierService;
        this.financialTransactionRepository = financialTransactionRepository;
    }

    public ResponseDTO create(CashierExpenseRequestDTO request) {
        validateRequest(request);

        Cashier cashier = cashierService.findCashierById(request.cashierId);
        FinancialTransaction sourceTransaction = findSourceTransactionIfPresent(request.sourceTransactionId);
        CashierExpense expense = new CashierExpense(
                cashier,
                null,
                normalizeRequired(request.description),
                request.amount,
                normalizeDate(request.expenseDate),
                normalizeOptional(request.category),
                parseStatus(request.status),
                sourceTransaction
        );

        CashierExpense savedExpense = cashierExpenseRepository.save(expense);

        return new ResponseDTO("success", "Saida cadastrada com sucesso", new CashierExpenseResponseDTO(savedExpense));
    }

    public ResponseDTO findAll() {
        List<CashierExpenseResponseDTO> expenses = cashierExpenseRepository.findAll()
                .stream()
                .map(CashierExpenseResponseDTO::new)
                .toList();

        return new ResponseDTO("success", "Saidas encontradas com sucesso", expenses);
    }

    public ResponseDTO findByCashierId(Long cashierId) {
        cashierService.findCashierById(cashierId);

        List<CashierExpenseResponseDTO> expenses = cashierExpenseRepository.findByCashierId(cashierId)
                .stream()
                .map(CashierExpenseResponseDTO::new)
                .toList();

        return new ResponseDTO("success", "Saidas do caixa encontradas com sucesso", expenses);
    }

    public ResponseDTO findById(Long id) {
        CashierExpense expense = findExpenseById(id);
        return new ResponseDTO("success", "Saida encontrada com sucesso", new CashierExpenseResponseDTO(expense));
    }

    public ResponseDTO update(Long id, CashierExpenseRequestDTO request) {
        validateRequest(request);

        CashierExpense expense = findExpenseById(id);
        Cashier cashier = cashierService.findCashierById(request.cashierId);
        FinancialTransaction sourceTransaction = findSourceTransactionIfPresent(request.sourceTransactionId);
        expense.updateExpense(
                cashier,
                normalizeRequired(request.description),
                request.amount,
                normalizeDate(request.expenseDate),
                normalizeOptional(request.category),
                parseStatus(request.status),
                sourceTransaction
        );

        CashierExpense savedExpense = cashierExpenseRepository.save(expense);
        return new ResponseDTO("success", "Saida atualizada com sucesso", new CashierExpenseResponseDTO(savedExpense));
    }

    public ResponseDTO delete(Long id) {
        CashierExpense expense = findExpenseById(id);
        cashierExpenseRepository.delete(expense);
        return new ResponseDTO("success", "Saida removida com sucesso", null);
    }

    public CashierExpense findExpenseById(Long id) {
        if (id == null) {
            throw new FinanceException("Saida nao encontrada.");
        }

        return cashierExpenseRepository.findById(id)
                .orElseThrow(() -> new FinanceException("Saida nao encontrada."));
    }

    private FinancialTransaction findSourceTransactionIfPresent(Long sourceTransactionId) {
        if (sourceTransactionId == null) {
            return null;
        }

        return financialTransactionRepository.findById(sourceTransactionId)
                .orElseThrow(() -> new FinanceException("Transacao financeira de origem nao encontrada."));
    }

    private void validateRequest(CashierExpenseRequestDTO request) {
        if (request == null) {
            throw new FinanceException("Dados da saida sao obrigatorios.");
        }

        if (request.cashierId == null) {
            throw new FinanceException("Caixa da saida e obrigatorio.");
        }

        if (isBlank(request.description)) {
            throw new FinanceException("Descricao da saida e obrigatoria.");
        }

        if (request.amount == null || request.amount.compareTo(BigDecimal.ZERO) >= 0) {
            throw new FinanceException("Valor da saida deve ser menor que zero.");
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
            throw new FinanceException("Status da saida invalido.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
