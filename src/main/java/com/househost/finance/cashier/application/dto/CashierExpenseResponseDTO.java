package com.househost.finance.cashier.application.dto;

import com.househost.finance.cashier.domain.model.CashierExpense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CashierExpenseResponseDTO {

    private Long id;
    private Long cashierId;
    private String cashierName;
    private Long sourceTransactionId;
    private String sourceTransactionClass;
    private String description;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate settlementDate;
    private LocalDate expenseDate;
    private String category;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CashierExpenseResponseDTO(CashierExpense expense) {
        this.id = expense.getId();
        this.cashierId = expense.getCashier().getId();
        this.cashierName = expense.getCashier().getName();
        this.sourceTransactionId = expense.getSourceTransaction() == null ? null : expense.getSourceTransaction().getId();
        this.sourceTransactionClass = expense.getSourceTransaction() == null ? null : expense.getSourceTransaction().getClass().getSimpleName();
        this.description = expense.getDescription();
        this.amount = expense.getAmount();
        this.dueDate = expense.getDueDate();
        this.settlementDate = expense.getSettlementDate();
        this.expenseDate = expense.getDueDate();
        this.category = expense.getCategory();
        this.status = expense.getStatus().name();
        this.createdAt = expense.getCreatedAt();
        this.updatedAt = expense.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getCashierId() {
        return cashierId;
    }

    public String getCashierName() {
        return cashierName;
    }

    public Long getSourceTransactionId() {
        return sourceTransactionId;
    }

    public String getSourceTransactionClass() {
        return sourceTransactionClass;
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public String getCategory() {
        return category;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
