package com.househost.finance.cashier.application.service;

import com.househost.finance.cashier.application.port.in.CashierUseCase;
import com.househost.finance.cashier.application.port.out.CashierEntryPersistencePort;
import com.househost.finance.cashier.application.port.out.CashierExpensePersistencePort;
import com.househost.finance.cashier.application.port.out.CashierPersistencePort;
import com.househost.finance.cashier.domain.model.Cashier;
import com.househost.finance.cashier.domain.model.CashierEntry;
import com.househost.finance.cashier.domain.model.CashierExpense;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
class CashierMovementService {

    private static final String FINANCIAL_TRANSACTION_SOURCE = "FINANCIAL_TRANSACTION";

    private final CashierUseCase cashierService;
    private final CashierPersistencePort cashierPersistence;
    private final CashierEntryPersistencePort entryPersistence;
    private final CashierExpensePersistencePort expensePersistence;
    private final CashierMovementValidationService validationService;

    CashierMovementService(
            CashierUseCase cashierService,
            CashierPersistencePort cashierPersistence,
            CashierEntryPersistencePort entryPersistence,
            CashierExpensePersistencePort expensePersistence,
            CashierMovementValidationService validationService
    ) {
        this.cashierService = cashierService;
        this.cashierPersistence = cashierPersistence;
        this.entryPersistence = entryPersistence;
        this.expensePersistence = expensePersistence;
        this.validationService = validationService;
    }

    @Transactional
    CashierEntry settleDeposit(Long cashierId, FinancialTransaction transaction) {
        validationService.validatePositiveAmount(transaction.getAmount());
        Cashier cashier = cashierService.findCashierById(cashierId);
        CashierEntry entry = findEntry(transaction.getId(), cashierId);
        validationService.validateMovementAmount(entry.getAmount(), transaction.getAmount());

        if (entry.getStatus() == FinancialTransactionStatus.SETTLED) {
            return entry;
        }
        validationService.validateWaitingStatus(entry.getStatus());

        cashier.settleWaitingEntry(entry);
        entry.setStatus(FinancialTransactionStatus.SETTLED);
        cashierPersistence.save(cashier);
        return entryPersistence.save(entry);
    }

    @Transactional
    CashierEntry scheduleDeposit(Long cashierId, FinancialTransaction transaction) {
        BigDecimal amount = transaction.getAmount();
        validationService.validatePositiveAmount(amount);
        var existingEntry = entryPersistence.findBySourceTransactionIdAndCashierId(transaction.getId(), cashierId);
        if (existingEntry.isPresent()) {
            validationService.validateMovementAmount(existingEntry.get().getAmount(), amount);
            return existingEntry.get();
        }

        Cashier cashier = cashierService.findCashierById(cashierId);
        CashierEntry entry = new CashierEntry(
                cashier,
                "Entrada da transacao financeira #" + transaction.getId(),
                amount,
                LocalDate.now(),
                FINANCIAL_TRANSACTION_SOURCE,
                FinancialTransactionStatus.WAITING,
                transaction
        );

        cashier.deposit(entry);
        CashierEntry savedEntry = entryPersistence.save(entry);
        cashierPersistence.save(cashier);
        return savedEntry;
    }

    @Transactional
    CashierExpense settleWithdrawal(Long cashierId, FinancialTransaction transaction) {
        BigDecimal amount = transaction.getAmount();
        validationService.validatePositiveAmount(amount);
        Cashier cashier = cashierService.findCashierById(cashierId);
        CashierExpense expense = findExpense(transaction.getId(), cashierId);
        validationService.validateMovementAmount(expense.getAmount().abs(), amount);

        if (expense.getStatus() == FinancialTransactionStatus.SETTLED) {
            return expense;
        }
        validationService.validateWaitingStatus(expense.getStatus());
        validationService.validateSufficientBalance(cashier, amount);

        cashier.settleWaitingExpense(expense);
        expense.setStatus(FinancialTransactionStatus.SETTLED);
        cashierPersistence.save(cashier);
        return expensePersistence.save(expense);
    }

    @Transactional
    CashierExpense scheduleWithdrawal(Long cashierId, FinancialTransaction transaction) {
        BigDecimal amount = transaction.getAmount();
        validationService.validatePositiveAmount(amount);
        var existingExpense = expensePersistence.findBySourceTransactionIdAndCashierId(transaction.getId(), cashierId);
        if (existingExpense.isPresent()) {
            validationService.validateMovementAmount(existingExpense.get().getAmount().abs(), amount);
            return existingExpense.get();
        }

        Cashier cashier = cashierService.findCashierById(cashierId);
        CashierExpense expense = new CashierExpense(
                cashier,
                "Saida da transacao financeira #" + transaction.getId(),
                amount.negate(),
                LocalDate.now(),
                FINANCIAL_TRANSACTION_SOURCE,
                FinancialTransactionStatus.WAITING,
                transaction
        );

        cashier.withdraw(expense);
        CashierExpense savedExpense = expensePersistence.save(expense);
        cashierPersistence.save(cashier);
        return savedExpense;
    }

    @Transactional
    void reverseMovementsForTransaction(Long transactionId) {
        List<CashierExpense> expenses = expensePersistence.findBySourceTransactionId(transactionId);
        expenses.forEach(expense -> {
            Cashier cashier = expense.getCashier();
            cashier.removeExpense(expense);
            cashierPersistence.save(cashier);
        });
        expensePersistence.deleteAll(expenses);

        List<CashierEntry> entries = entryPersistence.findBySourceTransactionId(transactionId);
        entries.forEach(entry -> {
            Cashier cashier = entry.getCashier();
            cashier.removeEntry(entry);
            cashierPersistence.save(cashier);
        });
        entryPersistence.deleteAll(entries);
    }

    private CashierEntry findEntry(Long transactionId, Long cashierId) {
        return entryPersistence.findBySourceTransactionIdAndCashierId(transactionId, cashierId)
                .orElseThrow(() -> new FinanceException("Entrada da transacao nao encontrada para o caixa."));
    }

    private CashierExpense findExpense(Long transactionId, Long cashierId) {
        return expensePersistence.findBySourceTransactionIdAndCashierId(transactionId, cashierId)
                .orElseThrow(() -> new FinanceException("Saida da transacao nao encontrada para o caixa."));
    }

}
