package com.househost.finance.cashier.application.service;

import com.househost.finance.cashier.domain.model.Cashier;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
class CashierMovementValidationService {
    void validateWaitingStatus(FinancialTransactionStatus status) { if (status != FinancialTransactionStatus.WAITING) throw new FinanceException("Movimentacao da transacao nao esta aguardando liquidacao."); }
    void validateMovementAmount(BigDecimal movement, BigDecimal transaction) { if (movement.compareTo(transaction) != 0) throw new FinanceException("Valor da movimentacao aguardando difere do valor da transacao."); }
    void validatePositiveAmount(BigDecimal amount) { if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new FinanceException("Valor da movimentacao deve ser maior que zero."); }
    void validateSufficientBalance(Cashier cashier, BigDecimal amount) { if (cashier.getCashOnHand().compareTo(amount) < 0) throw new FinanceException("Caixa pagante nao possui saldo suficiente para liquidar a transacao."); }
}
