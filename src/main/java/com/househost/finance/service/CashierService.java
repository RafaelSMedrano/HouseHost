package com.househost.finance.service;

import com.househost.finance.dto.CashierRequestDTO;
import com.househost.finance.dto.CashierResponseDTO;
import com.househost.finance.model.Cashier;
import com.househost.finance.model.CashierEntry;
import com.househost.finance.model.CashierExpense;
import com.househost.finance.model.CashierStatus;
import com.househost.finance.model.FinancialTransaction;
import com.househost.finance.model.FinancialPartyType;
import com.househost.finance.model.FinancialTransactionStatus;
import com.househost.finance.repository.CashierEntryRepository;
import com.househost.finance.repository.CashierExpenseRepository;
import com.househost.finance.repository.CashierRepository;
import com.househost.finance.repository.FinancialTransactionRepository;
import com.househost.shared.dto.ResponseDTO;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CashierService {

    private final CashierRepository cashierRepository;
    private final CashierEntryRepository cashierEntryRepository;
    private final CashierExpenseRepository cashierExpenseRepository;
    private final FinancialTransactionRepository financialTransactionRepository;

    public CashierService(CashierRepository cashierRepository, CashierEntryRepository cashierEntryRepository, CashierExpenseRepository cashierExpenseRepository, FinancialTransactionRepository financialTransactionRepository) {
        this.cashierRepository = cashierRepository;
        this.cashierEntryRepository = cashierEntryRepository;
        this.cashierExpenseRepository = cashierExpenseRepository;
        this.financialTransactionRepository = financialTransactionRepository;
    }

    public ResponseDTO create(CashierRequestDTO request) {
        validateRequest(request);

        String name = normalizeRequired(request.name);
        validateUniqueName(name);

        Cashier cashier = new Cashier(
                name,
                normalizeOptional(request.description),
                request.openingBalance,
                normalizeMoney(request.cashOnHand),
                normalizeMoney(request.expectedInflow),
                normalizeMoney(request.expectedOutflow),
                normalizeMoney(request.totalInflow),
                normalizeMoney(request.totalOutflow),
                parseCashierStatus(request.status)
        );

        Cashier savedCashier = cashierRepository.save(cashier);
        return new ResponseDTO("success", "Caixa cadastrado com sucesso", new CashierResponseDTO(savedCashier));
    }

    public ResponseDTO findAll() {
        List<CashierResponseDTO> cashiers = cashierRepository.findAll()
                .stream()
                .map(CashierResponseDTO::new)
                .toList();

        return new ResponseDTO("success", "Caixas encontrados com sucesso", cashiers);
    }

    public ResponseDTO findById(Long id) {
        Cashier cashier = findCashierById(id);
        return new ResponseDTO("success", "Caixa encontrado com sucesso", new CashierResponseDTO(cashier));
    }

    public ResponseDTO update(Long id, CashierRequestDTO request) {
        validateRequest(request);

        Cashier cashier = findCashierById(id);
        String name = normalizeRequired(request.name);
        validateUniqueName(name, id);

        cashier.updateProfile(
                name,
                normalizeOptional(request.description),
                request.openingBalance,
                normalizeMoney(request.cashOnHand),
                normalizeMoney(request.expectedInflow),
                normalizeMoney(request.expectedOutflow),
                normalizeMoney(request.totalInflow),
                normalizeMoney(request.totalOutflow),
                parseCashierStatus(request.status)
        );

        Cashier savedCashier = cashierRepository.save(cashier);
        return new ResponseDTO("success", "Caixa atualizado com sucesso", new CashierResponseDTO(savedCashier));
    }

    public ResponseDTO delete(Long id) {
        Cashier cashier = findCashierById(id);
        cashierRepository.delete(cashier);
        return new ResponseDTO("success", "Caixa removido com sucesso", null);
    }

    public Cashier findCashierById(Long id) {
        if (id == null) {
            throw new FinanceException("Caixa nao encontrado.");
        }

        return cashierRepository.findById(id)
                .orElseThrow(() -> new FinanceException("Caixa nao encontrado."));
    }

    @Transactional
    public CashierEntry deposit(Long transactionId, Long cashierId, BigDecimal amount) {
        return deposit(transactionId, cashierId, amount, FinancialTransactionStatus.SETTLED);
    }

    @Transactional
    public CashierEntry deposit(Long transactionId, Long cashierId, BigDecimal amount, FinancialTransactionStatus status) {
        validatePositiveAmount(amount);

        Cashier cashier = findCashierById(cashierId);
        FinancialTransaction sourceTransaction = findTransactionById(transactionId);
        CashierEntry entry = new CashierEntry(
                cashier,
                sourceTransaction.getGuest(),
                "Entrada da transacao financeira #" + transactionId,
                amount,
                LocalDate.now(),
                "FINANCIAL_TRANSACTION_SETTLEMENT",
                normalizeMovementStatus(status),
                sourceTransaction
        );

        cashier.deposit(entry);
        CashierEntry savedEntry = cashierEntryRepository.save(entry);
        cashierRepository.save(cashier);
        return savedEntry;
    }

    @Transactional
    public CashierExpense withdraw(Long transactionId, Long cashierId, BigDecimal amount) {
        return withdraw(transactionId, cashierId, amount, FinancialTransactionStatus.SETTLED);
    }

    @Transactional
    public CashierExpense withdraw(Long transactionId, Long cashierId, BigDecimal amount, FinancialTransactionStatus status) {
        validatePositiveAmount(amount);

        Cashier cashier = findCashierById(cashierId);
        FinancialTransactionStatus movementStatus = normalizeMovementStatus(status);
        if (movementStatus != FinancialTransactionStatus.WAITING && cashier.getCashOnHand().compareTo(amount) < 0) {
            throw new FinanceException("Caixa pagante nao possui saldo suficiente para liquidar a transacao.");
        }

        FinancialTransaction sourceTransaction = findTransactionById(transactionId);
        CashierExpense expense = new CashierExpense(
                cashier,
                sourceTransaction.getGuest(),
                "Saida da transacao financeira #" + transactionId,
                amount.negate(),
                LocalDate.now(),
                "FINANCIAL_TRANSACTION_SETTLEMENT",
                movementStatus,
                sourceTransaction
        );

        cashier.withdraw(expense);
        CashierExpense savedExpense = cashierExpenseRepository.save(expense);
        cashierRepository.save(cashier);
        return savedExpense;
    }

    @Transactional
    public void createMovementsForTransaction(FinancialTransaction transaction) {
        FinancialTransactionStatus status = normalizeMovementStatus(transaction.getStatus());

        if (transaction.getSenderType() == FinancialPartyType.CASHIER) {
            withdraw(transaction.getId(), transaction.getSenderId(), transaction.getAmount(), status);
        }

        if (transaction.getReceiverType() == FinancialPartyType.CASHIER) {
            deposit(transaction.getId(), transaction.getReceiverId(), transaction.getAmount(), status);
        }
    }

    @Transactional
    public void settleMovementsForTransaction(Long transactionId) {
        List<CashierExpense> expenses = cashierExpenseRepository.findBySourceTransactionId(transactionId);
        expenses.forEach(expense -> {
            if (expense.getStatus() == FinancialTransactionStatus.WAITING) {
                Cashier cashier = expense.getCashier();
                BigDecimal amount = expense.getAmount().abs();
                if (cashier.getCashOnHand().compareTo(amount) < 0) {
                    throw new FinanceException("Caixa pagante nao possui saldo suficiente para liquidar a transacao.");
                }
                cashier.settleWaitingExpense(expense);
                cashierRepository.save(cashier);
            }
            expense.setStatus(FinancialTransactionStatus.SETTLED);
        });
        cashierExpenseRepository.saveAll(expenses);

        List<CashierEntry> entries = cashierEntryRepository.findBySourceTransactionId(transactionId);
        entries.forEach(entry -> {
            if (entry.getStatus() == FinancialTransactionStatus.WAITING) {
                Cashier cashier = entry.getCashier();
                cashier.settleWaitingEntry(entry);
                cashierRepository.save(cashier);
            }
            entry.setStatus(FinancialTransactionStatus.SETTLED);
        });
        cashierEntryRepository.saveAll(entries);
    }

    @Transactional
    public void removeMovementsForTransaction(Long transactionId) {
        List<CashierExpense> expenses = cashierExpenseRepository.findBySourceTransactionId(transactionId);
        expenses.forEach(expense -> {
            Cashier cashier = expense.getCashier();
            cashier.removeExpense(expense);
            cashierRepository.save(cashier);
        });
        cashierExpenseRepository.deleteAll(expenses);

        List<CashierEntry> entries = cashierEntryRepository.findBySourceTransactionId(transactionId);
        entries.forEach(entry -> {
            Cashier cashier = entry.getCashier();
            cashier.removeEntry(entry);
            cashierRepository.save(cashier);
        });
        cashierEntryRepository.deleteAll(entries);
    }

    private FinancialTransaction findTransactionById(Long id) {
        if (id == null) {
            throw new FinanceException("Transacao financeira nao encontrada.");
        }

        return financialTransactionRepository.findById(id)
                .orElseThrow(() -> new FinanceException("Transacao financeira nao encontrada."));
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new FinanceException("Valor da movimentacao deve ser maior que zero.");
        }
    }

    private FinancialTransactionStatus normalizeMovementStatus(FinancialTransactionStatus status) {
        return status == null ? FinancialTransactionStatus.WAITING : status;
    }

    private void validateRequest(CashierRequestDTO request) {
        if (request == null) {
            throw new FinanceException("Dados do caixa sao obrigatorios.");
        }

        if (isBlank(request.name)) {
            throw new FinanceException("Nome do caixa e obrigatorio.");
        }

        if (request.openingBalance == null || request.openingBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new FinanceException("Saldo inicial do caixa nao pode ser negativo.");
        }

        validateMoney(request.cashOnHand, "Valor em caixa nao pode ser negativo.");
        validateMoney(request.expectedInflow, "Valor previsto para entrar nao pode ser negativo.");
        validateMoney(request.expectedOutflow, "Valor previsto para sair nao pode ser negativo.");
        validateMoney(request.totalInflow, "Total de entradas nao pode ser negativo.");
        validateMoney(request.totalOutflow, "Total de saidas nao pode ser negativo.");
    }

    private void validateUniqueName(String name) {
        if (cashierRepository.existsByName(name)) {
            throw new FinanceException("Nome do caixa ja esta cadastrado.");
        }
    }

    private void validateUniqueName(String name, Long id) {
        if (cashierRepository.existsByNameAndIdNot(name, id)) {
            throw new FinanceException("Nome do caixa ja esta cadastrado.");
        }
    }

    private CashierStatus parseCashierStatus(String status) {
        if (isBlank(status)) {
            return CashierStatus.OPEN;
        }

        try {
            return CashierStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new FinanceException("Status do caixa invalido. Use OPEN, CLOSED ou INACTIVE.");
        }
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

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        return value;
    }

    private void validateMoney(BigDecimal value, String message) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new FinanceException(message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
