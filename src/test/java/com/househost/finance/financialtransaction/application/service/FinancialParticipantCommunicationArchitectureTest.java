package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.cashier.application.port.in.CashierFinancialTransactionUseCase;
import com.househost.finance.cashier.application.service.CashierTransactionParticipantService;
import com.househost.finance.financialtransaction.adapter.out.integration.CashierFinancialPartyAdapter;
import com.househost.finance.financialtransaction.application.port.out.FinancialParty;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialParticipantCommunicationArchitectureTest {

    @Test
    void principalServicesUseOnlyTheCentralParticipantNotifierForExternalEffects() {
        List<Class<?>> principalServiceClassList = List.of(
                FinancialTransactionService.class,
                InstallmentPlanTransactionService.class,
                FinancialTransactionPlanService.class,
                FinancialTransactionPlanReplacementService.class
        );

        principalServiceClassList.forEach(principalServiceClass -> {
            List<Class<?>> fieldTypeList = Arrays.stream(principalServiceClass.getDeclaredFields())
                    .map(Field::getType)
                    .toList();
            long participantNotifierCount = fieldTypeList.stream()
                    .filter(FinancialParticipantNotifier.class::equals)
                    .count();

            assertEquals(1L, participantNotifierCount, principalServiceClass.getSimpleName());
            assertFalse(
                    fieldTypeList.stream().anyMatch(fieldType -> fieldType.getSimpleName().endsWith("Resolver")),
                    principalServiceClass.getSimpleName()
            );
            assertFalse(
                    fieldTypeList.stream()
                            .filter(fieldType -> !FinancialParticipantNotifier.class.equals(fieldType))
                            .anyMatch(fieldType -> fieldType.getSimpleName().endsWith("Notifier")),
                    principalServiceClass.getSimpleName()
            );
        });
    }

    @Test
    void centralParticipantNotifierOwnsBothSpecializedResolvers() {
        List<Class<?>> resolverTypeList = Arrays.stream(
                        FinancialParticipantNotifier.class.getDeclaredFields()
                )
                .map(Field::getType)
                .filter(fieldType -> fieldType.getSimpleName().endsWith("Resolver"))
                .toList();

        assertEquals(2, resolverTypeList.size());
        assertTrue(resolverTypeList.contains(FinancialPartyResolver.class));
        assertTrue(resolverTypeList.contains(FinancialTransactionSourceResolver.class));
    }

    @Test
    void cashierParticipantCrossesAnAdapterAndCashierOwnedUseCase() {
        assertTrue(FinancialParty.class.isAssignableFrom(CashierFinancialPartyAdapter.class));
        assertTrue(
                CashierFinancialTransactionUseCase.class.isAssignableFrom(
                        CashierTransactionParticipantService.class
                )
        );
        assertFalse(FinancialParty.class.isAssignableFrom(CashierTransactionParticipantService.class));
        assertTrue(Arrays.stream(CashierFinancialPartyAdapter.class.getDeclaredFields())
                .map(Field::getType)
                .anyMatch(CashierFinancialTransactionUseCase.class::equals));
    }
}
