package com.househost.finance.financialtransaction.architecture;

import com.househost.finance.financialtransaction.adapter.out.persistence.entity.FinancialTransactionJpaEntity;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionRequestDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionResponseDTO;
import com.househost.finance.financialtransaction.application.dto.InstallmentPlanTransactionRequestDTO;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinancialTransactionLegacyClassificationRemovalTest {

    @Test
    void removesLegacyDirectionalAmountsFromEveryTransactionContract() {
        List<Class<?>> financialTransactionContractClassList = List.of(
                FinancialTransaction.class,
                FinancialTransactionJpaEntity.class,
                FinancialTransactionRequestDTO.class,
                FinancialTransactionResponseDTO.class,
                InstallmentPlanTransactionRequestDTO.class
        );

        financialTransactionContractClassList.forEach(financialTransactionContractClass -> {
            assertThrows(
                    NoSuchFieldException.class,
                    () -> financialTransactionContractClass.getDeclaredField("entryAmount")
            );
            assertThrows(
                    NoSuchFieldException.class,
                    () -> financialTransactionContractClass.getDeclaredField("expenseAmount")
            );
        });
    }

    @Test
    void retainsTypeInCoreContractsWithTheApprovedVocabulary() {
        List<Class<?>> typedFinancialTransactionContractClassList = List.of(
                FinancialTransaction.class,
                FinancialTransactionJpaEntity.class,
                FinancialTransactionRequestDTO.class,
                FinancialTransactionResponseDTO.class
        );
        typedFinancialTransactionContractClassList.forEach(
                typedFinancialTransactionContractClass -> assertDoesNotThrow(
                        () -> typedFinancialTransactionContractClass.getDeclaredField("type")
                )
        );
        assertArrayEquals(
                new FinancialTransactionType[]{
                        FinancialTransactionType.STANDARD,
                        FinancialTransactionType.PLAN_DOWN_PAYMENT,
                        FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                        FinancialTransactionType.PLAN_CHECK_OUT_PAYMENT,
                        FinancialTransactionType.PLAN_TRANSACTION,
                        FinancialTransactionType.INSTALLMENT_PLAN_BLOCK,
                        FinancialTransactionType.INSTALLMENT_TRANSACTION
                },
                FinancialTransactionType.values()
        );
        assertDoesNotThrow(() -> FinancialTransactionSourceType.valueOf("PLAN"));
        assertDoesNotThrow(() -> FinancialTransactionSourceType.valueOf("INSTALLMENT"));
        assertThrows(IllegalArgumentException.class, () -> FinancialTransactionType.valueOf(
                "PLAN_SIGNAL_TRANSACTIONAL"
        ));
        assertThrows(IllegalArgumentException.class, () -> FinancialTransactionType.valueOf(
                "PLAN_TRANSACTIONAL"
        ));
        assertThrows(IllegalArgumentException.class, () -> FinancialTransactionType.valueOf(
                "INSTALLTMENT_PLAN_TRANSACTION"
        ));
    }
}
