package com.househost.finance.financialtransaction.adapter.out.persistence.entity;

import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_transactions")
@Inheritance(strategy = InheritanceType.JOINED)
public class FinancialTransactionJpaEntity {

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

    private LocalDate dueDate;

    private LocalDate creationDate;

    private LocalDate settlementDate;

    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public FinancialTransactionJpaEntity() {
    }

    public static FinancialTransactionJpaEntity reference(Long id) {
        FinancialTransactionJpaEntity transaction = new FinancialTransactionJpaEntity();
        transaction.id = id;
        return transaction;
    }

    public FinancialTransactionJpaEntity(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description) {
        this(senderType, senderId, receiverType, receiverId, type, amount, transactionDate, description, null);
    }

    public FinancialTransactionJpaEntity(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method) {
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
    }

    public FinancialTransactionJpaEntity(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method, FinancialTransactionStatus status) {
        this(senderType, senderId, receiverType, receiverId, type, amount, transactionDate, description, method);
        setStatus(status);
    }

    public FinancialTransactionJpaEntity(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, LocalDate dueDate, String description, FinancialTransactionMethod method, FinancialTransactionStatus status) {
        this(senderType, senderId, receiverType, receiverId, type, amount, transactionDate, description, method, status);
        this.dueDate = dueDate == null ? transactionDate : dueDate;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        creationDate = now.toLocalDate();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateTransaction(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description) {
        updateTransaction(senderType, senderId, receiverType, receiverId, type, amount, transactionDate, description, method);
    }

    public void updateTransaction(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method) {
        this.senderType = senderType;
        this.senderId = senderId;
        this.receiverType = receiverType;
        this.receiverId = receiverId;
        this.type = type;
        this.amount = amount;
        applyTypeAmounts(type, amount);
        this.method = method;
        this.transactionDate = transactionDate;
        this.description = description;
    }

    public void setStatus(FinancialTransactionStatus status) {
        this.status = status == null ? FinancialTransactionStatus.WAITING : status;
        if (this.status == FinancialTransactionStatus.SETTLED && settlementDate == null) {
            settlementDate = LocalDate.now();
        }
    }

    public void setSource(FinancialTransactionSourceType sourceType, Long sourceId) {
        this.sourceType = sourceType;
        this.sourceId = sourceType == null ? null : sourceId;
    }

    public void restorePersistenceState(Long id, LocalDate creationDate, LocalDate settlementDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.creationDate = creationDate;
        this.settlementDate = settlementDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public LocalDate getDueDate() {
        return dueDate == null ? transactionDate : dueDate;
    }

    public LocalDate getCreationDate() {
        return creationDate == null && createdAt != null ? createdAt.toLocalDate() : creationDate;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    protected void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
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
