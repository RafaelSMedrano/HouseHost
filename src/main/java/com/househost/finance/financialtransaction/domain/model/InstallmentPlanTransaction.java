package com.househost.finance.financialtransaction.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class InstallmentPlanTransaction extends FinancialTransaction {

    private Integer installmentsQuantity;

    private Integer installmentDueDay;

    private List<InstallmentTransaction> installmentTransactionList = new ArrayList<>();

    public InstallmentPlanTransaction() {
    }

    public InstallmentPlanTransaction(
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

    public InstallmentPlanTransaction(
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
            FinancialTransactionType type
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
                type,
                FinancialTransactionStatus.WAITING
        );
    }

    public InstallmentPlanTransaction(
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

    public InstallmentPlanTransaction(
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
                type,
                financialTransactionStatus,
                true
        );
    }

    private InstallmentPlanTransaction(
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
            FinancialTransactionStatus financialTransactionStatus,
            boolean enforceCurrentInstallmentLimits
    ) {
        super(
                senderType,
                senderId,
                receiverType,
                receiverId,
                validateDirectType(type),
                amount,
                transactionDate,
                description,
                method
        );
        this.installmentsQuantity = enforceCurrentInstallmentLimits
                ? validateInstallmentsQuantity(installmentsQuantity)
                : validateHistoricalInstallmentsQuantity(installmentsQuantity);
        this.installmentDueDay = validateInstallmentDueDay(installmentDueDay);
        setStatus(financialTransactionStatus);
        createInstallments(
                senderType,
                senderId,
                receiverType,
                receiverId,
                amount,
                transactionDate,
                description,
                method,
                resolveInstallmentStatus(financialTransactionStatus)
        );
    }

    public static InstallmentPlanTransaction restore(
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
        return new InstallmentPlanTransaction(
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
                type,
                financialTransactionStatus,
                false
        );
    }

    private void createInstallments(
            FinancialPartyType senderType,
            Long senderId,
            FinancialPartyType receiverType,
            Long receiverId,
            BigDecimal amount,
            LocalDate transactionDate,
            String description,
            FinancialTransactionMethod method,
            InstallmentTransactionStatus installmentStatus
    ) {
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
            installmentTransactionList.add(new InstallmentTransaction(
                    senderType,
                    senderId,
                    receiverType,
                    receiverId,
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

    private InstallmentTransactionStatus resolveInstallmentStatus(
            FinancialTransactionStatus financialTransactionStatus
    ) {
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
        synchronizeInstallmentSources();
    }

    @Override
    public void restorePersistenceState(
            Long id,
            LocalDate creationDate,
            LocalDate settlementDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        super.restorePersistenceState(id, creationDate, settlementDate, createdAt, updatedAt);
        synchronizeInstallmentSources();
    }

    public List<InstallmentTransaction> getInstallments() {
        return installmentTransactionList;
    }

    public void restoreInstallments(List<InstallmentTransaction> installmentTransactionList) {
        this.installmentTransactionList = new ArrayList<>(installmentTransactionList);
        synchronizeInstallmentSources();
    }

    public InstallmentTransaction findInstallment(Integer installmentNumber) {
        if (installmentNumber == null) {
            throw new IllegalArgumentException("Numero da parcela e obrigatorio.");
        }
        return installmentTransactionList.stream()
                .filter(installment -> installmentNumber.equals(installment.getInstallmentNumber()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Parcela nao encontrada no plano."));
    }

    public void refreshStatus() {
        if (installmentTransactionList.stream().anyMatch(
                installment -> installment.getInstallmentStatus() == InstallmentTransactionStatus.OVERDUE
        )) {
            setStatus(FinancialTransactionStatus.OVERDUE);
            return;
        }

        boolean hasSettledInstallment = installmentTransactionList.stream().anyMatch(
                installment -> installment.getInstallmentStatus() == InstallmentTransactionStatus.SETTLED
        );
        setStatus(hasSettledInstallment
                ? FinancialTransactionStatus.ON_TIME
                : FinancialTransactionStatus.WAITING);
    }

    public boolean areAllInstallmentsSettled() {
        return !installmentTransactionList.isEmpty() && installmentTransactionList.stream().allMatch(
                installment -> installment.getInstallmentStatus() == InstallmentTransactionStatus.SETTLED
        );
    }

    @Override
    public void settle(LocalDate effectiveSettlementDate) {
        super.settle(effectiveSettlementDate);
        installmentTransactionList.forEach(
                installmentTransaction -> installmentTransaction.settle(effectiveSettlementDate)
        );
    }

    @Override
    public void cancel() {
        super.cancel();
        installmentTransactionList.forEach(InstallmentTransaction::cancel);
    }

    private Integer validateInstallmentDueDay(Integer installmentDueDay) {
        if (installmentDueDay == null || installmentDueDay < 1 || installmentDueDay > 31) {
            throw new IllegalArgumentException("Dia mensal de vencimento deve estar entre 1 e 31.");
        }
        return installmentDueDay;
    }

    private Integer validateInstallmentsQuantity(Integer installmentsQuantity) {
        if (installmentsQuantity == null || installmentsQuantity < 2 || installmentsQuantity > 12) {
            throw new IllegalArgumentException("Quantidade de parcelas deve estar entre 2 e 12.");
        }
        return installmentsQuantity;
    }

    private Integer validateHistoricalInstallmentsQuantity(Integer installmentsQuantity) {
        if (installmentsQuantity == null || installmentsQuantity < 1) {
            throw new IllegalArgumentException("Quantidade historica de parcelas deve ser positiva.");
        }
        return installmentsQuantity;
    }

    private void synchronizeInstallmentSources() {
        installmentTransactionList.forEach(InstallmentTransaction::synchronizeSourceWithPlan);
    }

    private static FinancialTransactionType validateDirectType(FinancialTransactionType type) {
        if (type == FinancialTransactionType.PLAN_DOWN_PAYMENT
                || type == FinancialTransactionType.PLAN_CHECK_IN_PAYMENT
                || type == FinancialTransactionType.PLAN_CHECK_OUT_PAYMENT
                || type == FinancialTransactionType.PLAN_TRANSACTION
                || type == FinancialTransactionType.INSTALLMENT_PLAN_BLOCK) {
            return type;
        }

        throw new IllegalArgumentException("Tipo direto invalido para bloco parcelado.");
    }
}
