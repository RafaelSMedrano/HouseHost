package com.househost.finance.financialtransaction.application.service;

import com.househost.finance.financialtransaction.application.dto.InstallmentPlanTransactionRequestDTO;
import com.househost.finance.financialtransaction.domain.model.FinancialPartyType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionType;
import com.househost.shared.exception.FinanceException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinancialTransactionClassificationValidationTest {

    @Test
    void reservesPlanPurposeTypesForTheirOwningUseCases() {
        FinancialTransactionValidationService financialTransactionValidationService =
                new FinancialTransactionValidationService();

        assertDoesNotThrow(() -> financialTransactionValidationService.validateStandaloneCreationType(
                FinancialTransactionType.STANDARD
        ));
        for (FinancialTransactionType type : FinancialTransactionType.values()) {
            if (type != FinancialTransactionType.STANDARD) {
                assertThrows(
                        FinanceException.class,
                        () -> financialTransactionValidationService.validateStandaloneCreationType(type)
                );
            }
        }
    }

    @Test
    void reservesPlanAndInstallmentSourcesForInternalOwnershipFlows() {
        FinancialTransactionValidationService financialTransactionValidationService =
                new FinancialTransactionValidationService();
        InstallmentPlanValidationService installmentPlanValidationService =
                new InstallmentPlanValidationService();

        assertThrows(
                FinanceException.class,
                () -> financialTransactionValidationService.validateSource(
                        FinancialTransactionSourceType.PLAN,
                        10L
                )
        );
        assertThrows(
                FinanceException.class,
                () -> financialTransactionValidationService.validateSource(
                        FinancialTransactionSourceType.INSTALLMENT,
                        10L
                )
        );
        assertThrows(
                FinanceException.class,
                () -> installmentPlanValidationService.validate(
                        validInstallmentRequest(FinancialTransactionSourceType.PLAN)
                )
        );
    }

    private InstallmentPlanTransactionRequestDTO validInstallmentRequest(
            FinancialTransactionSourceType sourceType
    ) {
        InstallmentPlanTransactionRequestDTO installmentPlanTransactionRequestDTO =
                new InstallmentPlanTransactionRequestDTO();
        installmentPlanTransactionRequestDTO.senderType = FinancialPartyType.GUEST;
        installmentPlanTransactionRequestDTO.senderId = 20L;
        installmentPlanTransactionRequestDTO.receiverType = FinancialPartyType.CASHIER;
        installmentPlanTransactionRequestDTO.receiverId = 1L;
        installmentPlanTransactionRequestDTO.sourceType = sourceType;
        installmentPlanTransactionRequestDTO.sourceId = 10L;
        installmentPlanTransactionRequestDTO.amount = new BigDecimal("300.00");
        installmentPlanTransactionRequestDTO.description = "Pagamento parcelado";
        installmentPlanTransactionRequestDTO.installmentsQuantity = 3;
        installmentPlanTransactionRequestDTO.installmentDueDay = 18;
        return installmentPlanTransactionRequestDTO;
    }
}
