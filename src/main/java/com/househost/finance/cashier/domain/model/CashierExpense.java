package com.househost.finance.cashier.domain.model;

import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CashierExpense {

    private Long id;

    private Cashier cashier;

    private String description;

    private BigDecimal amount;

    private LocalDate expenseDate;

    private String category;

    private FinancialTransactionStatus status;

    private FinancialTransaction sourceTransaction;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public CashierExpense() {
    }

    public CashierExpense(Cashier cashier, String description, BigDecimal amount, LocalDate expenseDate, String category, FinancialTransactionStatus status) {
        this(cashier, description, amount, expenseDate, category, status, null);
    }

    public CashierExpense(Cashier cashier, String description, BigDecimal amount, LocalDate expenseDate, String category, FinancialTransactionStatus status, FinancialTransaction sourceTransaction) {
        this.cashier = cashier;
        this.description = description;
        this.amount = normalizeExpenseAmount(amount);
        this.expenseDate = expenseDate;
        this.category = category;
        this.sourceTransaction = sourceTransaction;
        this.status = status == null ? FinancialTransactionStatus.WAITING : status;
    }

    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateExpense(Cashier cashier, String description, BigDecimal amount, LocalDate expenseDate, String category, FinancialTransactionStatus status) {
        updateExpense(cashier, description, amount, expenseDate, category, status, sourceTransaction);
    }

    public void updateExpense(Cashier cashier, String description, BigDecimal amount, LocalDate expenseDate, String category, FinancialTransactionStatus status, FinancialTransaction sourceTransaction) {
        this.cashier = cashier;
        this.description = description;
        this.amount = normalizeExpenseAmount(amount);
        this.expenseDate = expenseDate;
        this.category = category;
        this.sourceTransaction = sourceTransaction;
        this.status = status == null ? FinancialTransactionStatus.WAITING : status;
    }

    public void setStatus(FinancialTransactionStatus status) {
        this.status = status == null ? FinancialTransactionStatus.WAITING : status;
    }

    public void restorePersistenceState(Long id, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Cashier getCashier() {
        return cashier;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public String getCategory() {
        return category;
    }

    public FinancialTransactionStatus getStatus() {
        return status;
    }

    public FinancialTransaction getSourceTransaction() {
        return sourceTransaction;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private static BigDecimal normalizeExpenseAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) >= 0) {
            throw new IllegalArgumentException("Valor da saida deve ser menor que zero.");
        }

        return amount;
    }
}
