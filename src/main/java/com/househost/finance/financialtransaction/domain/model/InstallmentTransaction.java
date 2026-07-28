package com.househost.finance.financialtransaction.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InstallmentTransaction extends FinancialTransaction {

    private InstallmentPlanTransaction installmentPlan;

    private Integer installmentNumber;

    private Integer totalInstallments;

    private InstallmentTransactionStatus installmentStatus;

    InstallmentTransaction() {
    }

    InstallmentTransaction(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method, InstallmentPlanTransaction installmentPlan, Integer installmentNumber, Integer totalInstallments, LocalDate dueDate, InstallmentTransactionStatus installmentStatus) {
        super(senderType, senderId, receiverType, receiverId, type, amount, transactionDate, description, method);
        this.installmentPlan = installmentPlan;
        this.installmentNumber = installmentNumber;
        this.totalInstallments = totalInstallments;
        setDueDate(validateDueDate(dueDate));
        this.installmentStatus = installmentStatus;
    }

    public static InstallmentTransaction restore(
            FinancialPartyType senderType,
            Long senderId,
            FinancialPartyType receiverType,
            Long receiverId,
            FinancialTransactionType type,
            BigDecimal amount,
            LocalDate transactionDate,
            String description,
            FinancialTransactionMethod method,
            InstallmentPlanTransaction installmentPlan,
            Integer installmentNumber,
            Integer totalInstallments,
            LocalDate dueDate,
            InstallmentTransactionStatus installmentStatus
    ) {
        return new InstallmentTransaction(
                senderType,
                senderId,
                receiverType,
                receiverId,
                type,
                amount,
                transactionDate,
                description,
                method,
                installmentPlan,
                installmentNumber,
                totalInstallments,
                dueDate,
                installmentStatus
        );
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

    public void settle() {
        setStatus(FinancialTransactionStatus.SETTLED);
        installmentStatus = InstallmentTransactionStatus.SETTLED;
    }

    private LocalDate validateDueDate(LocalDate dueDate) {
        if (dueDate == null) {
            throw new IllegalArgumentException("Data de vencimento da parcela e obrigatoria.");
        }
        return dueDate;
    }
}
