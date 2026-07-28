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
   private final FinancialParticipantNotifier participantNotifier;
   private final FinancialSourceNotifier sourceNotifier;
   private final FinancialAuditPort financialAuditPort;
   private final FinancialTransactionValidationService validationService;

   // CASOS DE USO

   public FinancialTransactionService(FinancialTransactionPersistencePort financialTransactionRepository, FinancialParticipantNotifier participantNotifier, FinancialSourceNotifier sourceNotifier, FinancialAuditPort financialAuditPort, FinancialTransactionValidationService validationService) {
      this.financialTransactionRepository = financialTransactionRepository;
      this.participantNotifier = participantNotifier;
      this.sourceNotifier = sourceNotifier;
      this.financialAuditPort = financialAuditPort;
      this.validationService = validationService;
   }

   @Transactional
   public FinancialTransactionResponseDTO create(FinancialTransactionRequestDTO request) {
      this.validationService.validateRequest(request);
      FinancialPartyType senderType = request.senderType;
      FinancialPartyType receiverType = request.receiverType;
      this.validationService.validateDifferentParties(senderType, request.senderId, receiverType, request.receiverId);
      FinancialTransactionType type = request.type;
      FinancialTransactionStatus status = FinancialTransactionStatus.WAITING;
      FinancialTransactionMethod method = request.method;
      FinancialTransactionSourceType sourceType = request.sourceType;
      this.validationService.validateSource(sourceType, request.sourceId);
      LocalDate transactionDate = this.normalizeDate(request.transactionDate);
      LocalDate dueDate = request.dueDate == null ? transactionDate : request.dueDate;
      String description = this.normalizeRequired(request.description);
      FinancialTransaction transaction = new FinancialTransaction(senderType, request.senderId, receiverType, request.receiverId, type, request.amount, transactionDate, dueDate, description, method, status);

      transaction.setSource(sourceType, request.sourceId);
      FinancialTransaction savedTransaction = this.financialTransactionRepository.save(transaction);
      this.participantNotifier.notifyCreation(savedTransaction);
      this.financialAuditPort.record("FINANCIAL_TRANSACTION_CREATED", savedTransaction.getId(), this.auditMetadata(savedTransaction));
      return new FinancialTransactionResponseDTO(savedTransaction);
   }

   public List<FinancialTransactionResponseDTO> findAll() {
      List<FinancialTransactionResponseDTO> transactions = this.financialTransactionRepository.findAll().stream().map(FinancialTransactionResponseDTO::new).toList();
      this.financialAuditPort.record("FINANCIAL_TRANSACTION_LIST_VIEWED", (Long)null, Map.of("resultCount", transactions.size()));
      return transactions;
   }

   public FinancialTransactionResponseDTO findById(Long id) {
      FinancialTransaction transaction = this.findTransactionById(id);
      this.financialAuditPort.record("FINANCIAL_TRANSACTION_VIEWED", transaction.getId(), Map.of());
      return new FinancialTransactionResponseDTO(transaction);
   }

   public FinancialTransactionResponseDTO update(Long id, FinancialTransactionRequestDTO request) {
      this.validationService.validateRequest(request);
      FinancialTransaction transaction = this.findTransactionById(id);
      if (transaction.getStatus() == FinancialTransactionStatus.SETTLED) {
         throw new FinanceException("Transacao financeira liquidada nao pode ser alterada.");
      } else {
         this.validationService.validateImmutableUpdateFields(transaction, request);
         FinancialTransactionMethod method = request.method;
         FinancialTransactionSourceType sourceType = request.sourceType;
         this.validationService.validateSource(sourceType, request.sourceId);
         LocalDate transactionDate = this.normalizeDate(request.transactionDate);
         LocalDate dueDate = request.dueDate == null ? transactionDate : request.dueDate;
         String description = this.normalizeRequired(request.description);
         transaction.updateDetails(transactionDate, dueDate, description, method);

         transaction.setSource(sourceType, request.sourceId);
         FinancialTransaction savedTransaction = this.financialTransactionRepository.save(transaction);
         this.financialAuditPort.record("FINANCIAL_TRANSACTION_UPDATED", savedTransaction.getId(), this.auditMetadata(savedTransaction));
         return new FinancialTransactionResponseDTO(savedTransaction);
      }
   }

   @Transactional
   public FinancialTransactionResponseDTO toSettle(Long id) {
      FinancialTransaction transaction = this.findTransactionById(id);
      if (transaction.getStatus() == FinancialTransactionStatus.SETTLED) {
         throw new FinanceException("Transacao financeira ja esta liquidada.");
      } else {
         this.validationService.validateDifferentParties(transaction.getSenderType(), transaction.getSenderId(), transaction.getReceiverType(), transaction.getReceiverId());
         BigDecimal amount = transaction.getAmount();
         this.validationService.validatePositiveAmount(amount);
         transaction.setStatus(FinancialTransactionStatus.SETTLED);
         if (transaction instanceof InstallmentPlanTransaction) {
            InstallmentPlanTransaction installmentPlanTransaction = (InstallmentPlanTransaction)transaction;
            installmentPlanTransaction.getInstallments().forEach((installmentTransaction) -> {
               installmentTransaction.setStatus(FinancialTransactionStatus.SETTLED);
               installmentTransaction.setInstallmentStatus(InstallmentTransactionStatus.SETTLED);
            });
         }

         FinancialTransaction savedTransaction = this.financialTransactionRepository.save(transaction);
         this.participantNotifier.notifySettlement(savedTransaction);
         this.sourceNotifier.notifySettlement(savedTransaction);
         this.financialAuditPort.record("FINANCIAL_TRANSACTION_SETTLED", savedTransaction.getId(), this.auditMetadata(savedTransaction));
         return new FinancialTransactionResponseDTO(savedTransaction);
      }
   }

   @Transactional
   public void delete(Long id) {
      FinancialTransaction transaction = this.findTransactionById(id);
      this.participantNotifier.notifyDeletion(transaction);
      this.financialTransactionRepository.delete(transaction);
      this.financialAuditPort.record("FINANCIAL_TRANSACTION_DELETED", transaction.getId(), this.auditMetadata(transaction));
   }

   // PERSISTENCIA E AUDITORIA

   private FinancialTransaction findTransactionById(Long id) {
      if (id == null) {
         throw new FinanceException("Transacao financeira nao encontrada.");
      } else {
         return (FinancialTransaction)this.financialTransactionRepository.findById(id).orElseThrow(() -> new FinanceException("Transacao financeira nao encontrada."));
      }
   }

   private Map<String, Object> auditMetadata(FinancialTransaction transaction) {
      return Map.of(
              "status", transaction.getStatus().name(),
              "type", transaction.getType().name(),
              "amount", transaction.getAmount(),
              "transactionDate", transaction.getTransactionDate().toString()
      );
   }

   // NORMALIZACAO

   private LocalDate normalizeDate(LocalDate date) {
      return date == null ? LocalDate.now() : date;
   }

   private String normalizeRequired(String value) {
      return value.trim();
   }

}
