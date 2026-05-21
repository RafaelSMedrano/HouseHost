package com.househost.finance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cashiers")
public class Cashier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal openingBalance;

    @Column(nullable = false)
    private BigDecimal cashOnHand;

    @Column(nullable = false)
    private BigDecimal onWaiting = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal expectedInflow;

    @Column(nullable = false)
    private BigDecimal expectedOutflow;

    @Column(nullable = false)
    private BigDecimal totalInflow;

    @Column(nullable = false)
    private BigDecimal totalOutflow;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashierStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "cashier")
    private List<CashierEntry> entries = new ArrayList<>();

    @OneToMany(mappedBy = "cashier")
    private List<CashierExpense> expenses = new ArrayList<>();

    public Cashier() {
    }

    public static Cashier reference(Long id) {
        Cashier cashier = new Cashier();
        cashier.id = id;
        return cashier;
    }

    public Cashier(String name, String description, BigDecimal openingBalance, BigDecimal cashOnHand, BigDecimal expectedInflow, BigDecimal expectedOutflow, BigDecimal totalInflow, BigDecimal totalOutflow, CashierStatus status) {
        this.name = name;
        this.description = description;
        this.openingBalance = openingBalance;
        this.cashOnHand = cashOnHand;
        this.onWaiting = BigDecimal.ZERO;
        this.expectedInflow = expectedInflow;
        this.expectedOutflow = expectedOutflow;
        this.totalInflow = totalInflow;
        this.totalOutflow = totalOutflow;
        this.status = status;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        normalizeBalances();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        normalizeBalances();
    }

    public void updateProfile(String name, String description, BigDecimal openingBalance, BigDecimal cashOnHand, BigDecimal expectedInflow, BigDecimal expectedOutflow, BigDecimal totalInflow, BigDecimal totalOutflow, CashierStatus status) {
        this.name = name;
        this.description = description;
        this.openingBalance = openingBalance;
        this.cashOnHand = cashOnHand;
        this.expectedInflow = expectedInflow;
        this.expectedOutflow = expectedOutflow;
        this.totalInflow = totalInflow;
        this.totalOutflow = totalOutflow;
        this.status = status;
    }

    public void deposit(CashierEntry entry) {
        entries.add(entry);
        if (entry.getStatus() == FinancialTransactionStatus.WAITING) {
            onWaiting = onWaiting.add(entry.getAmount());
            return;
        }

        settleEntry(entry);
    }

    public void withdraw(CashierExpense expense) {
        BigDecimal withdrawnAmount = expense.getAmount().abs();
        if (expense.getStatus() == FinancialTransactionStatus.WAITING) {
            onWaiting = onWaiting.subtract(withdrawnAmount);
            expenses.add(expense);
            return;
        }

        expenses.add(expense);
        settleExpense(expense);
    }

    public void settleEntry(CashierEntry entry) {
        cashOnHand = cashOnHand.add(entry.getAmount());
        totalInflow = totalInflow.add(entry.getAmount());
    }

    public void settleWaitingEntry(CashierEntry entry) {
        onWaiting = onWaiting.subtract(entry.getAmount());
        settleEntry(entry);
    }

    public void settleExpense(CashierExpense expense) {
        BigDecimal withdrawnAmount = expense.getAmount().abs();
        cashOnHand = cashOnHand.subtract(withdrawnAmount);
        totalOutflow = totalOutflow.add(withdrawnAmount);
    }

    public void settleWaitingExpense(CashierExpense expense) {
        onWaiting = onWaiting.add(expense.getAmount().abs());
        settleExpense(expense);
    }

    public void removeEntry(CashierEntry entry) {
        entries.remove(entry);
        if (entry.getStatus() == FinancialTransactionStatus.WAITING) {
            onWaiting = onWaiting.subtract(entry.getAmount());
            return;
        }

        cashOnHand = cashOnHand.subtract(entry.getAmount());
        totalInflow = totalInflow.subtract(entry.getAmount());
    }

    public void removeExpense(CashierExpense expense) {
        expenses.remove(expense);
        BigDecimal withdrawnAmount = expense.getAmount().abs();
        if (expense.getStatus() == FinancialTransactionStatus.WAITING) {
            onWaiting = onWaiting.add(withdrawnAmount);
            return;
        }

        cashOnHand = cashOnHand.add(withdrawnAmount);
        totalOutflow = totalOutflow.subtract(withdrawnAmount);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public BigDecimal getCashOnHand() {
        return cashOnHand;
    }

    public BigDecimal getOnWaiting() {
        return onWaiting;
    }

    public BigDecimal getExpectedInflow() {
        return expectedInflow;
    }

    public BigDecimal getExpectedOutflow() {
        return expectedOutflow;
    }

    public BigDecimal getTotalInflow() {
        return totalInflow;
    }

    public BigDecimal getTotalOutflow() {
        return totalOutflow;
    }

    public CashierStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<CashierEntry> getEntries() {
        return entries;
    }

    public List<CashierExpense> getExpenses() {
        return expenses;
    }

    private void normalizeBalances() {
        if (openingBalance == null) {
            openingBalance = BigDecimal.ZERO;
        }
        if (cashOnHand == null) {
            cashOnHand = BigDecimal.ZERO;
        }
        if (onWaiting == null) {
            onWaiting = BigDecimal.ZERO;
        }
        if (expectedInflow == null) {
            expectedInflow = BigDecimal.ZERO;
        }
        if (expectedOutflow == null) {
            expectedOutflow = BigDecimal.ZERO;
        }
        if (totalInflow == null) {
            totalInflow = BigDecimal.ZERO;
        }
        if (totalOutflow == null) {
            totalOutflow = BigDecimal.ZERO;
        }
    }
}
