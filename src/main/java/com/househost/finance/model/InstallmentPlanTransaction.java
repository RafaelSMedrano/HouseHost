package com.househost.finance.model;

import com.househost.guest.model.Guest;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "installment_plan_transactions")
@PrimaryKeyJoinColumn(name = "financial_transaction_id")
public class InstallmentPlanTransaction extends FinancialTransaction {

    @Column(nullable = false)
    private Integer installmentsQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstallmentPlanStatus installmentPlanStatus;

    @OneToMany(mappedBy = "installmentPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InstallmentTransaction> installments = new ArrayList<>();

    public InstallmentPlanTransaction() {
    }

    public InstallmentPlanTransaction(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, Guest guest, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method, Integer installmentsQuantity, InstallmentPlanStatus installmentPlanStatus) {
        this(senderType, senderId, receiverType, receiverId, guest, type, amount, transactionDate, description, method, installmentsQuantity, installmentPlanStatus, FinancialTransactionStatus.WAITING);
    }

    public InstallmentPlanTransaction(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, Guest guest, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method, Integer installmentsQuantity, InstallmentPlanStatus installmentPlanStatus, FinancialTransactionStatus financialTransactionStatus) {
        super(senderType, senderId, receiverType, receiverId, guest, type, amount, transactionDate, description, method);
        this.installmentsQuantity = installmentsQuantity;
        this.installmentPlanStatus = installmentPlanStatus;
        setStatus(financialTransactionStatus);
        createInstallments(senderType, senderId, receiverType, receiverId, guest, type, amount, transactionDate, description, method, resolveInstallmentStatus(financialTransactionStatus));
    }

    private void createInstallments(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, Guest guest, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method, InstallmentTransactionStatus installmentStatus) {
        BigDecimal installmentAmount = amount.divide(BigDecimal.valueOf(installmentsQuantity), 2, RoundingMode.DOWN);
        BigDecimal distributedAmount = BigDecimal.ZERO;

        for (int index = 1; index <= installmentsQuantity; index++) {
            BigDecimal currentAmount = installmentAmount;
            if (index == installmentsQuantity) {
                currentAmount = amount.subtract(distributedAmount);
            }

            distributedAmount = distributedAmount.add(currentAmount);
            installments.add(new InstallmentTransaction(
                    senderType,
                    senderId,
                    receiverType,
                    receiverId,
                    guest,
                    type,
                    currentAmount,
                    transactionDate.plusMonths(index - 1L),
                    description + " - parcela " + index + "/" + installmentsQuantity,
                    method,
                    this,
                    index,
                    installmentsQuantity,
                    installmentStatus
            ));
        }
    }

    private InstallmentTransactionStatus resolveInstallmentStatus(FinancialTransactionStatus financialTransactionStatus) {
        if (financialTransactionStatus == FinancialTransactionStatus.PAID || financialTransactionStatus == FinancialTransactionStatus.SETTLED) {
            return InstallmentTransactionStatus.SETTLED;
        }

        return InstallmentTransactionStatus.WAITING;
    }

    public Integer getInstallmentsQuantity() {
        return installmentsQuantity;
    }

    public InstallmentPlanStatus getInstallmentPlanStatus() {
        return installmentPlanStatus;
    }

    public void setInstallmentPlanStatus(InstallmentPlanStatus installmentPlanStatus) {
        this.installmentPlanStatus = installmentPlanStatus;
    }

    @Override
    public void setSource(FinancialTransactionSourceType sourceType, Long sourceId) {
        super.setSource(sourceType, sourceId);
        installments.forEach(installment -> installment.setSource(sourceType, sourceId));
    }

    public List<InstallmentTransaction> getInstallments() {
        return installments;
    }
}
