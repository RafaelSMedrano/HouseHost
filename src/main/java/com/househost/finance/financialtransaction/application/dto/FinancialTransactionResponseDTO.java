package com.househost.finance.financialtransaction.application.dto;

import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentTransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FinancialTransactionResponseDTO {

    private Long id;
    private String senderType;
    private Long senderId;
    private String receiverType;
    private Long receiverId;
    private String sourceType;
    private Long sourceId;

    private String type;
    private BigDecimal amount;
    private String status;
    private String method;
    private String transactionClass;
    private Integer installmentsQuantity;
    private Integer installmentDueDay;
    private Integer installmentNumber;
    private Integer totalInstallments;
    private LocalDate installmentDueDate;
    private InstallmentTransactionStatus installmentStatus;
    private LocalDate transactionDate;
    private LocalDate dueDate;
    private LocalDate creationDate;
    private LocalDate settlementDate;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FinancialTransactionResponseDTO(FinancialTransaction transaction) {
        this.id = transaction.getId();
        this.senderType = transaction.getSenderType().name();
        this.senderId = transaction.getSenderId();
        this.receiverType = transaction.getReceiverType().name();
        this.receiverId = transaction.getReceiverId();
        this.sourceType = transaction.getSourceType() == null ? null : transaction.getSourceType().name();
        this.sourceId = transaction.getSourceId();
        this.type = transaction.getType().name();
        this.amount = transaction.getAmount();
        this.status = transaction.getStatus().name();
        this.method = transaction.getMethod() == null ? null : transaction.getMethod().name();
        this.transactionClass = transaction.getClass().getSimpleName();
        if (transaction instanceof InstallmentPlanTransaction installmentPlanTransaction) {
            this.installmentsQuantity = installmentPlanTransaction.getInstallmentsQuantity();
            this.installmentDueDay = installmentPlanTransaction.getInstallmentDueDay();
        }
        if (transaction instanceof InstallmentTransaction installmentTransaction) {
            this.installmentNumber = installmentTransaction.getInstallmentNumber();
            this.totalInstallments = installmentTransaction.getTotalInstallments();
            this.installmentDueDate = installmentTransaction.getDueDate();
            this.installmentStatus = installmentTransaction.getInstallmentStatus();
        }
        this.transactionDate = transaction.getTransactionDate();
        this.dueDate = transaction.getDueDate();
        this.creationDate = transaction.getCreationDate();
        this.settlementDate = transaction.getSettlementDate();
        this.description = transaction.getDescription();
        this.createdAt = transaction.getCreatedAt();
        this.updatedAt = transaction.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getSenderType() {
        return senderType;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getReceiverType() {
        return receiverType;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getMethod() {
        return method;
    }

    public String getTransactionClass() {
        return transactionClass;
    }

    public Integer getInstallmentsQuantity() {
        return installmentsQuantity;
    }

    public Integer getInstallmentDueDay() {
        return installmentDueDay;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public Integer getTotalInstallments() {
        return totalInstallments;
    }

    public LocalDate getInstallmentDueDate() {
        return installmentDueDate;
    }

    public InstallmentTransactionStatus getInstallmentStatus() {
        return installmentStatus;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
