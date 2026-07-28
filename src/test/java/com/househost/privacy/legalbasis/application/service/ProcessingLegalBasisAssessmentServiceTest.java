package com.househost.privacy.legalbasis.application.service;

import com.househost.privacy.legalbasis.application.dto.ProcessingLegalBasisAssessmentRequestDTO;
import com.househost.privacy.legalbasis.application.port.out.PrivacyLegalBasisAuditPort;
import com.househost.privacy.legalbasis.application.port.out.LegalBasisReviewerPort;
import com.househost.privacy.legalbasis.application.port.out.ProcessingLegalBasisAssessmentPersistencePort;
import com.househost.privacy.legalbasis.domain.model.LegalBasisAssessmentStatus;
import com.househost.privacy.legalbasis.domain.model.LegalBasisType;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import com.househost.privacy.processing.application.records.ProcessingOperationRecord;
import com.househost.privacy.processing.application.service.DataProcessingOperationService;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessingLegalBasisAssessmentServiceTest {

    @Test
    void createsDraftUsingDirectMinimumProcessingServiceCapability() {
        ProcessingLegalBasisAssessmentPersistencePort persistencePort =
                mock(ProcessingLegalBasisAssessmentPersistencePort.class);
        DataProcessingOperationService processingOperationService =
                mock(DataProcessingOperationService.class);
        when(processingOperationService.findOperationRecordById(3L)).thenReturn(
                new ProcessingOperationRecord(
                        3L,
                        DataProcessingOperationCodes.BOOKING_MANAGEMENT,
                        DataProcessingOperationStatus.ACTIVE
                )
        );
        when(persistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        PrivacyLegalBasisAuditPort auditPort = mock(PrivacyLegalBasisAuditPort.class);
        ProcessingLegalBasisAssessmentValidationService validationService =
                mock(ProcessingLegalBasisAssessmentValidationService.class);
        ProcessingLegalBasisAssessmentService service = new ProcessingLegalBasisAssessmentService(
                persistencePort,
                processingOperationService,
                email -> 7L,
                auditPort,
                validationService,
                new LegalBasisAssessmentReadinessService()
        );
        ProcessingLegalBasisAssessmentRequestDTO request =
                new ProcessingLegalBasisAssessmentRequestDTO();
        request.purpose = "Administrar reserva";
        request.legalBasis = LegalBasisType.CONTRACT_OR_PRE_CONTRACT;

        service.createDraft(3L, request);

        verify(processingOperationService).findOperationRecordById(3L);
        verify(auditPort).record(
                org.mockito.ArgumentMatchers.eq("LEGAL_BASIS_ASSESSMENT_CREATED"),
                any(ProcessingLegalBasisAssessment.class)
        );
    }

    @Test
    void approvingRevisionSupersedesPreviousApprovedVersion() {
        ProcessingLegalBasisAssessment previous = approved(1L, 1, null);
        ProcessingLegalBasisAssessment revision = underReview(2L, 2, 1L);
        ProcessingLegalBasisAssessmentPersistencePort persistencePort =
                mock(ProcessingLegalBasisAssessmentPersistencePort.class);
        when(persistencePort.findById(2L)).thenReturn(Optional.of(revision));
        when(persistencePort.findById(1L)).thenReturn(Optional.of(previous));
        when(persistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        PrivacyLegalBasisAuditPort auditPort = mock(PrivacyLegalBasisAuditPort.class);
        ProcessingLegalBasisAssessmentValidationService validationService =
                mock(ProcessingLegalBasisAssessmentValidationService.class);
        DataProcessingOperationService processingOperationService =
                mock(DataProcessingOperationService.class);
        LegalBasisReviewerPort reviewerPort = email -> 7L;
        ProcessingLegalBasisAssessmentService service = new ProcessingLegalBasisAssessmentService(
                persistencePort, processingOperationService, reviewerPort, auditPort, validationService,
                new LegalBasisAssessmentReadinessService());

        service.approve(2L, "admin@househost.test");

        assertEquals(LegalBasisAssessmentStatus.APPROVED, revision.getStatus());
        assertEquals(7L, revision.getReviewedByUserId());
        assertEquals(LegalBasisAssessmentStatus.SUPERSEDED, previous.getStatus());
        verify(auditPort).record("LEGAL_BASIS_ASSESSMENT_APPROVED", revision);
        verify(auditPort).record("LEGAL_BASIS_ASSESSMENT_SUPERSEDED", previous);
    }

    private ProcessingLegalBasisAssessment approved(Long id, int version, Long previousVersionId) {
        ProcessingLegalBasisAssessment assessment = draft(id, version, previousVersionId);
        assessment.submit();
        assessment.approve(4L);
        return assessment;
    }

    private ProcessingLegalBasisAssessment underReview(Long id, int version, Long previousVersionId) {
        ProcessingLegalBasisAssessment assessment = draft(id, version, previousVersionId);
        assessment.submit();
        return assessment;
    }

    private ProcessingLegalBasisAssessment draft(Long id, int version, Long previousVersionId) {
        ProcessingLegalBasisAssessment assessment = new ProcessingLegalBasisAssessment(
                3L, "Executar contrato", LegalBasisType.CONTRACT_OR_PRE_CONTRACT);
        assessment.updateDetails("Executar contrato", LegalBasisType.CONTRACT_OR_PRE_CONTRACT,
                "Justificativa", "Nome", "Necessidade", null, null, "Contrato", null, null, null,
                null, null, null, "Salvaguardas", null, false, null, null);
        assessment.prepareForCreation();
        assessment.restorePersistenceState(id, LegalBasisAssessmentStatus.DRAFT, version, previousVersionId,
                null, null, null, null, assessment.getCreatedAt(), assessment.getUpdatedAt());
        return assessment;
    }
}
