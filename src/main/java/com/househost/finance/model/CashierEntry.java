package com.househost.finance.model;

import com.househost.guest.model.Guest;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cashier_entries")
public class CashierEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cashier_id", nullable = false)
    private Cashier cashier;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    private Guest guest;

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
    private FinancialTransaction sourceTransaction;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public CashierEntry() {
    }

    public CashierEntry(Cashier cashier, String description, BigDecimal amount, LocalDate entryDate, String source, FinancialTransactionStatus status) {
        this(cashier, null, description, amount, entryDate, source, status, null);
    }

    public CashierEntry(Cashier cashier, Guest guest, String description, BigDecimal amount, LocalDate entryDate, String source, FinancialTransactionStatus status) {
        this(cashier, guest, description, amount, entryDate, source, status, null);
    }

    public CashierEntry(Cashier cashier, Guest guest, String description, BigDecimal amount, LocalDate entryDate, String source, FinancialTransactionStatus status, FinancialTransaction sourceTransaction) {
        this.cashier = cashier;
        this.guest = guest;
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

    public void updateEntry(Cashier cashier, String description, BigDecimal amount, LocalDate entryDate, String source, FinancialTransactionStatus status) {
        updateEntry(cashier, description, amount, entryDate, source, status, sourceTransaction);
    }

    public void updateEntry(Cashier cashier, String description, BigDecimal amount, LocalDate entryDate, String source, FinancialTransactionStatus status, FinancialTransaction sourceTransaction) {
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

    public Cashier getCashier() {
        return cashier;
    }

    public Guest getGuest() {
        return guest;
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

    public FinancialTransaction getSourceTransaction() {
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
