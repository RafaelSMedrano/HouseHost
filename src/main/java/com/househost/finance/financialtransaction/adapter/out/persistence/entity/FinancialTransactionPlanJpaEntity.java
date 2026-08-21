package com.househost.finance.financialtransaction.adapter.out.persistence.entity;

import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionPlanStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "financial_transaction_plans",
        indexes = @Index(
                name = "idx_financial_transaction_plan_source",
                columnList = "source_type,source_id"
        )
)
public class FinancialTransactionPlanJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    private FinancialPartyType senderType;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "receiver_type", nullable = false)
    private FinancialPartyType receiverType;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private FinancialTransactionSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialTransactionPlanStatus status;

    @Column(name = "plan_due_date", nullable = false)
    private LocalDate planDueDate;

    @Column(name = "plan_settlement_date")
    private LocalDate planSettlementDate;

    @Column(nullable = false, length = 500)
    private String description;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public FinancialTransactionPlanJpaEntity() {
    }

    public FinancialTransactionPlanJpaEntity(
            FinancialPartyType senderType,
            Long senderId,
            FinancialPartyType receiverType,
            Long receiverId,
            FinancialTransactionSourceType sourceType,
            Long sourceId,
            BigDecimal totalAmount,
            FinancialTransactionPlanStatus status,
            LocalDate planDueDate,
            LocalDate planSettlementDate,
            String description
    ) {
        this.senderType = senderType;
        this.senderId = senderId;
        this.receiverType = receiverType;
        this.receiverId = receiverId;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.planDueDate = planDueDate;
        this.planSettlementDate = planSettlementDate;
        this.description = description;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime persistenceTime = LocalDateTime.now();
        createdAt = createdAt == null ? persistenceTime : createdAt;
        updatedAt = updatedAt == null ? persistenceTime : updatedAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void restorePersistenceState(
            Long id,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
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

    public FinancialTransactionSourceType getSourceType() {
        return sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public FinancialTransactionPlanStatus getStatus() {
        return status;
    }

    public LocalDate getPlanDueDate() {
        return planDueDate;
    }

    public LocalDate getPlanSettlementDate() {
        return planSettlementDate;
    }

    public String getDescription() {
        return description;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
