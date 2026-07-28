package com.househost.privacy.processing.application.service;

import com.househost.privacy.processing.application.port.out.DataProcessingOperationPersistencePort;
import com.househost.privacy.processing.domain.model.DataProcessingOperation;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataProcessingOperationCatalogServiceTest {

    @Mock
    private DataProcessingOperationPersistencePort operationRepository;

    private DataProcessingOperationCatalogService initializer;

    @BeforeEach
    void setUp() {
        initializer = new DataProcessingOperationCatalogService(operationRepository);
    }

    @Test
    void createsAllInitialOperationsWhenCatalogIsEmpty() {
        when(operationRepository.existsByOperationName(anyString())).thenReturn(false);
        ArgumentCaptor<DataProcessingOperation> operationCaptor =
                ArgumentCaptor.forClass(DataProcessingOperation.class);

        initializer.initializeCatalog();

        verify(operationRepository, org.mockito.Mockito.times(9)).save(operationCaptor.capture());
        List<String> names = operationCaptor.getAllValues().stream()
                .map(DataProcessingOperation::getOperationName)
                .toList();

        assertTrue(names.contains("Gestao de reservas"));
        assertTrue(names.contains("Gestao cadastral de hospedes"));
        assertTrue(names.contains("Gestao de hospedagem, check-in e check-out"));
        assertTrue(names.contains("Gestao financeira de hospedagens"));
        assertTrue(names.contains("Marketing por WhatsApp"));
        assertTrue(names.contains("Gestao de usuarios e controle de acesso"));
        assertTrue(names.contains("Governanca de fornecedores e operadores"));
        assertTrue(names.contains("Seguranca, auditoria e resposta a incidentes"));
        assertTrue(names.contains("Governanca de privacidade e bases legais"));

        DataProcessingOperation marketingOperation = operationCaptor.getAllValues().stream()
                .filter(operation -> operation.getOperationCode().equals(DataProcessingOperationCodes.WHATSAPP_MARKETING))
                .findFirst()
                .orElseThrow();
        assertEquals(DataProcessingOperationStatus.INACTIVE, marketingOperation.getStatus());
    }

    @Test
    void doesNotOverwriteExistingOperations() {
        when(operationRepository.existsByOperationName(anyString())).thenReturn(true);

        initializer.initializeCatalog();

        verify(operationRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void marksWhatsappMarketingAsInternationalTransfer() {
        DataProcessingOperation marketing = initializer.initialOperations().stream()
                .filter(operation -> operation.getOperationName().equals("Marketing por WhatsApp"))
                .findFirst()
                .orElseThrow();

        assertEquals(Boolean.TRUE, marketing.getInternationalTransfer());
        assertEquals("CONSENTIMENTO", marketing.getLegalBasis());
        assertEquals(DataProcessingOperationStatus.INACTIVE, marketing.getStatus());
    }

    @Test
    void deactivatesExistingActiveWhatsappMarketingWithoutDeletingIt() {
        DataProcessingOperation marketingOperation = initializer.initialOperations().stream()
                .filter(operation -> operation.getOperationCode().equals(DataProcessingOperationCodes.WHATSAPP_MARKETING))
                .findFirst()
                .orElseThrow();
        marketingOperation.changeStatus(DataProcessingOperationStatus.ACTIVE);
        when(operationRepository.existsByOperationName(anyString())).thenReturn(true);
        when(operationRepository.findByOperationCode(DataProcessingOperationCodes.WHATSAPP_MARKETING))
                .thenReturn(java.util.Optional.of(marketingOperation));

        initializer.initializeCatalog();

        assertEquals(DataProcessingOperationStatus.INACTIVE, marketingOperation.getStatus());
        verify(operationRepository).save(marketingOperation);
    }

    @Test
    void describesSecurityAndAuditAsAnActiveProcessingOperation() {
        DataProcessingOperation securityAuditOperation = initializer.initialOperations().stream()
                .filter(operation -> operation.getOperationCode().equals(
                        DataProcessingOperationCodes.SECURITY_AUDIT_MANAGEMENT
                ))
                .findFirst()
                .orElseThrow();

        assertEquals(DataProcessingOperationStatus.ACTIVE, securityAuditOperation.getStatus());
        assertTrue(securityAuditOperation.getPersonalDataCategories().contains("IP"));
        assertTrue(securityAuditOperation.getPurpose().contains("investigar incidentes"));
    }
}
