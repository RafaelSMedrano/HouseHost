package com.househost.finance.dto;

import com.househost.finance.model.FinancialTransaction;
import com.househost.finance.model.InstallmentPlanTransaction;
import com.househost.finance.model.InstallmentTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FinancialTransactionResponseDTO {

    private Long id;
    private String senderType;
    private Long senderId;
    private String receiverType;
    private Long receiverId;
    private Long guestId;
    private String guestName;
    private String sourceType;
    private Long sourceId;
    private Long bookingId;
    private String type;
    private BigDecimal amount;
    private BigDecimal entryAmount;
    private BigDecimal expenseAmount;
    private String status;
    private String method;
    private String transactionClass;
    private Integer installmentsQuantity;
    private String installmentPlanStatus;
    private Integer installmentNumber;
    private Integer totalInstallments;
    private String installmentStatus;
    private LocalDate transactionDate;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FinancialTransactionResponseDTO(FinancialTransaction transaction) {
        this.id = transaction.getId();
        this.senderType = transaction.getSenderType().name();
        this.senderId = transaction.getSenderId();
        this.receiverType = transaction.getReceiverType().name();
        this.receiverId = transaction.getReceiverId();
        this.guestId = transaction.getGuest() == null ? null : transaction.getGuest().getId();
        this.guestName = transaction.getGuest() == null ? null : transaction.getGuest().getFullName();
        this.sourceType = transaction.getSourceType() == null ? null : transaction.getSourceType().name();
        this.sourceId = transaction.getSourceId();
        this.bookingId = transaction.getSourceType() == null || !transaction.getSourceType().name().equals("BOOKING") ? null : transaction.getSourceId();
        this.type = transaction.getType().name();
        this.amount = transaction.getAmount();
        this.entryAmount = transaction.getEntryAmount();
        this.expenseAmount = transaction.getExpenseAmount();
        this.status = transaction.getStatus().name();
        this.method = transaction.getMethod() == null ? null : transaction.getMethod().name();
        this.transactionClass = transaction.getClass().getSimpleName();
        if (transaction instanceof InstallmentPlanTransaction installmentPlanTransaction) {
            this.installmentsQuantity = installmentPlanTransaction.getInstallmentsQuantity();
            this.installmentPlanStatus = installmentPlanTransaction.getInstallmentPlanStatus().name();
        }
        if (transaction instanceof InstallmentTransaction installmentTransaction) {
            this.installmentNumber = installmentTransaction.getInstallmentNumber();
            this.totalInstallments = installmentTransaction.getTotalInstallments();
            this.installmentStatus = installmentTransaction.getInstallmentStatus().name();
        }
        this.transactionDate = transaction.getTransactionDate();
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

    public Long getGuestId() {
        return guestId;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getEntryAmount() {
        return entryAmount;
    }

    public BigDecimal getExpenseAmount() {
        return expenseAmount;
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

    public String getInstallmentPlanStatus() {
        return installmentPlanStatus;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public Integer getTotalInstallments() {
        return totalInstallments;
    }

    public String getInstallmentStatus() {
        return installmentStatus;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
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
