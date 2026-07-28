package com.househost.finance.cashier.application.dto;

import com.househost.finance.cashier.domain.model.Cashier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CashierResponseDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal openingBalance;
    private BigDecimal cashOnHand;
    private BigDecimal onWaiting;
    private BigDecimal expectedInflow;
    private BigDecimal expectedOutflow;
    private BigDecimal totalInflow;
    private BigDecimal totalOutflow;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CashierResponseDTO(Cashier cashier) {
        this.id = cashier.getId();
        this.name = cashier.getName();
        this.description = cashier.getDescription();
        this.openingBalance = cashier.getOpeningBalance();
        this.cashOnHand = cashier.getCashOnHand();
        this.onWaiting = cashier.getOnWaiting();
        this.expectedInflow = cashier.getExpectedInflow();
        this.expectedOutflow = cashier.getExpectedOutflow();
        this.totalInflow = cashier.getTotalInflow();
        this.totalOutflow = cashier.getTotalOutflow();
        this.status = cashier.getStatus().name();
        this.createdAt = cashier.getCreatedAt();
        this.updatedAt = cashier.getUpdatedAt();
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
