package com.househost.finance.cashier.adapter.out.persistence.entity;

import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.finance.financialtransaction.adapter.out.persistence.entity.FinancialTransactionJpaEntity;
import com.househost.finance.cashier.domain.model.CashierEntry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "cashier_entries",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cashier_entry_transaction",
                columnNames = {"cashier_id", "source_transaction_id"}
        )
)
public class CashierEntryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cashier_id", nullable = false)
    private CashierJpaEntity cashier;

    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate entryDate;

    private String source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialTransactionStatus status;

    @ManyToOne
    @JoinColumn(name = "source_transaction_id")
    private FinancialTransactionJpaEntity sourceTransaction;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public CashierEntryJpaEntity() {
    }

    public static CashierEntryJpaEntity fromDomain(CashierEntry entry) {
        CashierEntryJpaEntity entity = new CashierEntryJpaEntity();
        entity.id = entry.getId();
        entity.cashier = CashierJpaEntity.reference(entry.getCashier().getId());
        entity.description = entry.getDescription();
        entity.amount = entry.getAmount();
        entity.entryDate = entry.getEntryDate();
        entity.source = entry.getSource();
        entity.status = entry.getStatus();
        entity.sourceTransaction = entry.getSourceTransaction() == null
                ? null
                : FinancialTransactionJpaEntity.reference(entry.getSourceTransaction().getId());
        entity.createdAt = entry.getCreatedAt();
        entity.updatedAt = entry.getUpdatedAt();
        return entity;
    }

    public CashierEntryJpaEntity(CashierJpaEntity cashier, String description, BigDecimal amount, LocalDate entryDate, String source, FinancialTransactionStatus status) {
        this(cashier, description, amount, entryDate, source, status, null);
    }

    public CashierEntryJpaEntity(CashierJpaEntity cashier, String description, BigDecimal amount, LocalDate entryDate, String source, FinancialTransactionStatus status, FinancialTransactionJpaEntity sourceTransaction) {
        this.cashier = cashier;
        this.description = description;
        this.amount = normalizeEntryAmount(amount);
        this.entryDate = entryDate;
        this.source = source;
        this.sourceTransaction = sourceTransaction;
        this.status = status == null ? FinancialTransactionStatus.WAITING : status;
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

    public void updateEntry(CashierJpaEntity cashier, String description, BigDecimal amount, LocalDate entryDate, String source, FinancialTransactionStatus status) {
        updateEntry(cashier, description, amount, entryDate, source, status, sourceTransaction);
    }

    public void updateEntry(CashierJpaEntity cashier, String description, BigDecimal amount, LocalDate entryDate, String source, FinancialTransactionStatus status, FinancialTransactionJpaEntity sourceTransaction) {
        this.cashier = cashier;
        this.description = description;
        this.amount = normalizeEntryAmount(amount);
        this.entryDate = entryDate;
        this.source = source;
        this.sourceTransaction = sourceTransaction;
        this.status = status == null ? FinancialTransactionStatus.WAITING : status;
    }

    public void setStatus(FinancialTransactionStatus status) {
        this.status = status == null ? FinancialTransactionStatus.WAITING : status;
    }

    public Long getId() {
        return id;
    }

    public CashierJpaEntity getCashier() {
        return cashier;
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

    public String getSource() {
        return source;
    }

    public FinancialTransactionStatus getStatus() {
        return status;
    }

    public FinancialTransactionJpaEntity getSourceTransaction() {
        return sourceTransaction;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private static BigDecimal normalizeEntryAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da entrada deve ser maior que zero.");
        }

        return amount;
    }
}
