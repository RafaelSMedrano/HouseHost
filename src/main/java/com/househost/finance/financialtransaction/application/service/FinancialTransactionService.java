package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.dto.FinancialTransactionRequestDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionResponseDTO;
import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionUseCase;
import com.househost.finance.financialtransaction.application.port.out.FinancialAuditPort;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionPersistencePort;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import com.househost.finance.financialtransaction.domain.model.InstallmentTransactionStatus;
import com.househost.shared.exception.FinanceException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinancialTransactionService implements FinancialTransactionUseCase {

    private final FinancialTransactionPersistencePort financialTransactionRepository;
    private final FinancialParticipantNotifier financialParticipantNotifier;
    private final FinancialAuditPort financialAuditPort;
    private final FinancialTransactionValidationService financialTransactionValidationService;

    public FinancialTransactionService(
            FinancialTransactionPersistencePort financialTransactionRepository,
            FinancialParticipantNotifier financialParticipantNotifier,
            FinancialAuditPort financialAuditPort,
            FinancialTransactionValidationService financialTransactionValidationService
    ) {
        this.financialTransactionRepository = financialTransactionRepository;
        this.financialParticipantNotifier = financialParticipantNotifier;
        this.financialAuditPort = financialAuditPort;
        this.financialTransactionValidationService = financialTransactionValidationService;
    }

    @Override
    @Transactional
    public FinancialTransactionResponseDTO create(FinancialTransactionRequestDTO request) {
        financialTransactionValidationService.validateRequest(request);
        FinancialPartyType senderType = request.senderType;
        FinancialPartyType receiverType = request.receiverType;
        financialTransactionValidationService.validateDifferentParties(
                senderType,
                request.senderId,
                receiverType,
                request.receiverId
        );
        FinancialTransactionType type = request.type;
        financialTransactionValidationService.validateStandaloneCreationType(type);
        FinancialTransactionStatus status = FinancialTransactionStatus.WAITING;
        FinancialTransactionMethod method = request.method;
        FinancialTransactionSourceType sourceType = request.sourceType;
        financialTransactionValidationService.validateSource(sourceType, request.sourceId);
        LocalDate transactionDate = normalizeDate(request.transactionDate);
        LocalDate dueDate = request.dueDate == null ? transactionDate : request.dueDate;
        String description = normalizeRequired(request.description);
        FinancialTransaction transaction = new FinancialTransaction(
                senderType,
                request.senderId,
                receiverType,
                request.receiverId,
                type,
                request.amount,
                transactionDate,
                dueDate,
                description,
                method,
                status
        );

        transaction.setSource(sourceType, request.sourceId);
        FinancialTransaction savedTransaction = financialTransactionRepository.save(transaction);
        financialParticipantNotifier.notifyCreation(savedTransaction);
        financialAuditPort.record(
                "FINANCIAL_TRANSACTION_CREATED",
                savedTransaction.getId(),
                auditMetadata(savedTransaction)
        );
        return new FinancialTransactionResponseDTO(savedTransaction);
    }

    @Override
    public List<FinancialTransactionResponseDTO> findAll() {
        List<FinancialTransactionResponseDTO> financialTransactionResponseDTOList =
                financialTransactionRepository.findAll().stream()
                        .map(FinancialTransactionResponseDTO::new)
                        .toList();
        financialAuditPort.record(
                "FINANCIAL_TRANSACTION_LIST_VIEWED",
                null,
                Map.of("resultCount", financialTransactionResponseDTOList.size())
        );
        return financialTransactionResponseDTOList;
    }

    @Override
    public FinancialTransactionResponseDTO findById(Long id) {
        FinancialTransaction transaction = findTransactionById(id);
        financialAuditPort.record("FINANCIAL_TRANSACTION_VIEWED", transaction.getId(), Map.of());
        return new FinancialTransactionResponseDTO(transaction);
    }

    @Override
    public FinancialTransactionResponseDTO update(Long id, FinancialTransactionRequestDTO request) {
        financialTransactionValidationService.validateRequest(request);
        FinancialTransaction transaction = findTransactionById(id);
        if (transaction.getStatus() == FinancialTransactionStatus.SETTLED) {
            throw new FinanceException("Transacao financeira liquidada nao pode ser alterada.");
        }

        financialTransactionValidationService.validateImmutableUpdateFields(transaction, request);
        FinancialTransactionMethod method = request.method;
        FinancialTransactionSourceType sourceType = request.sourceType;
        financialTransactionValidationService.validateSource(sourceType, request.sourceId);
        LocalDate transactionDate = normalizeDate(request.transactionDate);
        LocalDate dueDate = request.dueDate == null ? transactionDate : request.dueDate;
        String description = normalizeRequired(request.description);
        transaction.updateDetails(transactionDate, dueDate, description, method);

        transaction.setSource(sourceType, request.sourceId);
        FinancialTransaction savedTransaction = financialTransactionRepository.save(transaction);
        financialAuditPort.record(
                "FINANCIAL_TRANSACTION_UPDATED",
                savedTransaction.getId(),
                auditMetadata(savedTransaction)
        );
        return new FinancialTransactionResponseDTO(savedTransaction);
    }

    @Override
    @Transactional
    public FinancialTransactionResponseDTO toSettle(Long id) {
        FinancialTransaction transaction = findTransactionById(id);
        if (transaction.getStatus() == FinancialTransactionStatus.SETTLED) {
            throw new FinanceException("Transacao financeira ja esta liquidada.");
        }

        financialTransactionValidationService.validateDifferentParties(
                transaction.getSenderType(),
                transaction.getSenderId(),
                transaction.getReceiverType(),
                transaction.getReceiverId()
        );
        BigDecimal amount = transaction.getAmount();
        financialTransactionValidationService.validatePositiveAmount(amount);
        transaction.setStatus(FinancialTransactionStatus.SETTLED);
        if (transaction instanceof InstallmentPlanTransaction installmentPlanTransaction) {
            installmentPlanTransaction.getInstallments().forEach(installmentTransaction -> {
                installmentTransaction.setStatus(FinancialTransactionStatus.SETTLED);
                installmentTransaction.setInstallmentStatus(InstallmentTransactionStatus.SETTLED);
            });
        }

        FinancialTransaction savedTransaction = financialTransactionRepository.save(transaction);
        financialParticipantNotifier.notifySettlement(savedTransaction);
        financialAuditPort.record(
                "FINANCIAL_TRANSACTION_SETTLED",
                savedTransaction.getId(),
                auditMetadata(savedTransaction)
        );
        return new FinancialTransactionResponseDTO(savedTransaction);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        FinancialTransaction transaction = findTransactionById(id);
        financialParticipantNotifier.notifyDeletion(transaction);
        financialTransactionRepository.delete(transaction);
        financialAuditPort.record(
                "FINANCIAL_TRANSACTION_DELETED",
                transaction.getId(),
                auditMetadata(transaction)
        );
    }

    private FinancialTransaction findTransactionById(Long id) {
        if (id == null) {
            throw new FinanceException("Transacao financeira nao encontrada.");
        }

        return financialTransactionRepository.findById(id)
                .orElseThrow(() -> new FinanceException("Transacao financeira nao encontrada."));
    }

    private Map<String, Object> auditMetadata(FinancialTransaction transaction) {
        return Map.of(
                "status", transaction.getStatus().name(),
                "type", transaction.getType().name(),
                "amount", transaction.getAmount(),
                "transactionDate", transaction.getTransactionDate().toString()
        );
    }

    private LocalDate normalizeDate(LocalDate date) {
        return date == null ? LocalDate.now() : date;
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }
}
