package com.househost.finance.financialtransaction.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.househost.finance.financialtransaction.adapter.out.persistence.entity.FinancialTransactionJpaEntity;
import com.househost.finance.financialtransaction.adapter.out.persistence.entity.InstallmentPlanTransactionJpaEntity;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionRequestDTO;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionResponseDTO;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionMethod;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import com.househost.finance.financialtransaction.domain.model.InstallmentPlanTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinancialTransactionPersistenceMapperTest {

    @Test
    void roundTripsEveryAuthoritativeTypeThroughJpaAndApiContracts() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        for (FinancialTransactionType type : FinancialTransactionType.values()) {
            FinancialTransaction transaction = transaction(type);
            FinancialTransactionJpaEntity financialTransactionJpaEntity =
                    FinancialTransactionPersistenceMapper.toEntity(transaction);
            FinancialTransaction restoredTransaction =
                    FinancialTransactionPersistenceMapper.toDomain(financialTransactionJpaEntity);
            String requestJson = "{\"type\":\"" + type.name() + "\"}";
            FinancialTransactionRequestDTO financialTransactionRequestDTO = objectMapper.readValue(
                    requestJson,
                    FinancialTransactionRequestDTO.class
            );
            String responseJson = objectMapper.writeValueAsString(
                    new FinancialTransactionResponseDTO(restoredTransaction)
            );

            assertEquals(type, financialTransactionJpaEntity.getType());
            assertEquals(type, restoredTransaction.getType());
            assertEquals(type, financialTransactionRequestDTO.type);
            assertEquals(type.name(), objectMapper.readTree(responseJson).get("type").asText());
        }
    }

    @Test
    void preservesDirectBlockPurposeAndKeepsInternalInstallmentOwnership() {
        List<FinancialTransactionType> directTypeList = List.of(
                FinancialTransactionType.PLAN_DOWN_PAYMENT,
                FinancialTransactionType.PLAN_CHECK_IN_PAYMENT,
                FinancialTransactionType.PLAN_CHECK_OUT_PAYMENT,
                FinancialTransactionType.PLAN_TRANSACTION,
                FinancialTransactionType.INSTALLMENT_PLAN_BLOCK
        );

        directTypeList.forEach(directType -> {
            InstallmentPlanTransaction installmentPlanTransaction = installmentPlanTransaction(directType);
            installmentPlanTransaction.restorePersistenceState(100L, null, null);
            installmentPlanTransaction.setSource(FinancialTransactionSourceType.PLAN, 50L);

            InstallmentPlanTransactionJpaEntity installmentPlanTransactionJpaEntity =
                    (InstallmentPlanTransactionJpaEntity) FinancialTransactionPersistenceMapper.toEntity(
                            installmentPlanTransaction
                    );
            InstallmentPlanTransaction restoredInstallmentPlanTransaction =
                    (InstallmentPlanTransaction) FinancialTransactionPersistenceMapper.toDomain(
                            installmentPlanTransactionJpaEntity
                    );

            assertEquals(directType, installmentPlanTransactionJpaEntity.getType());
            assertEquals(directType, restoredInstallmentPlanTransaction.getType());
            assertEquals(FinancialTransactionSourceType.PLAN, restoredInstallmentPlanTransaction.getSourceType());
            assertEquals(50L, restoredInstallmentPlanTransaction.getSourceId());
            assertInternalInstallmentSources(
                    installmentPlanTransactionJpaEntity,
                    restoredInstallmentPlanTransaction
            );
        });
    }

    @Test
    void rejectsNonDirectTypesForInstallmentBlocks() {
        assertThrows(
                IllegalArgumentException.class,
                () -> installmentPlanTransaction(FinancialTransactionType.STANDARD)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> installmentPlanTransaction(FinancialTransactionType.INSTALLMENT_TRANSACTION)
        );
    }

    @Test
    void preparesInternalSourceWhenPersistenceAssignsTheBlockId() {
        InstallmentPlanTransactionJpaEntity installmentPlanTransactionJpaEntity =
                (InstallmentPlanTransactionJpaEntity) FinancialTransactionPersistenceMapper.toEntity(
                        installmentPlanTransaction(FinancialTransactionType.INSTALLMENT_PLAN_BLOCK)
                );
        installmentPlanTransactionJpaEntity.restorePersistenceState(100L, null, null, null, null);

        installmentPlanTransactionJpaEntity.getInstallments().forEach(
                installmentTransactionJpaEntity -> installmentTransactionJpaEntity.synchronizeSourceWithPlan()
        );

        installmentPlanTransactionJpaEntity.getInstallments().forEach(installmentTransactionJpaEntity -> {
            assertEquals(FinancialTransactionSourceType.INSTALLMENT, installmentTransactionJpaEntity.getSourceType());
            assertEquals(100L, installmentTransactionJpaEntity.getSourceId());
        });
    }

    @Test
    void restoresHistoricalSingleInstallmentBlockWithoutPermittingNewOnes() {
        InstallmentPlanTransactionJpaEntity historicalInstallmentPlanTransactionJpaEntity =
                new InstallmentPlanTransactionJpaEntity(
                        FinancialPartyType.GUEST,
                        20L,
                        FinancialPartyType.CASHIER,
                        1L,
                        new BigDecimal("100.00"),
                        LocalDate.of(2026, 8, 18),
                        "Bloco historico",
                        FinancialTransactionMethod.CREDIT_CARD,
                        1,
                        18,
                        FinancialTransactionType.INSTALLMENT_PLAN_BLOCK,
                        FinancialTransactionStatus.WAITING
                );

        InstallmentPlanTransaction restoredInstallmentPlanTransaction =
                (InstallmentPlanTransaction) FinancialTransactionPersistenceMapper.toDomain(
                        historicalInstallmentPlanTransactionJpaEntity
                );

        assertEquals(1, restoredInstallmentPlanTransaction.getInstallmentsQuantity());
        assertThrows(
                IllegalArgumentException.class,
                () -> new InstallmentPlanTransaction(
                        FinancialPartyType.GUEST,
                        20L,
                        FinancialPartyType.CASHIER,
                        1L,
                        new BigDecimal("100.00"),
                        LocalDate.of(2026, 8, 18),
                        "Novo bloco invalido",
                        FinancialTransactionMethod.CREDIT_CARD,
                        1,
                        18
                )
        );
    }

    private void assertInternalInstallmentSources(
            InstallmentPlanTransactionJpaEntity installmentPlanTransactionJpaEntity,
            InstallmentPlanTransaction installmentPlanTransaction
    ) {
        installmentPlanTransactionJpaEntity.getInstallments().forEach(installmentTransactionJpaEntity -> {
            assertEquals(
                    FinancialTransactionType.INSTALLMENT_TRANSACTION,
                    installmentTransactionJpaEntity.getType()
            );
            assertEquals(FinancialTransactionSourceType.INSTALLMENT, installmentTransactionJpaEntity.getSourceType());
            assertEquals(100L, installmentTransactionJpaEntity.getSourceId());
        });
        installmentPlanTransaction.getInstallments().forEach(installmentTransaction -> {
            assertEquals(FinancialTransactionType.INSTALLMENT_TRANSACTION, installmentTransaction.getType());
            assertEquals(FinancialTransactionSourceType.INSTALLMENT, installmentTransaction.getSourceType());
            assertEquals(100L, installmentTransaction.getSourceId());
        });
    }

    private FinancialTransaction transaction(FinancialTransactionType type) {
        return new FinancialTransaction(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L,
                type,
                new BigDecimal("100.00"),
                LocalDate.of(2026, 8, 18),
                "Transacao",
                FinancialTransactionMethod.PIX
        );
    }

    private InstallmentPlanTransaction installmentPlanTransaction(FinancialTransactionType type) {
        return new InstallmentPlanTransaction(
                FinancialPartyType.GUEST,
                20L,
                FinancialPartyType.CASHIER,
                1L,
                new BigDecimal("300.00"),
                LocalDate.of(2026, 8, 18),
                "Pagamento parcelado",
                FinancialTransactionMethod.CREDIT_CARD,
                3,
                18,
                type
        );
    }
}
