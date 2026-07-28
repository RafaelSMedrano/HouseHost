package com.househost.finance.financialtransaction.adapter.out.persistence.entity;

import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import com.househost.finance.financialtransaction.domain.model.InstallmentTransactionStatus;

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
public class InstallmentTransactionJpaEntity extends FinancialTransactionJpaEntity {

    @ManyToOne
    @JoinColumn(name = "installment_plan_id", nullable = false)
    private InstallmentPlanTransactionJpaEntity installmentPlan;

    @Column(nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false)
    private Integer totalInstallments;

    @Column(name = "due_date", nullable = false)
    private LocalDate installmentDueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstallmentTransactionStatus installmentStatus;

    public InstallmentTransactionJpaEntity() {
    }

    public InstallmentTransactionJpaEntity(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method, InstallmentPlanTransactionJpaEntity installmentPlan, Integer installmentNumber, Integer totalInstallments, LocalDate dueDate, InstallmentTransactionStatus installmentStatus) {
        super(senderType, senderId, receiverType, receiverId,  type, amount, transactionDate, description, method);
        this.installmentPlan = installmentPlan;
        this.installmentNumber = installmentNumber;
        this.totalInstallments = totalInstallments;
        setDueDate(dueDate);
        this.installmentDueDate = dueDate;
        this.installmentStatus = installmentStatus;
    }

    public InstallmentPlanTransactionJpaEntity getInstallmentPlan() {
        return installmentPlan;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public Integer getTotalInstallments() {
        return totalInstallments;
    }

    @Override
    public LocalDate getDueDate() {
        return installmentDueDate == null ? super.getDueDate() : installmentDueDate;
    }

    public InstallmentTransactionStatus getInstallmentStatus() {
        return installmentStatus;
    }

    public void setInstallmentStatus(InstallmentTransactionStatus installmentStatus) {
        this.installmentStatus = installmentStatus;
    }
}
