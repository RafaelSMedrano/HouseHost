package com.househost.privacy.legalbasis.application.service;

import com.househost.privacy.legalbasis.domain.model.LegalBasisAssessmentStatus;
import com.househost.privacy.legalbasis.domain.model.LegalBasisReadiness;
import com.househost.privacy.legalbasis.domain.model.LegalBasisType;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegalBasisAssessmentReadinessServiceTest {
    private final LegalBasisAssessmentReadinessService service = new LegalBasisAssessmentReadinessService();

    @Test
    void latestRevisionDeterminesReadiness() {
        ProcessingLegalBasisAssessment approved = assessment(1L, 1, LegalBasisAssessmentStatus.APPROVED);
        ProcessingLegalBasisAssessment draftRevision = assessment(2L, 2, LegalBasisAssessmentStatus.DRAFT);
        assertEquals(LegalBasisReadiness.DRAFT, service.readiness(List.of(approved, draftRevision)));
    }

    @Test
    void emptyInventoryIsNotAssessed() {
        assertEquals(LegalBasisReadiness.NOT_ASSESSED, service.readiness(List.of()));
    }

    private ProcessingLegalBasisAssessment assessment(Long id, int version, LegalBasisAssessmentStatus status) {
        ProcessingLegalBasisAssessment assessment = new ProcessingLegalBasisAssessment(
                1L, "Finalidade", LegalBasisType.CONTRACT_OR_PRE_CONTRACT);
        assessment.prepareForCreation();
        assessment.restorePersistenceState(id, status, version, version > 1 ? 1L : null, null,
                null, null, null, assessment.getCreatedAt(), assessment.getUpdatedAt());
        return assessment;
    }
}
