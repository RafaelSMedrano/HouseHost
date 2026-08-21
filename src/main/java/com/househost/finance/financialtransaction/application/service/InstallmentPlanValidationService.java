package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.dto.InstallmentPlanTransactionRequestDTO;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
class InstallmentPlanValidationService {

    void validate(InstallmentPlanTransactionRequestDTO request) {
        if (request == null
                || request.senderType == null
                || request.senderId == null
                || request.receiverType == null
                || request.receiverId == null) {
            throw new FinanceException("Participantes do plano parcelado sao obrigatorios.");
        }
        if (request.amount == null || request.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new FinanceException("Valor do plano parcelado deve ser maior que zero.");
        }
        if (request.installmentsQuantity == null
                || request.installmentsQuantity < 2
                || request.installmentsQuantity > 12) {
            throw new FinanceException("Quantidade de parcelas deve estar entre 2 e 12.");
        }
        if (request.installmentDueDay == null
                || request.installmentDueDay < 1
                || request.installmentDueDay > 31) {
            throw new FinanceException("Dia mensal de vencimento deve estar entre 1 e 31.");
        }
        if (request.description == null || request.description.isBlank()) {
            throw new FinanceException("Descricao do plano parcelado e obrigatoria.");
        }
        if ((request.sourceType == null) != (request.sourceId == null)) {
            throw new FinanceException("Tipo e identificador da origem devem ser informados juntos.");
        }
        if (request.sourceType == FinancialTransactionSourceType.PLAN
                || request.sourceType == FinancialTransactionSourceType.INSTALLMENT) {
            throw new FinanceException("Origem de propriedade financeira so pode ser atribuida pelo fluxo interno.");
        }
        if (request.senderType == request.receiverType && request.senderId.equals(request.receiverId)) {
            throw new FinanceException("Pagante e recebedor devem ser diferentes.");
        }
    }
}
