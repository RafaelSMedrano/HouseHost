package com.househost.finance.cashier.domain.model;

import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Cashier {

    private Long id;

    private String name;

    private String description;

    private BigDecimal openingBalance;

    private BigDecimal cashOnHand;

    private BigDecimal onWaiting = BigDecimal.ZERO;

    private BigDecimal expectedInflow;

    private BigDecimal expectedOutflow;

    private BigDecimal totalInflow;

    private BigDecimal totalOutflow;

    private CashierStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

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

    public Cashier(String name, String description, BigDecimal openingBalance, CashierStatus status) {
        this(
                name,
                description,
                openingBalance,
                openingBalance,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                status
        );
    }

    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        normalizeBalances();
    }

    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        normalizeBalances();
    }

    public void update(String name, String description, CashierStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public void deposit(CashierEntry entry) {
        if (entry.getStatus() == FinancialTransactionStatus.WAITING) {
            onWaiting = onWaiting.add(entry.getAmount());
            expectedInflow = expectedInflow.add(entry.getAmount());
            return;
        }

        settleEntry(entry);
    }

    public void withdraw(CashierExpense expense) {
        BigDecimal withdrawnAmount = expense.getAmount().abs();
        if (expense.getStatus() == FinancialTransactionStatus.WAITING) {
            onWaiting = onWaiting.subtract(withdrawnAmount);
            expectedOutflow = expectedOutflow.add(withdrawnAmount);
            return;
        }

        settleExpense(expense);
    }

    public void settleEntry(CashierEntry entry) {
        cashOnHand = cashOnHand.add(entry.getAmount());
        totalInflow = totalInflow.add(entry.getAmount());
    }

    public void settleWaitingEntry(CashierEntry entry) {
        onWaiting = onWaiting.subtract(entry.getAmount());
        expectedInflow = expectedInflow.subtract(entry.getAmount());
        settleEntry(entry);
    }

    public void settleExpense(CashierExpense expense) {
        BigDecimal withdrawnAmount = expense.getAmount().abs();
        cashOnHand = cashOnHand.subtract(withdrawnAmount);
        totalOutflow = totalOutflow.add(withdrawnAmount);
    }

    public void settleWaitingExpense(CashierExpense expense) {
        BigDecimal withdrawnAmount = expense.getAmount().abs();
        onWaiting = onWaiting.add(withdrawnAmount);
        expectedOutflow = expectedOutflow.subtract(withdrawnAmount);
        settleExpense(expense);
    }

    public void removeEntry(CashierEntry entry) {
        if (entry.getStatus() == FinancialTransactionStatus.WAITING) {
            onWaiting = onWaiting.subtract(entry.getAmount());
            expectedInflow = expectedInflow.subtract(entry.getAmount());
            return;
        }

        cashOnHand = cashOnHand.subtract(entry.getAmount());
        totalInflow = totalInflow.subtract(entry.getAmount());
    }

    public void removeExpense(CashierExpense expense) {
        BigDecimal withdrawnAmount = expense.getAmount().abs();
        if (expense.getStatus() == FinancialTransactionStatus.WAITING) {
            onWaiting = onWaiting.add(withdrawnAmount);
            expectedOutflow = expectedOutflow.subtract(withdrawnAmount);
            return;
        }

        cashOnHand = cashOnHand.add(withdrawnAmount);
        totalOutflow = totalOutflow.subtract(withdrawnAmount);
    }


    public void restorePersistenceState(
            Long id,
            BigDecimal onWaiting,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.onWaiting = onWaiting == null ? BigDecimal.ZERO : onWaiting;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        normalizeBalances();
    }

    public Long getId() {
        return id;
    }


    public FinancialPartyType getFinancialPartyType() {
        return FinancialPartyType.CASHIER;
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
