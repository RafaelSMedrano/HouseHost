package com.househost.privacy.legalbasis.adapter.out.integration;

import com.househost.audit.application.service.AuditEventService;
import com.househost.privacy.legalbasis.domain.model.LegalBasisAssessmentStatus;
import com.househost.privacy.legalbasis.domain.model.LegalBasisType;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PrivacyLegalBasisAuditAdapterTest {
    @Test
    void recordsOnlyLifecycleMetadataWithoutNarratives() {
        AuditEventService auditEventService = mock(AuditEventService.class);
        PrivacyLegalBasisAuditAdapter adapter = new PrivacyLegalBasisAuditAdapter(auditEventService);
        ProcessingLegalBasisAssessment assessment = new ProcessingLegalBasisAssessment(
                3L, "Finalidade confidencial", LegalBasisType.LEGITIMATE_INTEREST);
        assessment.prepareForCreation();
        assessment.restorePersistenceState(9L, LegalBasisAssessmentStatus.DRAFT, 1, null, null,
                null, null, null, assessment.getCreatedAt(), assessment.getUpdatedAt());

        adapter.record("LEGAL_BASIS_ASSESSMENT_CREATED", assessment);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> metadataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(auditEventService).recordForJwtActor(anyString(), anyString(), anyString(), anyLong(),
                metadataCaptor.capture());
        assertTrue(metadataCaptor.getValue().containsKey("processingOperationId"));
        assertFalse(metadataCaptor.getValue().containsKey("purpose"));
        assertFalse(metadataCaptor.getValue().toString().contains("Finalidade confidencial"));
    }
}
