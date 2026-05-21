package com.househost.finance.model;

import com.househost.guest.model.Guest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_transactions")
@Inheritance(strategy = InheritanceType.JOINED)
public class FinancialTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialPartyType senderType;

    @Column(nullable = false)
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialPartyType receiverType;

    @Column(nullable = false)
    private Long receiverId;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    private Guest guest;

    @Enumerated(EnumType.STRING)
    private FinancialTransactionSourceType sourceType;

    private Long sourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialTransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal entryAmount;

    @Column(nullable = false)
    private BigDecimal expenseAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialTransactionStatus status;

    @Enumerated(EnumType.STRING)
    private FinancialTransactionMethod method;

    @Column(nullable = false)
    private LocalDate transactionDate;

    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public FinancialTransaction() {
    }

    public static FinancialTransaction reference(Long id) {
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.id = id;
        return transaction;
    }

    public FinancialTransaction(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, Guest guest, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description) {
        this(senderType, senderId, receiverType, receiverId, guest, type, amount, transactionDate, description, null);
    }

    public FinancialTransaction(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, Guest guest, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method) {
        this.senderType = senderType;
        this.senderId = senderId;
        this.receiverType = receiverType;
        this.receiverId = receiverId;
        this.type = type;
        this.amount = amount;
        applyTypeAmounts(type, amount);
        this.status = FinancialTransactionStatus.WAITING;
        this.method = method;
        this.transactionDate = transactionDate;
        this.description = description;
        setGuest(guest);
    }

    public FinancialTransaction(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, Guest guest, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method, FinancialTransactionStatus status) {
        this(senderType, senderId, receiverType, receiverId, guest, type, amount, transactionDate, description, method);
        setStatus(status);
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

    public void updateTransaction(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, Guest guest, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description) {
        updateTransaction(senderType, senderId, receiverType, receiverId, guest, type, amount, transactionDate, description, method);
    }

    public void updateTransaction(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, Guest guest, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method) {
        this.senderType = senderType;
        this.senderId = senderId;
        this.receiverType = receiverType;
        this.receiverId = receiverId;
        setGuest(guest);
        this.type = type;
        this.amount = amount;
        applyTypeAmounts(type, amount);
        this.method = method;
        this.transactionDate = transactionDate;
        this.description = description;
    }

    private void setGuest(Guest guest) {
        if (this.guest != null && this.guest != guest) {
            this.guest.removeFinancialTransaction(this);
        }

        this.guest = guest;

        if (guest != null) {
            guest.addFinancialTransaction(this);
        }
    }

    public void setStatus(FinancialTransactionStatus status) {
        this.status = status == null ? FinancialTransactionStatus.WAITING : status;
        if (guest != null) {
            guest.refreshFinancialStatus();
        }
    }

    public void setSource(FinancialTransactionSourceType sourceType, Long sourceId) {
        this.sourceType = sourceType;
        this.sourceId = sourceType == null ? null : sourceId;
    }

    private void applyTypeAmounts(FinancialTransactionType type, BigDecimal amount) {
        if (type == FinancialTransactionType.ENTRY) {
            this.entryAmount = amount;
            this.expenseAmount = BigDecimal.ZERO;
            return;
        }

        if (type == FinancialTransactionType.TRANSFER) {
            this.entryAmount = amount;
            this.expenseAmount = amount;
            return;
        }

        this.entryAmount = BigDecimal.ZERO;
        this.expenseAmount = amount.abs();
    }

    public Long getId() {
        return id;
    }

    public Guest getGuest() {
        return guest;
    }

    public FinancialTransactionSourceType getSourceType() {
        return sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public FinancialPartyType getSenderType() {
        return senderType;
    }

    public Long getSenderId() {
        return senderId;
    }

    public FinancialPartyType getReceiverType() {
        return receiverType;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public FinancialTransactionType getType() {
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

    public FinancialTransactionStatus getStatus() {
        return status;
    }

    public FinancialTransactionMethod getMethod() {
        return method;
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
