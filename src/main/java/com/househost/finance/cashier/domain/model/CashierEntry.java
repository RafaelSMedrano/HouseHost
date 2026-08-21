package com.househost.finance.cashier.domain.model;

import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CashierEntry {

    private Long id;

    private Cashier cashier;

    private String description;

    private BigDecimal amount;

    private LocalDate dueDate;

    private LocalDate settlementDate;

    private String source;

    private FinancialTransactionStatus status;

    private FinancialTransaction sourceTransaction;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public CashierEntry() {
    }

    public CashierEntry(
            Cashier cashier,
            String description,
            BigDecimal amount,
            LocalDate dueDate,
            String source,
            FinancialTransactionStatus status
    ) {
        this(cashier, description, amount, dueDate, null, source, status, null);
    }

    public CashierEntry(
            Cashier cashier,
            String description,
            BigDecimal amount,
            LocalDate dueDate,
            String source,
            FinancialTransactionStatus status,
            FinancialTransaction sourceTransaction
    ) {
        this(cashier, description, amount, dueDate, null, source, status, sourceTransaction);
    }

    public CashierEntry(
            Cashier cashier,
            String description,
            BigDecimal amount,
            LocalDate dueDate,
            LocalDate settlementDate,
            String source,
            FinancialTransactionStatus status,
            FinancialTransaction sourceTransaction
    ) {
        this.cashier = cashier;
        this.description = description;
        this.amount = normalizeEntryAmount(amount);
        this.dueDate = validateDueDate(dueDate);
        this.settlementDate = settlementDate;
        this.source = source;
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

    public void updateEntry(
            Cashier cashier,
            String description,
            BigDecimal amount,
            LocalDate dueDate,
            String source,
            FinancialTransactionStatus status
    ) {
        updateEntry(cashier, description, amount, dueDate, source, status, sourceTransaction);
    }

    public void updateEntry(
            Cashier cashier,
            String description,
            BigDecimal amount,
            LocalDate dueDate,
            String source,
            FinancialTransactionStatus status,
            FinancialTransaction sourceTransaction
    ) {
        this.cashier = cashier;
        this.description = description;
        this.amount = normalizeEntryAmount(amount);
        this.dueDate = validateDueDate(dueDate);
        this.source = source;
        this.sourceTransaction = sourceTransaction;
        this.status = status == null ? FinancialTransactionStatus.WAITING : status;
    }

    public void setStatus(FinancialTransactionStatus status) {
        this.status = status == null ? FinancialTransactionStatus.WAITING : status;
    }

    public void settle(LocalDate settlementDate) {
        if (status == FinancialTransactionStatus.SETTLED) {
            return;
        }
        if (settlementDate == null) {
            throw new IllegalArgumentException("Data real da entrada e obrigatoria na liquidacao.");
        }

        status = FinancialTransactionStatus.SETTLED;
        this.settlementDate = settlementDate;
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public String getSource() {
        return source;
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

    private static BigDecimal normalizeEntryAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da entrada deve ser maior que zero.");
        }

        return amount;
    }

    private static LocalDate validateDueDate(LocalDate dueDate) {
        if (dueDate == null) {
            throw new IllegalArgumentException("Data prevista da entrada e obrigatoria.");
        }

        return dueDate;
    }
}
