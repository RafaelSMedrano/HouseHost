package com.househost.privacy.legalbasis.adapter.out.persistence.entity;

import com.househost.privacy.legalbasis.domain.model.LegalBasisAssessmentStatus;
import com.househost.privacy.legalbasis.domain.model.LegalBasisType;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessingLegalBasisAssessmentPersistenceMapperTest {
    @Test
    void preservesDomainStateOnRoundTrip() {
        ProcessingLegalBasisAssessment assessment = new ProcessingLegalBasisAssessment(
                4L, "Executar contrato", LegalBasisType.CONTRACT_OR_PRE_CONTRACT);
        assessment.updateDetails("Executar contrato", LegalBasisType.CONTRACT_OR_PRE_CONTRACT,
                "Justificativa", "Nome", "Necessidade", null, null, "Contrato", null, null, null,
                null, null, null, "Salvaguardas", null, false, null, null);
        assessment.prepareForCreation();
        assessment.restorePersistenceState(11L, LegalBasisAssessmentStatus.DRAFT, 3, 8L, null,
                null, null, null, assessment.getCreatedAt(), assessment.getUpdatedAt());

        ProcessingLegalBasisAssessment restored = ProcessingLegalBasisAssessmentPersistenceMapper.toDomain(
                ProcessingLegalBasisAssessmentPersistenceMapper.toEntity(assessment));

        assertEquals(11L, restored.getId());
        assertEquals(4L, restored.getProcessingOperationId());
        assertEquals(3, restored.getAssessmentVersion());
        assertEquals(8L, restored.getPreviousVersionId());
        assertEquals("Contrato", restored.getContractualContext());
    }
}
