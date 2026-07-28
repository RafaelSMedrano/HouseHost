package com.househost.privacy.legalbasis.application.service;

import com.househost.privacy.legalbasis.application.port.out.ProcessingLegalBasisAssessmentPersistencePort;
import com.househost.privacy.processing.application.records.ProcessingOperationRecord;
import com.househost.privacy.processing.application.service.DataProcessingOperationService;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessingLegalBasisAssessmentCatalogServiceTest {
    @Test
    void seedsSeparateDraftCandidatesAndSkipsMarketing() {
        DataProcessingOperationService processingOperationService =
                mock(DataProcessingOperationService.class);
        ProcessingLegalBasisAssessmentPersistencePort assessmentPort =
                mock(ProcessingLegalBasisAssessmentPersistencePort.class);
        when(processingOperationService.findAllOperationRecords()).thenReturn(List.of(
                operationRecord(1L, DataProcessingOperationCodes.STAY_MANAGEMENT),
                operationRecord(2L, DataProcessingOperationCodes.WHATSAPP_MARKETING)
        ));
        when(assessmentPort.existsByOperationIdAndPurpose(anyLong(), anyString())).thenReturn(false);
        when(assessmentPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ProcessingLegalBasisAssessmentCatalogService service =
                new ProcessingLegalBasisAssessmentCatalogService(
                        processingOperationService,
                        assessmentPort
                );
        ArgumentCaptor<ProcessingLegalBasisAssessment> assessmentCaptor =
                ArgumentCaptor.forClass(ProcessingLegalBasisAssessment.class);

        service.initializeCatalog();

        verify(assessmentPort, org.mockito.Mockito.times(2)).save(assessmentCaptor.capture());
        assertEquals(2, assessmentCaptor.getAllValues().stream()
                .map(ProcessingLegalBasisAssessment::getLegalBasis).distinct().count());
        assertEquals(0, assessmentCaptor.getAllValues().stream()
                .filter(assessment -> assessment.getProcessingOperationId().equals(2L))
                .count());
    }

    @Test
    void repeatedStartupDoesNotOverwriteExistingAssessment() {
        DataProcessingOperationService processingOperationService =
                mock(DataProcessingOperationService.class);
        ProcessingLegalBasisAssessmentPersistencePort assessmentPort =
                mock(ProcessingLegalBasisAssessmentPersistencePort.class);
        when(processingOperationService.findAllOperationRecords()).thenReturn(List.of(
                operationRecord(1L, DataProcessingOperationCodes.BOOKING_MANAGEMENT)
        ));
        when(assessmentPort.existsByOperationIdAndPurpose(anyLong(), anyString())).thenReturn(true);
        ProcessingLegalBasisAssessmentCatalogService service =
                new ProcessingLegalBasisAssessmentCatalogService(
                        processingOperationService,
                        assessmentPort
                );

        service.initializeCatalog();

        verify(assessmentPort, never()).save(any());
    }

    private ProcessingOperationRecord operationRecord(Long id, String code) {
        return new ProcessingOperationRecord(id, code, DataProcessingOperationStatus.ACTIVE);
    }
}
