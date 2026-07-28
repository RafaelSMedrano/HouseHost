package com.househost.privacy.application.service;

import com.househost.privacy.application.dto.DataProcessingOperationResponseDTO;
import com.househost.privacy.legalbasis.application.dto.DataProcessingOperationLegalBasisSummaryDTO;
import com.househost.privacy.legalbasis.application.records.LegalBasisAssessmentOverviewRecord;
import com.househost.privacy.legalbasis.application.service.LegalBasisAssessmentQueryService;
import com.househost.privacy.legalbasis.domain.model.LegalBasisReadiness;
import com.househost.privacy.legalbasis.domain.model.LegalBasisType;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import com.househost.privacy.processing.application.dto.ProcessingOperationResponseDTO;
import com.househost.privacy.processing.application.port.in.DataProcessingOperationReviewUseCase;
import com.househost.privacy.processing.application.port.in.DataProcessingOperationUseCase;
import com.househost.privacy.processing.domain.model.DataProcessingOperation;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataProcessingOperationGovernanceServiceTest {

    @Mock
    private DataProcessingOperationUseCase operationUseCase;

    @Mock
    private DataProcessingOperationReviewUseCase reviewUseCase;

    @Mock
    private LegalBasisAssessmentQueryService legalBasisAssessmentQueryService;

    private DataProcessingOperationGovernanceService service;

    @BeforeEach
    void setUp() {
        service = new DataProcessingOperationGovernanceService(
                operationUseCase,
                reviewUseCase,
                legalBasisAssessmentQueryService
        );
    }

    @Test
    void composesProcessingDataAndLegalBasisReadinessAtParentBoundary() {
        ProcessingOperationResponseDTO processingResponse = processingResponse();
        ProcessingLegalBasisAssessment assessment = new ProcessingLegalBasisAssessment(
                7L,
                "Administrar reservas",
                LegalBasisType.CONTRACT_OR_PRE_CONTRACT
        );
        when(operationUseCase.findById(7L)).thenReturn(processingResponse);
        when(legalBasisAssessmentQueryService.findOverviewByOperationId(7L)).thenReturn(
                new LegalBasisAssessmentOverviewRecord(
                        LegalBasisReadiness.DRAFT,
                        List.of(new DataProcessingOperationLegalBasisSummaryDTO(assessment, true))
                )
        );

        DataProcessingOperationResponseDTO response = service.findById(7L);

        assertEquals(7L, response.getId());
        assertEquals("BOOKING_MANAGEMENT", response.getOperationCode());
        assertEquals(LegalBasisReadiness.DRAFT, response.getLegalBasisReadiness());
        assertEquals(1, response.getLegalBasisAssessmentList().size());
        verify(legalBasisAssessmentQueryService).findOverviewByOperationId(7L);
    }

    private ProcessingOperationResponseDTO processingResponse() {
        DataProcessingOperation operation = new DataProcessingOperation(
                "BOOKING_MANAGEMENT",
                "Gestao de reservas",
                "Administracao da reserva.",
                "Administrar hospedagens.",
                "EXECUCAO_DE_CONTRATO",
                "Hospedes",
                "Dados cadastrais",
                "Site publico",
                "Coleta e armazenamento",
                "Administracao",
                "Infraestrutura",
                false,
                "Prazo contratual",
                "Exclusao",
                "Controle de acesso",
                "Administracao",
                "HouseHost"
        );
        operation.restorePersistenceState(
                7L,
                DataProcessingOperationStatus.ACTIVE,
                null,
                null,
                null,
                null
        );
        return new ProcessingOperationResponseDTO(operation);
    }
}
