package com.househost.finance.financialtransaction.adapter.out.persistence.entity;

import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "installment_plan_transactions")
@PrimaryKeyJoinColumn(name = "financial_transaction_id")
public class InstallmentPlanTransactionJpaEntity extends FinancialTransactionJpaEntity {

    @Column(nullable = false)
    private Integer installmentsQuantity;

    @Column(nullable = false)
    private Integer installmentDueDay;

    @OneToMany(mappedBy = "installmentPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InstallmentTransactionJpaEntity> installmentTransactionJpaEntityList = new ArrayList<>();

    public InstallmentPlanTransactionJpaEntity() {
    }

    public InstallmentPlanTransactionJpaEntity(
            FinancialPartyType senderType,
            Long senderId,
            FinancialPartyType receiverType,
            Long receiverId,
            BigDecimal amount,
            LocalDate transactionDate,
            String description,
            FinancialTransactionMethod method,
            Integer installmentsQuantity,
            Integer installmentDueDay
    ) {
        this(
                senderType,
                senderId,
                receiverType,
                receiverId,
                amount,
                transactionDate,
                description,
                method,
                installmentsQuantity,
                installmentDueDay,
                FinancialTransactionType.INSTALLMENT_PLAN_BLOCK,
                FinancialTransactionStatus.WAITING
        );
    }

    public InstallmentPlanTransactionJpaEntity(
            FinancialPartyType senderType,
            Long senderId,
            FinancialPartyType receiverType,
            Long receiverId,
            BigDecimal amount,
            LocalDate transactionDate,
            String description,
            FinancialTransactionMethod method,
            Integer installmentsQuantity,
            Integer installmentDueDay,
            FinancialTransactionStatus financialTransactionStatus
    ) {
        this(
                senderType,
                senderId,
                receiverType,
                receiverId,
                amount,
                transactionDate,
                description,
                method,
                installmentsQuantity,
                installmentDueDay,
                FinancialTransactionType.INSTALLMENT_PLAN_BLOCK,
                financialTransactionStatus
        );
    }

    public InstallmentPlanTransactionJpaEntity(
            FinancialPartyType senderType,
            Long senderId,
            FinancialPartyType receiverType,
            Long receiverId,
            BigDecimal amount,
            LocalDate transactionDate,
            String description,
            FinancialTransactionMethod method,
            Integer installmentsQuantity,
            Integer installmentDueDay,
            FinancialTransactionType type,
            FinancialTransactionStatus financialTransactionStatus
    ) {
        super(
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
        this.installmentsQuantity = installmentsQuantity;
        this.installmentDueDay = installmentDueDay;
        setStatus(financialTransactionStatus);
    }

    public Integer getInstallmentsQuantity() {
        return installmentsQuantity;
    }

    public Integer getInstallmentDueDay() {
        return installmentDueDay;
    }

    public List<InstallmentTransactionJpaEntity> getInstallments() {
        return installmentTransactionJpaEntityList;
    }

    public void replaceInstallments(
            List<InstallmentTransactionJpaEntity> installmentTransactionJpaEntityList
    ) {
        this.installmentTransactionJpaEntityList.clear();
        this.installmentTransactionJpaEntityList.addAll(installmentTransactionJpaEntityList);
        this.installmentTransactionJpaEntityList.forEach(
                InstallmentTransactionJpaEntity::synchronizeSourceWithPlan
        );
    }
}
