package com.househost.finance.cashier.adapter.out.persistence.entity;

import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.finance.financialtransaction.adapter.out.persistence.entity.FinancialTransactionJpaEntity;
import com.househost.finance.cashier.domain.model.CashierExpense;

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
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "cashier_expenses",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cashier_expense_transaction",
                columnNames = {"cashier_id", "source_transaction_id"}
        )
)
public class CashierExpenseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cashier_id", nullable = false)
    private CashierJpaEntity cashier;

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
    private FinancialTransactionJpaEntity sourceTransaction;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public CashierExpenseJpaEntity() {
    }

    public static CashierExpenseJpaEntity fromDomain(CashierExpense expense) {
        CashierExpenseJpaEntity entity = new CashierExpenseJpaEntity();
        entity.id = expense.getId();
        entity.cashier = CashierJpaEntity.reference(expense.getCashier().getId());
        entity.description = expense.getDescription();
        entity.amount = expense.getAmount();
        entity.expenseDate = expense.getExpenseDate();
        entity.category = expense.getCategory();
        entity.status = expense.getStatus();
        entity.sourceTransaction = expense.getSourceTransaction() == null
                ? null
                : FinancialTransactionJpaEntity.reference(expense.getSourceTransaction().getId());
        entity.createdAt = expense.getCreatedAt();
        entity.updatedAt = expense.getUpdatedAt();
        return entity;
    }

    public CashierExpenseJpaEntity(CashierJpaEntity cashier, String description, BigDecimal amount, LocalDate expenseDate, String category, FinancialTransactionStatus status) {
        this(cashier, description, amount, expenseDate, category, status, null);
    }

    public CashierExpenseJpaEntity(CashierJpaEntity cashier, String description, BigDecimal amount, LocalDate expenseDate, String category, FinancialTransactionStatus status, FinancialTransactionJpaEntity sourceTransaction) {
        this.cashier = cashier;
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

    public void updateExpense(CashierJpaEntity cashier, String description, BigDecimal amount, LocalDate expenseDate, String category, FinancialTransactionStatus status) {
        updateExpense(cashier, description, amount, expenseDate, category, status, sourceTransaction);
    }

    public void updateExpense(CashierJpaEntity cashier, String description, BigDecimal amount, LocalDate expenseDate, String category, FinancialTransactionStatus status, FinancialTransactionJpaEntity sourceTransaction) {
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

    public CashierJpaEntity getCashier() {
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

    public FinancialTransactionJpaEntity getSourceTransaction() {
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
