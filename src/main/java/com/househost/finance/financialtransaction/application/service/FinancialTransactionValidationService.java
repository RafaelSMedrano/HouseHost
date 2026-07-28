package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.dto.FinancialTransactionRequestDTO;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
class FinancialTransactionValidationService {

    void validateRequest(FinancialTransactionRequestDTO request) {
        if (request == null) throw new FinanceException("Dados da transacao financeira sao obrigatorios.");
        if (request.type == null) throw new FinanceException("Tipo da transacao financeira e obrigatorio.");
        validatePositiveAmount(request.amount);
        if (request.senderType == null) throw new FinanceException("Tipo do pagante e obrigatorio.");
        if (request.senderId == null) throw new FinanceException("Identificador do pagante da transacao financeira e obrigatorio.");
        if (request.receiverType == null) throw new FinanceException("Tipo do recebedor e obrigatorio.");
        if (request.receiverId == null) throw new FinanceException("Identificador do recebedor da transacao financeira e obrigatorio.");
    }

    void validateDifferentParties(FinancialPartyType senderType, Long senderId, FinancialPartyType receiverType, Long receiverId) {
        if (senderType == receiverType && senderId.equals(receiverId)) throw new FinanceException("Pagante e recebedor devem ser diferentes.");
    }

    void validateSource(FinancialTransactionSourceType sourceType, Long sourceId) {
        if (sourceType != null && sourceId == null) throw new FinanceException("Identificador da origem da transacao e obrigatorio quando sourceType e informado.");
    }

    void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new FinanceException("Valor da transacao financeira deve ser maior que zero.");
    }

    void validateImmutableUpdateFields(FinancialTransaction transaction, FinancialTransactionRequestDTO request) {
        if (transaction.getSenderType() != request.senderType || !transaction.getSenderId().equals(request.senderId)
                || transaction.getReceiverType() != request.receiverType || !transaction.getReceiverId().equals(request.receiverId))
            throw new FinanceException("Pagante e recebedor da transacao nao podem ser alterados.");
        if (transaction.getType() != request.type) throw new FinanceException("Tipo da transacao nao pode ser alterado.");
        if (transaction.getAmount().compareTo(request.amount) != 0) throw new FinanceException("Valor da transacao nao pode ser alterado.");
        if (request.status != null && request.status != transaction.getStatus()) throw new FinanceException("Status da transacao deve ser alterado pelo fluxo de liquidacao.");
    }
}
