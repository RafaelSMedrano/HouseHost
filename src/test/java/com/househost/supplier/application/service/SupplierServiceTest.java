package com.househost.supplier.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.househost.supplier.application.dto.*;
import com.househost.supplier.application.port.out.*;
import com.househost.supplier.domain.exception.SupplierException;
import com.househost.supplier.domain.model.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SupplierServiceTest {
    private final SupplierPersistencePort persistencePort = mock(SupplierPersistencePort.class);
    private final SupplierAuditPort auditPort = mock(SupplierAuditPort.class);
    private final SupplierReviewerPort reviewerPort = mock(SupplierReviewerPort.class);
    private SupplierService supplierService;

    @BeforeEach
    void setUp() {
        supplierService = new SupplierService(persistencePort, auditPort, reviewerPort,
                new SupplierValidationService());
        when(persistencePort.save(any())).thenAnswer(invocation -> {
            Supplier supplier = invocation.getArgument(0);
            supplier.prepareForSave(java.time.LocalDateTime.now());
            return supplier;
        });
    }

    @Test
    void createsSupplierWithDifferentRelationshipRolesAndAuditsAfterSave() {
        SupplierRequestDTO request = request();
        request.relationshipList = List.of(relationship(SupplierDataRole.OPERATOR),
                relationship(SupplierDataRole.INDEPENDENT_CONTROLLER));
        SupplierDetailResponseDTO response = supplierService.create(request);
        assertEquals(2, response.relationshipList.size());
        assertEquals(SupplierDataRole.OPERATOR, response.relationshipList.get(0).role);
        assertEquals(SupplierDataRole.INDEPENDENT_CONTROLLER, response.relationshipList.get(1).role);
        verify(persistencePort).save(any());
        verify(auditPort).record(eq("SUPPLIER_CREATED"), nullable(Long.class), anyMap());
    }

    @Test
    void rejectsDuplicateNormalizedOfficialNameBeforeSave() {
        when(persistencePort.existsByNormalizedOfficialName("empresa exemplo", null)).thenReturn(true);
        assertThrows(SupplierException.class, () -> supplierService.create(request()));
        verify(persistencePort, never()).save(any());
        verifyNoInteractions(auditPort);
    }

    private SupplierRequestDTO request() {
        SupplierRequestDTO request = new SupplierRequestDTO();
        request.officialName = "Empresa Exemplo";
        request.countryOfEstablishment = "Brasil";
        request.status = SupplierStatus.ACTIVE;
        request.relationshipList = List.of(relationship(SupplierDataRole.OPERATOR));
        return request;
    }

    private SupplierRelationshipRequestDTO relationship(SupplierDataRole role) {
        SupplierRelationshipRequestDTO request = new SupplierRelationshipRequestDTO();
        request.serviceName = "Infraestrutura " + role;
        request.purpose = "Hospedar o sistema";
        request.personalDataCategories = "Nome e contato";
        request.dataSubjectCategories = "Hospedes";
        request.processingActions = "Armazenamento";
        request.role = role;
        request.roleAssessment = "Papel avaliado conforme o servico";
        request.storageLocations = "Brasil";
        request.retentionCriteria = "Durante o contrato";
        request.deletionOrReturnProcedure = "Excluir ao termino";
        request.securityMeasures = "Criptografia e acesso restrito";
        request.contractStatus = SupplierContractStatus.ACTIVE;
        request.responsibilitySummary = "Responsabilidades contratuais";
        request.riskLevel = SupplierRiskLevel.MEDIUM;
        request.governanceStatus = SupplierGovernanceStatus.DRAFT;
        return request;
    }
}
