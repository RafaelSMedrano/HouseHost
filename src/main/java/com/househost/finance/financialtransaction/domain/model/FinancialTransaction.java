package com.househost.finance.financialtransaction.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FinancialTransaction {

    private Long id;

    private FinancialPartyType senderType;

    private Long senderId;

    private FinancialPartyType receiverType;

    private Long receiverId;

    private FinancialTransactionSourceType sourceType;

    private Long sourceId;

    private Integer planComponentOrder;

    private FinancialTransactionType type;

    private BigDecimal amount;

    private FinancialTransactionStatus status;

    private FinancialTransactionMethod method;

    private LocalDate transactionDate;

    private LocalDate dueDate;

    private LocalDate creationDate;

    private LocalDate settlementDate;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public FinancialTransaction() {
    }

    public static FinancialTransaction reference(Long id) {
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.id = id;
        return transaction;
    }

    public FinancialTransaction(
            FinancialPartyType senderType,
            Long senderId,
            FinancialPartyType receiverType,
            Long receiverId,
            FinancialTransactionType type,
            BigDecimal amount,
            LocalDate transactionDate,
            String description
    ) {
        this(
                senderType,
                senderId,
                receiverType,
                receiverId,
                type,
                amount,
                transactionDate,
                description,
                null
        );
    }

    public FinancialTransaction(
            FinancialPartyType senderType,
            Long senderId,
            FinancialPartyType receiverType,
            Long receiverId,
            FinancialTransactionType type,
            BigDecimal amount,
            LocalDate transactionDate,
            String description,
            FinancialTransactionMethod method
    ) {
        this.senderType = senderType;
        this.senderId = senderId;
        this.receiverType = receiverType;
        this.receiverId = receiverId;
        this.type = type;
        this.amount = amount;
        this.status = FinancialTransactionStatus.WAITING;
        this.method = method;
        this.transactionDate = transactionDate;
        this.description = description;
    }

    public FinancialTransaction(
            FinancialPartyType senderType,
            Long senderId,
            FinancialPartyType receiverType,
            Long receiverId,
            FinancialTransactionType type,
            BigDecimal amount,
            LocalDate transactionDate,
            String description,
            FinancialTransactionMethod method,
            FinancialTransactionStatus status
    ) {
        this(
                senderType,
                senderId,
                receiverType,
                receiverId,
                type,
                amount,
                transactionDate,
                description,
                method
        );
        setStatus(status);
    }

    public FinancialTransaction(
            FinancialPartyType senderType,
            Long senderId,
            FinancialPartyType receiverType,
            Long receiverId,
            FinancialTransactionType type,
            BigDecimal amount,
            LocalDate transactionDate,
            LocalDate dueDate,
            String description,
            FinancialTransactionMethod method,
            FinancialTransactionStatus status
    ) {
        this(
                senderType,
                senderId,
                receiverType,
                receiverId,
                type,
                amount,
                transactionDate,
                description,
                method,
                status
        );
        this.dueDate = dueDate == null ? transactionDate : dueDate;
    }

    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        creationDate = now.toLocalDate();
        createdAt = now;
        updatedAt = now;
    }

    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateTransaction(
            FinancialPartyType senderType,
            Long senderId,
            FinancialPartyType receiverType,
            Long receiverId,
            BigDecimal amount,
            LocalDate transactionDate,
            String description
    ) {
        updateTransaction(
                senderType,
                senderId,
                receiverType,
                receiverId,
                amount,
                transactionDate,
                description,
                method
        );
    }

    public void updateTransaction(
            FinancialPartyType senderType,
            Long senderId,
            FinancialPartyType receiverType,
            Long receiverId,
            BigDecimal amount,
            LocalDate transactionDate,
            String description,
            FinancialTransactionMethod method
    ) {
        this.senderType = senderType;
        this.senderId = senderId;
        this.receiverType = receiverType;
        this.receiverId = receiverId;
        this.amount = amount;
        this.method = method;
        this.transactionDate = transactionDate;
        this.description = description;
    }

    public void updateDetails(LocalDate transactionDate, String description, FinancialTransactionMethod method) {
        this.transactionDate = transactionDate;
        this.description = description;
        this.method = method;
    }

    public void updateDetails(
            LocalDate transactionDate,
            LocalDate dueDate,
            String description,
            FinancialTransactionMethod method
    ) {
        updateDetails(transactionDate, description, method);
        this.dueDate = dueDate == null ? transactionDate : dueDate;
    }

    protected void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setStatus(FinancialTransactionStatus status) {
        this.status = status == null ? FinancialTransactionStatus.WAITING : status;
        if (this.status == FinancialTransactionStatus.SETTLED && settlementDate == null) {
            settlementDate = LocalDate.now();
        }
    }

    public void settle(LocalDate effectiveSettlementDate) {
        if (effectiveSettlementDate == null) {
            throw new IllegalArgumentException("Data efetiva de liquidacao e obrigatoria.");
        }
        if (effectiveSettlementDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Liquidacao nao pode usar uma data futura.");
        }
        status = FinancialTransactionStatus.SETTLED;
        settlementDate = effectiveSettlementDate;
    }

    public void cancel() {
        if (status == FinancialTransactionStatus.SETTLED || settlementDate != null) {
            throw new IllegalStateException("Transacao liquidada nao pode ser cancelada.");
        }
        status = FinancialTransactionStatus.CANCELED;
    }

    public void setSource(FinancialTransactionSourceType sourceType, Long sourceId) {
        this.sourceType = sourceType;
        this.sourceId = sourceType == null ? null : sourceId;
        if (sourceType != FinancialTransactionSourceType.PLAN) {
            planComponentOrder = null;
        }
    }

    public void assignPlanMembership(Long planId, Integer planComponentOrder) {
        if (planId == null) {
            throw new IllegalArgumentException("Identificador do plano financeiro e obrigatorio.");
        }
        if (planComponentOrder == null || planComponentOrder < 1) {
            throw new IllegalArgumentException("Ordem do componente do plano deve iniciar em um.");
        }

        setSource(FinancialTransactionSourceType.PLAN, planId);
        this.planComponentOrder = planComponentOrder;
    }

    public void restorePlanComponentOrder(Integer planComponentOrder) {
        if (planComponentOrder != null && planComponentOrder < 1) {
            throw new IllegalArgumentException("Ordem persistida do componente do plano e invalida.");
        }
        this.planComponentOrder = planComponentOrder;
    }

    public void restorePersistenceState(
            Long id,
            LocalDate creationDate,
            LocalDate settlementDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.creationDate = creationDate;
        this.settlementDate = settlementDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void restorePersistenceState(Long id, LocalDateTime createdAt, LocalDateTime updatedAt) {
        restorePersistenceState(id, null, null, createdAt, updatedAt);
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

    public Integer getPlanComponentOrder() {
        return planComponentOrder;
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
