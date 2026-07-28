package com.househost.privacy.processing.application.service;

import com.househost.privacy.processing.application.dto.DataProcessingOperationRequestDTO;
import com.househost.privacy.processing.application.dto.ProcessingOperationResponseDTO;
import com.househost.privacy.processing.application.port.out.DataProcessingOperationPersistencePort;
import com.househost.privacy.processing.application.records.ProcessingOperationRecord;
import com.househost.privacy.processing.domain.model.DataProcessingOperation;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import com.househost.shared.exception.PrivacyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataProcessingOperationServiceTest {

    @Mock
    private DataProcessingOperationPersistencePort operationRepository;

    private DataProcessingOperationService service;

    @BeforeEach
    void setUp() {
        service = new DataProcessingOperationService(
                operationRepository,
                new DataProcessingOperationValidationService()
        );
    }

    @Test
    void createsActiveOperation() {
        DataProcessingOperationRequestDTO request = validRequest();
        when(operationRepository.save(any(DataProcessingOperation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProcessingOperationResponseDTO response = service.create(request);

        assertEquals("Gestao de reservas", response.getOperationName());
        assertEquals(DataProcessingOperationStatus.ACTIVE, response.getStatus());
        verify(operationRepository).existsByOperationName("Gestao de reservas");
    }

    @Test
    void rejectsDuplicatedOperationName() {
        DataProcessingOperationRequestDTO request = validRequest();
        when(operationRepository.existsByOperationName("Gestao de reservas")).thenReturn(true);

        PrivacyException exception = assertThrows(PrivacyException.class, () -> service.create(request));

        assertEquals("Ja existe uma operacao de tratamento com esse nome.", exception.getMessage());
    }

    @Test
    void filtersOperationsByStatus() {
        DataProcessingOperation operation = operation();
        when(operationRepository.findAllByStatusOrderedByName(DataProcessingOperationStatus.ACTIVE))
                .thenReturn(List.of(operation));

        List<ProcessingOperationResponseDTO> responseList =
                service.findAll(DataProcessingOperationStatus.ACTIVE);

        assertEquals(1, responseList.size());
        assertEquals(DataProcessingOperationStatus.ACTIVE, responseList.getFirst().getStatus());
    }

    @Test
    void changesStatusWithoutDeletingOperation() {
        DataProcessingOperation operation = operation();
        when(operationRepository.findById(1L)).thenReturn(Optional.of(operation));
        when(operationRepository.save(operation)).thenReturn(operation);

        ProcessingOperationResponseDTO response =
                service.changeStatus(1L, DataProcessingOperationStatus.INACTIVE);

        assertEquals(DataProcessingOperationStatus.INACTIVE, response.getStatus());
        verify(operationRepository).save(operation);
    }

    @Test
    void exposesMinimumOperationRecordForDirectServiceCollaboration() {
        DataProcessingOperation operation = operation();
        operation.restorePersistenceState(
                1L,
                DataProcessingOperationStatus.ACTIVE,
                null,
                null,
                null,
                null
        );
        when(operationRepository.findById(1L)).thenReturn(Optional.of(operation));

        ProcessingOperationRecord processingOperationRecord = service.findOperationRecordById(1L);

        assertEquals(1L, processingOperationRecord.operationId());
        assertEquals(operation.getOperationCode(), processingOperationRecord.operationCode());
        assertEquals(DataProcessingOperationStatus.ACTIVE, processingOperationRecord.status());
    }

    private DataProcessingOperationRequestDTO validRequest() {
        DataProcessingOperationRequestDTO request = new DataProcessingOperationRequestDTO();
        request.operationName = "Gestao de reservas";
        request.description = "Recebimento e administracao de reservas.";
        request.purpose = "Administrar hospedagens.";
        request.legalBasis = "EXECUCAO_DE_CONTRATO";
        request.dataSubjectCategories = "Hospedes";
        request.personalDataCategories = "Nome, telefone e e-mail";
        request.dataSource = "Site publico e painel administrativo";
        request.processingActions = "Coleta, armazenamento, consulta e atualizacao";
        request.internalAccessRoles = "Administracao e recepcao";
        request.externalRecipients = "Provedores de infraestrutura";
        request.internationalTransfer = false;
        request.retentionPeriod = "Conforme politica de retencao";
        request.deletionMethod = "Exclusao ou anonimizacao";
        request.securityMeasures = "Autenticacao e controle de acesso";
        request.responsibleArea = "Administracao";
        request.systemName = "HouseHost";
        return request;
    }

    private DataProcessingOperation operation() {
        DataProcessingOperationRequestDTO request = validRequest();
        return new DataProcessingOperation(
                request.operationName,
                request.description,
                request.purpose,
                request.legalBasis,
                request.dataSubjectCategories,
                request.personalDataCategories,
                request.dataSource,
                request.processingActions,
                request.internalAccessRoles,
                request.externalRecipients,
                request.internationalTransfer,
                request.retentionPeriod,
                request.deletionMethod,
                request.securityMeasures,
                request.responsibleArea,
                request.systemName
        );
    }
}
