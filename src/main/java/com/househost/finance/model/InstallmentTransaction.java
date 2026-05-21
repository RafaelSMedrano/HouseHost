package com.househost.finance.model;

import com.househost.guest.model.Guest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "installment_transactions")
@PrimaryKeyJoinColumn(name = "financial_transaction_id")
public class InstallmentTransaction extends FinancialTransaction {

    @ManyToOne
    @JoinColumn(name = "installment_plan_id", nullable = false)
    private InstallmentPlanTransaction installmentPlan;

    @Column(nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false)
    private Integer totalInstallments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstallmentTransactionStatus installmentStatus;

    public InstallmentTransaction() {
    }

    public InstallmentTransaction(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, Guest guest, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method, InstallmentPlanTransaction installmentPlan, Integer installmentNumber, Integer totalInstallments, InstallmentTransactionStatus installmentStatus) {
        super(senderType, senderId, receiverType, receiverId, guest, type, amount, transactionDate, description, method);
        this.installmentPlan = installmentPlan;
        this.installmentNumber = installmentNumber;
        this.totalInstallments = totalInstallments;
        this.installmentStatus = installmentStatus;
    }

    public InstallmentPlanTransaction getInstallmentPlan() {
        return installmentPlan;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public Integer getTotalInstallments() {
        return totalInstallments;
    }

    public InstallmentTransactionStatus getInstallmentStatus() {
        return installmentStatus;
    }

    public void setInstallmentStatus(InstallmentTransactionStatus installmentStatus) {
        this.installmentStatus = installmentStatus;
    }
}
