package com.househost.finance.financialtransaction.architecture;

import com.househost.finance.financialtransaction.adapter.out.persistence.FinancialTransactionPlanPersistenceAdapter;
import com.househost.finance.financialtransaction.adapter.in.rest.FinancialTransactionPlanController;
import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionPlanUseCase;
import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionPlanReplacementUseCase;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionPlanPersistencePort;
import com.househost.finance.financialtransaction.application.dto.FinancialTransactionPlanMaterializationDTO;
import com.househost.booking.checking.application.dto.CheckInRequestDTO;
import com.househost.booking.checkout.application.dto.CheckOutRequestDTO;
import com.househost.finance.financialtransaction.application.service.FinancialTransactionPlanService;
import com.househost.finance.financialtransaction.application.service.FinancialTransactionPlanReplacementService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancialTransactionPlanArchitectureTest {

    private static final Path FINANCIAL_TRANSACTION_SOURCE_PATH = Path.of(
            "src/main/java/com/househost/finance/financialtransaction"
    );

    @Test
    void keepsPlanDomainIndependentAndPersistenceBehindItsPort() throws IOException {
        String financialTransactionPlanSource = Files.readString(
                FINANCIAL_TRANSACTION_SOURCE_PATH.resolve(
                        "domain/model/FinancialTransactionPlan.java"
                )
        );

        assertFalse(financialTransactionPlanSource.contains("jakarta.persistence"));
        assertFalse(financialTransactionPlanSource.contains("org.springframework"));
        assertFalse(financialTransactionPlanSource.contains(".adapter."));
        assertTrue(FinancialTransactionPlanPersistencePort.class.isAssignableFrom(
                FinancialTransactionPlanPersistenceAdapter.class
        ));
    }

    @Test
    void exposesPlanApplicationContractThroughInboundAdapter() {
        assertTrue(FinancialTransactionPlanUseCase.class.isAssignableFrom(
                FinancialTransactionPlanService.class
        ));
        assertTrue(FinancialTransactionPlanReplacementUseCase.class.isAssignableFrom(
                FinancialTransactionPlanReplacementService.class
        ));
        assertTrue(Files.exists(FINANCIAL_TRANSACTION_SOURCE_PATH.resolve(
                "adapter/in/rest/FinancialTransactionPlanController.java"
        )));
        assertTrue(Files.exists(FINANCIAL_TRANSACTION_SOURCE_PATH.resolve(
                "application/port/in/FinancialTransactionPlanUseCase.java"
        )));
        assertFalse(FinancialTransactionPlanController.class.getName().contains(
                ".application.service."
        ));
    }

    @Test
    void operationalContractsExposeChoicesWithoutProtectedFinancialIdentifiers()
            throws NoSuchFieldException {
        assertEquals(
                FinancialTransactionPlanMaterializationDTO.class,
                CheckInRequestDTO.class.getDeclaredField("paymentMaterialization").getType()
        );
        assertEquals(
                FinancialTransactionPlanMaterializationDTO.class,
                CheckOutRequestDTO.class.getDeclaredField("paymentMaterialization").getType()
        );
        assertThrows(
                NoSuchFieldException.class,
                () -> FinancialTransactionPlanMaterializationDTO.class
                        .getDeclaredField("planId")
        );
        assertThrows(
                NoSuchFieldException.class,
                () -> FinancialTransactionPlanMaterializationDTO.class
                        .getDeclaredField("scheduledFinancialTransactionId")
        );
        assertThrows(
                NoSuchFieldException.class,
                () -> FinancialTransactionPlanMaterializationDTO.class
                        .getDeclaredField("amount")
        );
    }
}
