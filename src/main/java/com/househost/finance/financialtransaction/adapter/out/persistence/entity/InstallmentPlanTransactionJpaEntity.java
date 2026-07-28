package com.househost.finance.financialtransaction.adapter.out.persistence.entity;

import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
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
    private List<InstallmentTransactionJpaEntity> installments = new ArrayList<>();

    public InstallmentPlanTransactionJpaEntity() {
    }

    public InstallmentPlanTransactionJpaEntity(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method, Integer installmentsQuantity, Integer installmentDueDay) {
        this(senderType, senderId, receiverType, receiverId,  type, amount, transactionDate, description, method, installmentsQuantity, installmentDueDay, FinancialTransactionStatus.WAITING);
    }

    public InstallmentPlanTransactionJpaEntity(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method, Integer installmentsQuantity, Integer installmentDueDay, FinancialTransactionStatus financialTransactionStatus) {
        super(senderType, senderId, receiverType, receiverId,  type, amount, transactionDate, description, method);
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

    @Override
    public void setSource(FinancialTransactionSourceType sourceType, Long sourceId) {
        super.setSource(sourceType, sourceId);
        installments.forEach(installment -> installment.setSource(sourceType, sourceId));
    }

    public List<InstallmentTransactionJpaEntity> getInstallments() {
        return installments;
    }

    public void replaceInstallments(List<InstallmentTransactionJpaEntity> installments) {
        this.installments.clear();
        this.installments.addAll(installments);
    }
}
