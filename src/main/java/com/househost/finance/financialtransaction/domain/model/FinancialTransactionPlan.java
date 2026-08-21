package com.househost.finance.financialtransaction.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FinancialTransactionPlan {

    private Long id;
    private final FinancialPartyType senderType;
    private final Long senderId;
    private final FinancialPartyType receiverType;
    private final Long receiverId;
    private final FinancialTransactionSourceType sourceType;
    private final Long sourceId;
    private BigDecimal totalAmount;
    private FinancialTransactionPlanStatus status;
    private final List<FinancialTransaction> financialTransactionList;
    private LocalDate planDueDate;
    private LocalDate planSettlementDate;
    private final String description;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean canceled;

    public FinancialTransactionPlan(
            FinancialPartyType senderType,
            Long senderId,
            FinancialPartyType receiverType,
            Long receiverId,
            FinancialTransactionSourceType sourceType,
            Long sourceId,
            List<FinancialTransaction> financialTransactionList,
            LocalDate planDueDate,
            String description
    ) {
        this(
                null,
                senderType,
                senderId,
                receiverType,
                receiverId,
                sourceType,
                sourceId,
                financialTransactionList,
                planDueDate,
                description
        );
    }

    public FinancialTransactionPlan(
            Long id,
            FinancialPartyType senderType,
            Long senderId,
            FinancialPartyType receiverType,
            Long receiverId,
            FinancialTransactionSourceType sourceType,
            Long sourceId,
            List<FinancialTransaction> financialTransactionList,
            LocalDate planDueDate,
            String description
    ) {
        validateParticipants(senderType, senderId, receiverType, receiverId);
        validateExternalSource(sourceType, sourceId);
        this.id = id;
        this.senderType = senderType;
        this.senderId = senderId;
        this.receiverType = receiverType;
        this.receiverId = receiverId;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.planDueDate = requirePlanDueDate(planDueDate);
        this.description = normalizeDescription(description);
        this.financialTransactionList = copyAndValidateFinancialTransactionList(
                financialTransactionList
        );
        reorderAndRefresh(LocalDate.now());
    }

    public void assignIdentity(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Identificador do plano financeiro e obrigatorio.");
        }
        if (this.id != null && !this.id.equals(id)) {
            throw new IllegalStateException("Identidade do plano financeiro nao pode ser alterada.");
        }

        this.id = id;
        assignPlanMembership();
        validateFinancialTransactionList(financialTransactionList);
    }

    public void restorePersistenceState(
            Long version,
            FinancialTransactionPlanStatus persistedStatus,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        canceled = persistedStatus == FinancialTransactionPlanStatus.CANCELED;
        if (canceled && hasSettledFinancialTransaction()) {
            throw new IllegalStateException("Plano cancelado nao pode possuir historico liquidado.");
        }
        refreshDerivedState(LocalDate.now());
    }

    public void addFinancialTransaction(FinancialTransaction financialTransaction) {
        assertCompositionMutable();
        validateFinancialTransaction(financialTransaction);
        assertNotDuplicate(financialTransaction, financialTransactionList);
        financialTransactionList.add(financialTransaction);
        reorderAndRefresh(LocalDate.now());
    }

    public void addFinancialTransactionList(
            List<FinancialTransaction> additionalFinancialTransactionList
    ) {
        assertCompositionMutable();
        if (additionalFinancialTransactionList == null
                || additionalFinancialTransactionList.isEmpty()) {
            throw new IllegalArgumentException("Lista adicional de transacoes e obrigatoria.");
        }

        List<FinancialTransaction> candidateFinancialTransactionList =
                new ArrayList<>(financialTransactionList);
        for (FinancialTransaction financialTransaction : additionalFinancialTransactionList) {
            validateFinancialTransaction(financialTransaction);
            assertNotDuplicate(financialTransaction, candidateFinancialTransactionList);
            candidateFinancialTransactionList.add(financialTransaction);
        }

        financialTransactionList.clear();
        financialTransactionList.addAll(candidateFinancialTransactionList);
        reorderAndRefresh(LocalDate.now());
    }

    public FinancialTransaction removeFinancialTransaction(Long financialTransactionId) {
        assertCompositionMutable();
        FinancialTransaction financialTransaction = findRequiredFinancialTransaction(
                financialTransactionId
        );
        assertEligibleForRemoval(financialTransaction);
        if (financialTransactionList.size() == 1) {
            throw new IllegalStateException("Plano financeiro deve manter ao menos um componente.");
        }

        financialTransactionList.remove(financialTransaction);
        reorderAndRefresh(LocalDate.now());
        return financialTransaction;
    }

    public void replaceFinancialTransaction(
            Long currentFinancialTransactionId,
            FinancialTransaction replacementFinancialTransaction
    ) {
        assertCompositionMutable();
        FinancialTransaction currentFinancialTransaction = findRequiredFinancialTransaction(
                currentFinancialTransactionId
        );
        assertEligibleForReplacement(currentFinancialTransaction);
        validateFinancialTransaction(replacementFinancialTransaction);

        List<FinancialTransaction> financialTransactionWithoutCurrentList =
                new ArrayList<>(financialTransactionList);
        int replacementIndex = financialTransactionWithoutCurrentList.indexOf(
                currentFinancialTransaction
        );
        financialTransactionWithoutCurrentList.remove(currentFinancialTransaction);
        assertNotDuplicate(
                replacementFinancialTransaction,
                financialTransactionWithoutCurrentList
        );
        financialTransactionWithoutCurrentList.add(
                replacementIndex,
                replacementFinancialTransaction
        );

        financialTransactionList.clear();
        financialTransactionList.addAll(financialTransactionWithoutCurrentList);
        reorderAndRefresh(LocalDate.now());
    }

    public void extendPlanDueDate(LocalDate extendedPlanDueDate) {
        assertPlanMutable();
        if (extendedPlanDueDate == null || !extendedPlanDueDate.isAfter(planDueDate)) {
            throw new IllegalArgumentException("Novo prazo do plano deve ser posterior ao prazo atual.");
        }

        planDueDate = extendedPlanDueDate;
        validateFinancialTransactionList(financialTransactionList);
        refreshDerivedState(LocalDate.now());
    }

    public void cancel() {
        assertPlanMutable();
        if (hasSettledFinancialTransaction()) {
            throw new IllegalStateException("Plano com historico liquidado nao pode ser cancelado.");
        }
        financialTransactionList.forEach(FinancialTransaction::cancel);
        canceled = true;
        refreshDerivedState(LocalDate.now());
    }

    public boolean isEligibleForPhysicalDeletion() {
        return !hasSettledFinancialTransaction();
    }

    public void refreshDerivedState(LocalDate referenceDate) {
        if (referenceDate == null) {
            throw new IllegalArgumentException("Data de referencia do plano e obrigatoria.");
        }
        if (canceled) {
            status = FinancialTransactionPlanStatus.CANCELED;
            planSettlementDate = null;
            return;
        }

        List<FinancialTransaction> payableFinancialTransactionList =
                getSettlementFinancialTransactionList().stream()
                        .filter(this::isPayable)
                        .toList();
        boolean allSettled = !payableFinancialTransactionList.isEmpty()
                && payableFinancialTransactionList.stream().allMatch(this::isSettled);
        if (allSettled) {
            status = FinancialTransactionPlanStatus.SETTLED;
            planSettlementDate = payableFinancialTransactionList.stream()
                    .map(FinancialTransaction::getSettlementDate)
                    .max(LocalDate::compareTo)
                    .orElseThrow(() -> new IllegalStateException(
                            "Plano liquidado exige datas de liquidacao individuais."
                    ));
            return;
        }

        planSettlementDate = null;
        if (payableFinancialTransactionList.stream().anyMatch(
                financialTransaction -> isOverdue(financialTransaction, referenceDate)
        )) {
            status = FinancialTransactionPlanStatus.OVERDUE;
            return;
        }
        if (payableFinancialTransactionList.stream().anyMatch(this::isSettled)) {
            status = FinancialTransactionPlanStatus.PARTIALLY_SETTLED;
            return;
        }
        status = FinancialTransactionPlanStatus.ACTIVE;
    }

    public List<FinancialTransaction> getFinancialTransactionList() {
        return List.copyOf(financialTransactionList);
    }

    public Optional<FinancialTransaction> findFinancialTransactionById(Long financialTransactionId) {
        if (financialTransactionId == null) {
            return Optional.empty();
        }
        return financialTransactionList.stream()
                .filter(financialTransaction -> financialTransactionId.equals(
                        financialTransaction.getId()
                ))
                .findFirst();
    }

    public boolean containsFinancialTransaction(Long financialTransactionId) {
        return findFinancialTransactionById(financialTransactionId).isPresent();
    }

    public int getFinancialTransactionCount() {
        return financialTransactionList.size();
    }

    public Optional<FinancialTransaction> findFinancialTransactionByOrder(Integer order) {
        if (order == null || order < 1 || order > financialTransactionList.size()) {
            return Optional.empty();
        }
        return Optional.of(financialTransactionList.get(order - 1));
    }

    public List<FinancialTransaction> getSettlementFinancialTransactionList() {
        List<FinancialTransaction> settlementFinancialTransactionList = new ArrayList<>();
        for (FinancialTransaction financialTransaction : financialTransactionList) {
            if (financialTransaction instanceof InstallmentPlanTransaction installmentPlanTransaction) {
                settlementFinancialTransactionList.addAll(
                        installmentPlanTransaction.getInstallments().stream()
                                .sorted(Comparator.comparing(
                                        InstallmentTransaction::getInstallmentNumber
                                ))
                                .toList()
                );
            } else {
                settlementFinancialTransactionList.add(financialTransaction);
            }
        }
        settlementFinancialTransactionList.sort(
                Comparator.comparing(FinancialTransaction::getDueDate)
        );
        return List.copyOf(settlementFinancialTransactionList);
    }

    public List<FinancialTransaction> getSettledFinancialTransactionList() {
        return getSettlementFinancialTransactionList().stream()
                .filter(this::isSettled)
                .toList();
    }

    public List<FinancialTransaction> getUnsettledFinancialTransactionList() {
        return getSettlementFinancialTransactionList().stream()
                .filter(financialTransaction -> !isSettled(financialTransaction))
                .toList();
    }

    public List<FinancialTransaction> getOverdueFinancialTransactionList(LocalDate referenceDate) {
        requireReferenceDate(referenceDate);
        return getSettlementFinancialTransactionList().stream()
                .filter(financialTransaction -> isOverdue(financialTransaction, referenceDate))
                .toList();
    }

    public List<FinancialTransaction> getFinancialTransactionListDueOn(LocalDate dueDate) {
        requireReferenceDate(dueDate);
        return getSettlementFinancialTransactionList().stream()
                .filter(financialTransaction -> dueDate.equals(financialTransaction.getDueDate()))
                .toList();
    }

    public List<FinancialTransaction> getFinancialTransactionListDueBetween(
            LocalDate initialDate,
            LocalDate finalDate
    ) {
        requireReferenceDate(initialDate);
        requireReferenceDate(finalDate);
        if (finalDate.isBefore(initialDate)) {
            throw new IllegalArgumentException("Intervalo de vencimento e invalido.");
        }
        return getSettlementFinancialTransactionList().stream()
                .filter(financialTransaction -> !financialTransaction.getDueDate().isBefore(initialDate))
                .filter(financialTransaction -> !financialTransaction.getDueDate().isAfter(finalDate))
                .toList();
    }

    public Optional<FinancialTransaction> findNextFinancialTransactionToSettle() {
        return getSettlementFinancialTransactionList().stream()
                .filter(this::isPayable)
                .filter(financialTransaction -> !isSettled(financialTransaction))
                .findFirst();
    }

    public Optional<FinancialTransaction> findLastSettledFinancialTransaction() {
        return getSettlementFinancialTransactionList().stream()
                .filter(this::isSettled)
                .max(Comparator.comparing(FinancialTransaction::getSettlementDate));
    }

    public BigDecimal calculateSettledAmount() {
        return getSettlementFinancialTransactionList().stream()
                .filter(this::isPayable)
                .filter(this::isSettled)
                .map(FinancialTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateOutstandingAmount() {
        return getSettlementFinancialTransactionList().stream()
                .filter(this::isPayable)
                .filter(financialTransaction -> !isSettled(financialTransaction))
                .map(FinancialTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateOverdueAmount(LocalDate referenceDate) {
        return getOverdueFinancialTransactionList(referenceDate).stream()
                .map(FinancialTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateSettlementPercentage() {
        BigDecimal payableAmount = calculateSettledAmount().add(calculateOutstandingAmount());
        if (payableAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return calculateSettledAmount()
                .multiply(BigDecimal.valueOf(100))
                .divide(payableAmount, 2, RoundingMode.HALF_UP);
    }

    public boolean isFullySettled() {
        List<FinancialTransaction> payableFinancialTransactionList =
                getSettlementFinancialTransactionList().stream()
                        .filter(this::isPayable)
                        .toList();
        return !payableFinancialTransactionList.isEmpty()
                && payableFinancialTransactionList.stream().allMatch(this::isSettled);
    }

    public boolean hasOverdueFinancialTransaction(LocalDate referenceDate) {
        return !getOverdueFinancialTransactionList(referenceDate).isEmpty();
    }

    public LocalDate getFirstDueDate() {
        return getSettlementFinancialTransactionList().stream()
                .map(FinancialTransaction::getDueDate)
                .min(LocalDate::compareTo)
                .orElseThrow(() -> new IllegalStateException("Plano financeiro sem vencimentos."));
    }

    public LocalDate getLastDueDate() {
        return getSettlementFinancialTransactionList().stream()
                .map(FinancialTransaction::getDueDate)
                .max(LocalDate::compareTo)
                .orElseThrow(() -> new IllegalStateException("Plano financeiro sem vencimentos."));
    }

    public Long getId() {
        return id;
    }

    public FinancialPartyType getSenderType() {
        return senderType;
    }

    public Long getSenderId() {
        return senderId;
    }

    public FinancialPartyType getReceiverType() {
        return receiverType;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public FinancialTransactionSourceType getSourceType() {
        return sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public FinancialTransactionPlanStatus getStatus() {
        return status;
    }

    public LocalDate getPlanDueDate() {
        return planDueDate;
    }

    public LocalDate getPlanSettlementDate() {
        return planSettlementDate;
    }

    public String getDescription() {
        return description;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private List<FinancialTransaction> copyAndValidateFinancialTransactionList(
            List<FinancialTransaction> financialTransactionList
    ) {
        if (financialTransactionList == null || financialTransactionList.isEmpty()) {
            throw new IllegalArgumentException("Plano financeiro exige ao menos um componente.");
        }
        List<FinancialTransaction> copiedFinancialTransactionList =
                new ArrayList<>(financialTransactionList);
        validateFinancialTransactionList(copiedFinancialTransactionList);
        return copiedFinancialTransactionList;
    }

    private void validateFinancialTransactionList(
            List<FinancialTransaction> candidateFinancialTransactionList
    ) {
        Set<Long> financialTransactionIdSet = new HashSet<>();
        Set<FinancialTransaction> financialTransactionReferenceSet =
                java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (FinancialTransaction financialTransaction : candidateFinancialTransactionList) {
            validateFinancialTransaction(financialTransaction);
            if (!financialTransactionReferenceSet.add(financialTransaction)) {
                throw new IllegalArgumentException("Transacao duplicada no plano financeiro.");
            }
            if (financialTransaction.getId() != null
                    && !financialTransactionIdSet.add(financialTransaction.getId())) {
                throw new IllegalArgumentException("Identidade de transacao duplicada no plano financeiro.");
            }
        }
    }

    private void validateFinancialTransaction(FinancialTransaction financialTransaction) {
        if (financialTransaction == null) {
            throw new IllegalArgumentException("Componente financeiro e obrigatorio.");
        }
        if (financialTransaction instanceof InstallmentTransaction) {
            throw new IllegalArgumentException("Parcela interna nao pode ser componente direto do plano.");
        }
        if (!isEligibleDirectType(financialTransaction.getType())) {
            throw new IllegalArgumentException("Tipo de transacao invalido para componente direto do plano.");
        }
        if (financialTransaction.getAmount() == null
                || financialTransaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do componente financeiro deve ser positivo.");
        }
        if (financialTransaction.getSenderType() != senderType
                || !senderId.equals(financialTransaction.getSenderId())
                || financialTransaction.getReceiverType() != receiverType
                || !receiverId.equals(financialTransaction.getReceiverId())) {
            throw new IllegalArgumentException("Participantes do componente diferem do plano financeiro.");
        }
        if (financialTransaction.getDueDate() == null
                || financialTransaction.getDueDate().isAfter(planDueDate)) {
            throw new IllegalArgumentException("Vencimento do componente excede o prazo do plano.");
        }
        validatePlanMembership(financialTransaction);
        if (financialTransaction instanceof InstallmentPlanTransaction installmentPlanTransaction) {
            validateInstallmentPlanTransaction(installmentPlanTransaction);
        }
        if (isSettled(financialTransaction) && financialTransaction.getSettlementDate() == null) {
            throw new IllegalArgumentException("Transacao liquidada exige data de liquidacao.");
        }
    }

    private void validatePlanMembership(FinancialTransaction financialTransaction) {
        if (id == null) {
            if (financialTransaction.getSourceType() != null
                    || financialTransaction.getSourceId() != null
                    || financialTransaction.getPlanComponentOrder() != null) {
                throw new IllegalArgumentException(
                        "Componente novo deve aguardar a identidade do plano para receber origem."
                );
            }
            return;
        }
        if (financialTransaction.getSourceType() != FinancialTransactionSourceType.PLAN
                || !id.equals(financialTransaction.getSourceId())
                || financialTransaction.getPlanComponentOrder() == null) {
            throw new IllegalArgumentException("Componente nao possui associacao valida com o plano.");
        }
    }

    private void validateInstallmentPlanTransaction(
            InstallmentPlanTransaction installmentPlanTransaction
    ) {
        if (installmentPlanTransaction.getInstallments().size() < 2
                || installmentPlanTransaction.getInstallments().size() > 12) {
            throw new IllegalArgumentException("Bloco parcelado deve conter entre 2 e 12 parcelas.");
        }
        for (InstallmentTransaction installmentTransaction
                : installmentPlanTransaction.getInstallments()) {
            if (installmentTransaction.getInstallmentPlan() != installmentPlanTransaction) {
                throw new IllegalArgumentException("Parcela interna possui bloco proprietario inconsistente.");
            }
            if (installmentTransaction.getDueDate().isAfter(planDueDate)) {
                throw new IllegalArgumentException("Vencimento de parcela interna excede o prazo do plano.");
            }
            if (installmentTransaction.getSourceType()
                    != FinancialTransactionSourceType.INSTALLMENT) {
                throw new IllegalArgumentException("Origem da parcela interna e invalida.");
            }
            if (installmentPlanTransaction.getId() != null
                    && !installmentPlanTransaction.getId().equals(
                            installmentTransaction.getSourceId()
                    )) {
                throw new IllegalArgumentException("Parcela interna aponta para bloco incorreto.");
            }
        }
    }

    private void reorderAndRefresh(LocalDate referenceDate) {
        financialTransactionList.sort(Comparator.comparing(FinancialTransaction::getDueDate));
        if (id != null) {
            assignPlanMembership();
        }
        totalAmount = financialTransactionList.stream()
                .map(FinancialTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        refreshDerivedState(referenceDate);
    }

    private void assignPlanMembership() {
        for (int index = 0; index < financialTransactionList.size(); index++) {
            financialTransactionList.get(index).assignPlanMembership(id, index + 1);
        }
    }

    private void assertNotDuplicate(
            FinancialTransaction financialTransaction,
            List<FinancialTransaction> candidateFinancialTransactionList
    ) {
        boolean duplicated = candidateFinancialTransactionList.stream().anyMatch(
                candidateFinancialTransaction -> candidateFinancialTransaction == financialTransaction
                        || financialTransaction.getId() != null
                        && financialTransaction.getId().equals(candidateFinancialTransaction.getId())
        );
        if (duplicated) {
            throw new IllegalArgumentException("Transacao duplicada no plano financeiro.");
        }
    }

    private FinancialTransaction findRequiredFinancialTransaction(Long financialTransactionId) {
        return findFinancialTransactionById(financialTransactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transacao nao pertence ao plano financeiro."
                ));
    }

    private void assertCompositionMutable() {
        assertPlanMutable();
        if (getStatus() == FinancialTransactionPlanStatus.SETTLED) {
            throw new IllegalStateException("Plano liquidado possui composicao imutavel.");
        }
    }

    private void assertPlanMutable() {
        refreshDerivedState(LocalDate.now());
        if (canceled) {
            throw new IllegalStateException("Plano cancelado e imutavel.");
        }
        if (getStatus() == FinancialTransactionPlanStatus.SETTLED) {
            throw new IllegalStateException("Plano liquidado e imutavel.");
        }
    }

    private void assertEligibleForRemoval(FinancialTransaction financialTransaction) {
        if (hasSettledHistory(financialTransaction)) {
            throw new IllegalStateException("Componente com historico liquidado nao pode ser removido.");
        }
        if (!isPayable(financialTransaction)) {
            throw new IllegalStateException("Componente nao esta elegivel para remocao.");
        }
    }

    private void assertEligibleForReplacement(FinancialTransaction financialTransaction) {
        assertEligibleForRemoval(financialTransaction);
        if (financialTransaction.getStatus() != FinancialTransactionStatus.WAITING
                && financialTransaction.getStatus() != FinancialTransactionStatus.OVERDUE) {
            throw new IllegalStateException("Somente componente aguardando ou vencido pode ser substituido.");
        }
    }

    private boolean hasSettledFinancialTransaction() {
        return getSettlementFinancialTransactionList().stream().anyMatch(this::isSettled);
    }

    private boolean hasSettledHistory(FinancialTransaction financialTransaction) {
        if (financialTransaction instanceof InstallmentPlanTransaction installmentPlanTransaction) {
            return installmentPlanTransaction.getInstallments().stream().anyMatch(this::isSettled);
        }
        return isSettled(financialTransaction);
    }

    private boolean isSettled(FinancialTransaction financialTransaction) {
        return financialTransaction.getStatus() == FinancialTransactionStatus.SETTLED;
    }

    private boolean isPayable(FinancialTransaction financialTransaction) {
        return financialTransaction.getStatus() != FinancialTransactionStatus.CANCELED;
    }

    private boolean isOverdue(
            FinancialTransaction financialTransaction,
            LocalDate referenceDate
    ) {
        return isPayable(financialTransaction)
                && !isSettled(financialTransaction)
                && (financialTransaction.getStatus() == FinancialTransactionStatus.OVERDUE
                || financialTransaction.getDueDate().isBefore(referenceDate));
    }

    private boolean isEligibleDirectType(FinancialTransactionType financialTransactionType) {
        return financialTransactionType == FinancialTransactionType.STANDARD
                || financialTransactionType == FinancialTransactionType.PLAN_DOWN_PAYMENT
                || financialTransactionType == FinancialTransactionType.PLAN_CHECK_IN_PAYMENT
                || financialTransactionType == FinancialTransactionType.PLAN_CHECK_OUT_PAYMENT
                || financialTransactionType == FinancialTransactionType.PLAN_TRANSACTION
                || financialTransactionType == FinancialTransactionType.INSTALLMENT_PLAN_BLOCK;
    }

    private void validateParticipants(
            FinancialPartyType senderType,
            Long senderId,
            FinancialPartyType receiverType,
            Long receiverId
    ) {
        if (senderType == null || senderId == null || receiverType == null || receiverId == null) {
            throw new IllegalArgumentException("Participantes do plano financeiro sao obrigatorios.");
        }
        if (senderType == receiverType && senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Pagante e recebedor do plano devem ser diferentes.");
        }
    }

    private void validateExternalSource(
            FinancialTransactionSourceType sourceType,
            Long sourceId
    ) {
        if (sourceType == null || sourceId == null) {
            throw new IllegalArgumentException("Origem externa do plano financeiro e obrigatoria.");
        }
        if (sourceType == FinancialTransactionSourceType.PLAN
                || sourceType == FinancialTransactionSourceType.INSTALLMENT) {
            throw new IllegalArgumentException("Origem externa nao pode representar propriedade financeira.");
        }
    }

    private LocalDate requirePlanDueDate(LocalDate planDueDate) {
        if (planDueDate == null) {
            throw new IllegalArgumentException("Prazo contratual do plano financeiro e obrigatorio.");
        }
        return planDueDate;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Descricao do plano financeiro e obrigatoria.");
        }
        return description.trim();
    }

    private void requireReferenceDate(LocalDate referenceDate) {
        if (referenceDate == null) {
            throw new IllegalArgumentException("Data de referencia e obrigatoria.");
        }
    }
}
