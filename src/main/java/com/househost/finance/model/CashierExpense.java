package com.househost.finance.model;

import com.househost.guest.model.Guest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cashier_expenses")
public class CashierExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cashier_id", nullable = false)
    private Cashier cashier;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    private Guest guest;

    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate expenseDate;

    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialTransactionStatus status;

    @ManyToOne
    @JoinColumn(name = "source_transaction_id")
    private FinancialTransaction sourceTransaction;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public CashierExpense() {
    }

    public CashierExpense(Cashier cashier, String description, BigDecimal amount, LocalDate expenseDate, String category, FinancialTransactionStatus status) {
        this(cashier, null, description, amount, expenseDate, category, status, null);
    }

    public CashierExpense(Cashier cashier, Guest guest, String description, BigDecimal amount, LocalDate expenseDate, String category, FinancialTransactionStatus status) {
        this(cashier, guest, description, amount, expenseDate, category, status, null);
    }

    public CashierExpense(Cashier cashier, Guest guest, String description, BigDecimal amount, LocalDate expenseDate, String category, FinancialTransactionStatus status, FinancialTransaction sourceTransaction) {
        this.cashier = cashier;
        this.guest = guest;
        this.description = description;
        this.amount = normalizeExpenseAmount(amount);
        this.expenseDate = expenseDate;
        this.category = category;
        this.sourceTransaction = sourceTransaction;
        this.status = status == null ? FinancialTransactionStatus.WAITING : status;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
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

    public Long getId() {
        return id;
    }

    public Cashier getCashier() {
        return cashier;
    }

    public Guest getGuest() {
        return guest;
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
