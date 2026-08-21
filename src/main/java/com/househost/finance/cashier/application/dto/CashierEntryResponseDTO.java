package com.househost.finance.cashier.application.dto;

import com.househost.finance.cashier.domain.model.CashierEntry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CashierEntryResponseDTO {

    private Long id;
    private Long cashierId;
    private String cashierName;
    private Long sourceTransactionId;
    private String sourceTransactionClass;
    private String description;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate settlementDate;
    private LocalDate entryDate;
    private String source;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CashierEntryResponseDTO(CashierEntry entry) {
        this.id = entry.getId();
        this.cashierId = entry.getCashier().getId();
        this.cashierName = entry.getCashier().getName();
        this.sourceTransactionId = entry.getSourceTransaction() == null ? null : entry.getSourceTransaction().getId();
        this.sourceTransactionClass = entry.getSourceTransaction() == null ? null : entry.getSourceTransaction().getClass().getSimpleName();
        this.description = entry.getDescription();
        this.amount = entry.getAmount();
        this.dueDate = entry.getDueDate();
        this.settlementDate = entry.getSettlementDate();
        this.entryDate = entry.getDueDate();
        this.source = entry.getSource();
        this.status = entry.getStatus().name();
        this.createdAt = entry.getCreatedAt();
        this.updatedAt = entry.getUpdatedAt();
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

    public LocalDate getEntryDate() {
        return entryDate;
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
