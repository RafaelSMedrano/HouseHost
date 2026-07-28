package com.househost.finance.cashier.application.service;

import com.househost.finance.cashier.application.dto.CashierRequestDTO;
import com.househost.finance.cashier.application.dto.CashierUpdateRequestDTO;
import com.househost.finance.cashier.application.port.out.CashierEntryPersistencePort;
import com.househost.finance.cashier.application.port.out.CashierExpensePersistencePort;
import com.househost.finance.cashier.application.port.out.CashierPersistencePort;
import com.househost.finance.cashier.domain.model.Cashier;
import com.househost.shared.exception.FinanceException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
class CashierValidationService {
    private final CashierPersistencePort cashiers;
    private final CashierEntryPersistencePort entries;
    private final CashierExpensePersistencePort expenses;

    CashierValidationService(CashierPersistencePort cashiers, CashierEntryPersistencePort entries, CashierExpensePersistencePort expenses) {
        this.cashiers = cashiers; this.entries = entries; this.expenses = expenses;
    }

    void validateCreate(CashierRequestDTO request) {
        if (request == null) throw new FinanceException("Dados do caixa sao obrigatorios.");
        if (request.name == null || request.name.isBlank()) throw new FinanceException("Nome do caixa e obrigatorio.");
        if (request.openingBalance == null || request.openingBalance.compareTo(BigDecimal.ZERO) < 0) throw new FinanceException("Saldo inicial do caixa nao pode ser negativo.");
        if (cashiers.existsByName(request.name.trim())) throw new FinanceException("Nome do caixa ja esta cadastrado.");
    }

    void validateUpdate(Long id, CashierUpdateRequestDTO request) {
        if (request == null) throw new FinanceException("Dados do caixa sao obrigatorios.");
        if (request.name == null || request.name.isBlank()) throw new FinanceException("Nome do caixa e obrigatorio.");
        if (cashiers.existsByNameAndIdNot(request.name.trim(), id)) throw new FinanceException("Nome do caixa ja esta cadastrado.");
    }

    void validateCanDelete(Cashier cashier) {
        if (!entries.findByCashierId(cashier.getId()).isEmpty() || !expenses.findByCashierId(cashier.getId()).isEmpty()) throw new FinanceException("Caixa com historico financeiro nao pode ser removido. Inative-o.");
        if (cashier.getCashOnHand().compareTo(BigDecimal.ZERO) != 0 || cashier.getOnWaiting().compareTo(BigDecimal.ZERO) != 0
                || cashier.getExpectedInflow().compareTo(BigDecimal.ZERO) != 0 || cashier.getExpectedOutflow().compareTo(BigDecimal.ZERO) != 0
                || cashier.getTotalInflow().compareTo(BigDecimal.ZERO) != 0 || cashier.getTotalOutflow().compareTo(BigDecimal.ZERO) != 0)
            throw new FinanceException("Caixa com movimentacao financeira nao pode ser removido. Inative-o.");
    }
}
