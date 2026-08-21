package com.househost.finance.cashier.adapter.out.persistence;

import com.househost.finance.cashier.adapter.out.persistence.entity.CashierEntryJpaEntity;
import com.househost.finance.cashier.adapter.out.persistence.entity.CashierExpenseJpaEntity;
import com.househost.finance.cashier.application.dto.CashierEntryResponseDTO;
import com.househost.finance.cashier.application.dto.CashierExpenseResponseDTO;
import com.househost.finance.cashier.domain.model.Cashier;
import com.househost.finance.cashier.domain.model.CashierEntry;
import com.househost.finance.cashier.domain.model.CashierExpense;
import com.househost.finance.cashier.domain.model.CashierStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CashierTemporalPersistenceMapperTest {

    @Test
    void roundTripsEntryDueAndSettlementDatesThroughJpaAndResponse() {
        LocalDate dueDate = LocalDate.of(2026, 9, 10);
        LocalDate settlementDate = LocalDate.of(2026, 9, 8);
        CashierEntry cashierEntry = new CashierEntry(
                cashier(),
                "Entrada",
                new BigDecimal("250.00"),
                dueDate,
                "FINANCIAL_TRANSACTION",
                null
        );
        cashierEntry.settle(settlementDate);

        CashierEntryJpaEntity cashierEntryJpaEntity = CashierEntryPersistenceMapper.toEntity(cashierEntry);
        CashierEntry restoredCashierEntry = CashierEntryPersistenceMapper.toDomain(cashierEntryJpaEntity);
        CashierEntryResponseDTO cashierEntryResponseDTO = new CashierEntryResponseDTO(restoredCashierEntry);

        assertEquals(dueDate, restoredCashierEntry.getDueDate());
        assertEquals(settlementDate, restoredCashierEntry.getSettlementDate());
        assertEquals(dueDate, cashierEntryResponseDTO.getDueDate());
        assertEquals(settlementDate, cashierEntryResponseDTO.getSettlementDate());
        assertEquals(dueDate, cashierEntryResponseDTO.getEntryDate());
    }

    @Test
    void roundTripsExpenseDueAndSettlementDatesThroughJpaAndResponse() {
        LocalDate dueDate = LocalDate.of(2026, 9, 10);
        LocalDate settlementDate = LocalDate.of(2026, 9, 12);
        CashierExpense cashierExpense = new CashierExpense(
                cashier(),
                "Saida",
                new BigDecimal("-250.00"),
                dueDate,
                "FINANCIAL_TRANSACTION",
                null
        );
        cashierExpense.settle(settlementDate);

        CashierExpenseJpaEntity cashierExpenseJpaEntity =
                CashierExpensePersistenceMapper.toEntity(cashierExpense);
        CashierExpense restoredCashierExpense =
                CashierExpensePersistenceMapper.toDomain(cashierExpenseJpaEntity);
        CashierExpenseResponseDTO cashierExpenseResponseDTO =
                new CashierExpenseResponseDTO(restoredCashierExpense);

        assertEquals(dueDate, restoredCashierExpense.getDueDate());
        assertEquals(settlementDate, restoredCashierExpense.getSettlementDate());
        assertEquals(dueDate, cashierExpenseResponseDTO.getDueDate());
        assertEquals(settlementDate, cashierExpenseResponseDTO.getSettlementDate());
        assertEquals(dueDate, cashierExpenseResponseDTO.getExpenseDate());
    }

    private Cashier cashier() {
        Cashier cashier = new Cashier(
                "Caixa principal",
                null,
                new BigDecimal("1000.00"),
                CashierStatus.OPEN
        );
        cashier.restorePersistenceState(1L, BigDecimal.ZERO, null, null);
        return cashier;
    }
}
