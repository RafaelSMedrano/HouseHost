package com.househost.finance.cashier.application.service;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Service
class CashierMovementService {

    private static final String FINANCIAL_TRANSACTION_SOURCE = "FINANCIAL_TRANSACTION";

    private final CashierPersistencePort cashierPersistencePort;
    private final CashierEntryPersistencePort cashierEntryPersistencePort;
    private final CashierExpensePersistencePort cashierExpensePersistencePort;
    private final CashierMovementValidationService cashierMovementValidationService;

    CashierMovementService(
            CashierPersistencePort cashierPersistencePort,
            CashierEntryPersistencePort cashierEntryPersistencePort,
            CashierExpensePersistencePort cashierExpensePersistencePort,
            CashierMovementValidationService cashierMovementValidationService
    ) {
        this.cashierPersistencePort = cashierPersistencePort;
        this.cashierEntryPersistencePort = cashierEntryPersistencePort;
        this.cashierExpensePersistencePort = cashierExpensePersistencePort;
        this.cashierMovementValidationService = cashierMovementValidationService;
    }

    @Transactional
    CashierEntry settleDeposit(Long cashierId, FinancialTransaction transaction) {
        cashierMovementValidationService.validatePositiveAmount(transaction.getAmount());
        Cashier cashier = findCashierForUpdate(cashierId);
        CashierEntry entry = findEntry(transaction.getId(), cashierId);
        cashierMovementValidationService.validateMovementAmount(entry.getAmount(), transaction.getAmount());

        if (entry.getStatus() == FinancialTransactionStatus.SETTLED) {
            return entry;
        }
        cashierMovementValidationService.validateWaitingStatus(entry.getStatus());

        cashier.settleWaitingEntry(entry);
        entry.settle(transaction.getSettlementDate());
        cashierPersistencePort.save(cashier);
        return cashierEntryPersistencePort.save(entry);
    }

    @Transactional
    CashierEntry scheduleDeposit(Long cashierId, FinancialTransaction transaction) {
        BigDecimal amount = transaction.getAmount();
        cashierMovementValidationService.validatePositiveAmount(amount);
        Cashier cashier = findCashierForUpdate(cashierId);
        Optional<CashierEntry> existingEntryOptional =
                cashierEntryPersistencePort.findBySourceTransactionIdAndCashierId(
                        transaction.getId(),
                        cashierId
                );
        if (existingEntryOptional.isPresent()) {
            CashierEntry existingEntry = existingEntryOptional.get();
            cashierMovementValidationService.validateMovementAmount(existingEntry.getAmount(), amount);
            return existingEntry;
        }

        CashierEntry entry = new CashierEntry(
                cashier,
                "Entrada da transacao financeira #" + transaction.getId(),
                amount,
                transaction.getDueDate(),
                FINANCIAL_TRANSACTION_SOURCE,
                FinancialTransactionStatus.WAITING,
                transaction
        );

        cashier.deposit(entry);
        CashierEntry savedEntry = cashierEntryPersistencePort.save(entry);
        cashierPersistencePort.save(cashier);
        return savedEntry;
    }

    @Transactional
    CashierExpense settleWithdrawal(Long cashierId, FinancialTransaction transaction) {
        BigDecimal amount = transaction.getAmount();
        cashierMovementValidationService.validatePositiveAmount(amount);
        Cashier cashier = findCashierForUpdate(cashierId);
        CashierExpense expense = findExpense(transaction.getId(), cashierId);
        cashierMovementValidationService.validateMovementAmount(expense.getAmount().abs(), amount);

        if (expense.getStatus() == FinancialTransactionStatus.SETTLED) {
            return expense;
        }
        cashierMovementValidationService.validateWaitingStatus(expense.getStatus());
        cashierMovementValidationService.validateSufficientBalance(cashier, amount);

        cashier.settleWaitingExpense(expense);
        expense.settle(transaction.getSettlementDate());
        cashierPersistencePort.save(cashier);
        return cashierExpensePersistencePort.save(expense);
    }

    @Transactional
    CashierExpense scheduleWithdrawal(Long cashierId, FinancialTransaction transaction) {
        BigDecimal amount = transaction.getAmount();
        cashierMovementValidationService.validatePositiveAmount(amount);
        Cashier cashier = findCashierForUpdate(cashierId);
        Optional<CashierExpense> existingExpenseOptional =
                cashierExpensePersistencePort.findBySourceTransactionIdAndCashierId(
                        transaction.getId(),
                        cashierId
                );
        if (existingExpenseOptional.isPresent()) {
            CashierExpense existingExpense = existingExpenseOptional.get();
            cashierMovementValidationService.validateMovementAmount(
                    existingExpense.getAmount().abs(),
                    amount
            );
            return existingExpense;
        }

        CashierExpense expense = new CashierExpense(
                cashier,
                "Saida da transacao financeira #" + transaction.getId(),
                amount.negate(),
                transaction.getDueDate(),
                FINANCIAL_TRANSACTION_SOURCE,
                FinancialTransactionStatus.WAITING,
                transaction
        );

        cashier.withdraw(expense);
        CashierExpense savedExpense = cashierExpensePersistencePort.save(expense);
        cashierPersistencePort.save(cashier);
        return savedExpense;
    }

    @Transactional
    void reverseMovementsForTransaction(Long transactionId) {
        List<CashierExpense> initialCashierExpenseList =
                cashierExpensePersistencePort.findBySourceTransactionId(transactionId);
        List<CashierEntry> initialCashierEntryList =
                cashierEntryPersistencePort.findBySourceTransactionId(transactionId);
        List<Long> cashierIdList = Stream.concat(
                        initialCashierExpenseList.stream().map(expense -> expense.getCashier().getId()),
                        initialCashierEntryList.stream().map(entry -> entry.getCashier().getId())
                )
                .distinct()
                .sorted()
                .toList();
        Map<Long, Cashier> cashierMap = new LinkedHashMap<>();
        cashierIdList.forEach(cashierId -> cashierMap.put(cashierId, findCashierForUpdate(cashierId)));

        List<CashierExpense> cashierExpenseList =
                cashierExpensePersistencePort.findBySourceTransactionId(transactionId);
        List<CashierEntry> cashierEntryList =
                cashierEntryPersistencePort.findBySourceTransactionId(transactionId);
        cashierExpenseList.forEach(expense -> cashierMap.get(expense.getCashier().getId()).removeExpense(expense));
        cashierEntryList.forEach(entry -> cashierMap.get(entry.getCashier().getId()).removeEntry(entry));
        cashierMap.values().forEach(cashierPersistencePort::save);
        cashierExpensePersistencePort.deleteAll(cashierExpenseList);
        cashierEntryPersistencePort.deleteAll(cashierEntryList);
    }

    private CashierEntry findEntry(Long transactionId, Long cashierId) {
        return cashierEntryPersistencePort.findBySourceTransactionIdAndCashierId(transactionId, cashierId)
                .orElseThrow(() -> new FinanceException("Entrada da transacao nao encontrada para o caixa."));
    }

    private CashierExpense findExpense(Long transactionId, Long cashierId) {
        return cashierExpensePersistencePort.findBySourceTransactionIdAndCashierId(transactionId, cashierId)
                .orElseThrow(() -> new FinanceException("Saida da transacao nao encontrada para o caixa."));
    }

    private Cashier findCashierForUpdate(Long cashierId) {
        return cashierPersistencePort.findByIdForUpdate(cashierId)
                .orElseThrow(() -> new FinanceException("Caixa nao encontrado."));
    }

}
