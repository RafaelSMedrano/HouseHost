package com.househost.finance.financialtransaction.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class InstallmentPlanTransaction extends FinancialTransaction {

    private Integer installmentsQuantity;

    private Integer installmentDueDay;

    private List<InstallmentTransaction> installments = new ArrayList<>();

    public InstallmentPlanTransaction() {
    }

    public InstallmentPlanTransaction(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method, Integer installmentsQuantity, Integer installmentDueDay) {
        this(senderType, senderId, receiverType, receiverId, type, amount, transactionDate, description, method, installmentsQuantity, installmentDueDay, FinancialTransactionStatus.WAITING);
    }

    public InstallmentPlanTransaction(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method, Integer installmentsQuantity, Integer installmentDueDay, FinancialTransactionStatus financialTransactionStatus) {
        super(senderType, senderId, receiverType, receiverId, type, amount, transactionDate, description, method);
        this.installmentsQuantity = installmentsQuantity;
        this.installmentDueDay = validateInstallmentDueDay(installmentDueDay);
        setStatus(financialTransactionStatus);
        createInstallments(senderType, senderId, receiverType, receiverId, type, amount, transactionDate, description, method, resolveInstallmentStatus(financialTransactionStatus));
    }

    private void createInstallments(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId, FinancialTransactionType type, BigDecimal amount, LocalDate transactionDate, String description, FinancialTransactionMethod method, InstallmentTransactionStatus installmentStatus) {
        BigDecimal installmentAmount = amount.divide(BigDecimal.valueOf(installmentsQuantity), 2, RoundingMode.DOWN);
        BigDecimal distributedAmount = BigDecimal.ZERO;

        for (int index = 1; index <= installmentsQuantity; index++) {
            BigDecimal currentAmount = installmentAmount;
            if (index == installmentsQuantity) {
                currentAmount = amount.subtract(distributedAmount);
            }

            distributedAmount = distributedAmount.add(currentAmount);
            YearMonth dueMonth = YearMonth.from(transactionDate).plusMonths(index - 1L);
            LocalDate dueDate = dueMonth.atDay(Math.min(installmentDueDay, dueMonth.lengthOfMonth()));
            installments.add(new InstallmentTransaction(
                    senderType,
                    senderId,
                    receiverType,
                    receiverId,
                    type,
                    currentAmount,
                    transactionDate.plusMonths(index - 1L),
                    description + " - parcela " + index + "/" + installmentsQuantity,
                    method,
                    this,
                    index,
                    installmentsQuantity,
                    dueDate,
                    installmentStatus
            ));
        }
    }

    private InstallmentTransactionStatus resolveInstallmentStatus(FinancialTransactionStatus financialTransactionStatus) {
        if (financialTransactionStatus == FinancialTransactionStatus.SETTLED) {
            return InstallmentTransactionStatus.SETTLED;
        }

        return InstallmentTransactionStatus.WAITING;
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

    public List<InstallmentTransaction> getInstallments() {
        return installments;
    }

    public void restoreInstallments(List<InstallmentTransaction> installments) {
        this.installments = new ArrayList<>(installments);
    }

    public InstallmentTransaction findInstallment(Integer installmentNumber) {
        if (installmentNumber == null) {
            throw new IllegalArgumentException("Numero da parcela e obrigatorio.");
        }
        return installments.stream()
                .filter(installment -> installmentNumber.equals(installment.getInstallmentNumber()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Parcela nao encontrada no plano."));
    }

    public void refreshStatus() {
        if (installments.stream().anyMatch(
                installment -> installment.getInstallmentStatus() == InstallmentTransactionStatus.OVERDUE
        )) {
            setStatus(FinancialTransactionStatus.OVERDUE);
            return;
        }

        boolean hasSettledInstallment = installments.stream().anyMatch(
                installment -> installment.getInstallmentStatus() == InstallmentTransactionStatus.SETTLED
        );
        setStatus(hasSettledInstallment
                ? FinancialTransactionStatus.ON_TIME
                : FinancialTransactionStatus.WAITING);
    }

    public boolean areAllInstallmentsSettled() {
        return !installments.isEmpty() && installments.stream().allMatch(
                installment -> installment.getInstallmentStatus() == InstallmentTransactionStatus.SETTLED
        );
    }

    private Integer validateInstallmentDueDay(Integer installmentDueDay) {
        if (installmentDueDay == null || installmentDueDay < 1 || installmentDueDay > 31) {
            throw new IllegalArgumentException("Dia mensal de vencimento deve estar entre 1 e 31.");
        }
        return installmentDueDay;
    }
}
