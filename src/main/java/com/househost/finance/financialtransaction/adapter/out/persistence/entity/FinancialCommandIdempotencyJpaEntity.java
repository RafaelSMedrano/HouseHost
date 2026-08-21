package com.househost.finance.financialtransaction.adapter.out.persistence.entity;

import com.househost.finance.financialtransaction.application.records.FinancialCommandOperation;
import com.househost.finance.financialtransaction.application.records.FinancialCommandStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "financial_command_idempotency",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_financial_command_idempotency_scope",
                columnNames = {"operation", "actor_reference", "idempotency_key"}
        ),
        indexes = @Index(
                name = "idx_financial_command_idempotency_outcome",
                columnList = "booking_id,plan_id,financial_transaction_id"
        )
)
public class FinancialCommandIdempotencyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private FinancialCommandOperation operation;

    @Column(name = "actor_reference", nullable = false, length = 180)
    private String actorReference;

    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FinancialCommandStatus status;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "financial_transaction_id")
    private Long financialTransactionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public FinancialCommandIdempotencyJpaEntity() {
    }

    public FinancialCommandIdempotencyJpaEntity(
            FinancialCommandOperation operation,
            String actorReference,
            String idempotencyKey,
            FinancialCommandStatus status,
            Long bookingId,
            Long planId,
            Long financialTransactionId,
            LocalDateTime createdAt,
            LocalDateTime completedAt
    ) {
        this.operation = operation;
        this.actorReference = actorReference;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
        this.bookingId = bookingId;
        this.planId = planId;
        this.financialTransactionId = financialTransactionId;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    @PrePersist
    public void prePersist() {
        createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public void restoreId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public FinancialCommandOperation getOperation() {
        return operation;
    }

    public String getActorReference() {
        return actorReference;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public FinancialCommandStatus getStatus() {
        return status;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getPlanId() {
        return planId;
    }

    public Long getFinancialTransactionId() {
        return financialTransactionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
