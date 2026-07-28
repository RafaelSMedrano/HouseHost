package com.househost.privacy.legalbasis.domain.model;

import com.househost.shared.exception.PrivacyException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProcessingLegalBasisAssessmentTest {
    @Test
    void approvedAssessmentIsImmutableAndCreatesLinkedDraftRevision() {
        ProcessingLegalBasisAssessment assessment = completeContractAssessment();
        assessment.prepareForCreation();
        assessment.restorePersistenceState(10L, LegalBasisAssessmentStatus.DRAFT, 1, null, null,
                null, null, null, assessment.getCreatedAt(), assessment.getUpdatedAt());

        assessment.submit();
        assessment.approve(7L);

        assertThrows(PrivacyException.class, () -> assessment.updateDetails("Outra finalidade",
                LegalBasisType.CONTRACT_OR_PRE_CONTRACT, "j", "d", "n", null, null,
                "c", null, null, null, null, null, null, null, null, false, null, null));

        ProcessingLegalBasisAssessment revision = assessment.createRevision();
        assertEquals(LegalBasisAssessmentStatus.DRAFT, revision.getStatus());
        assertEquals(2, revision.getAssessmentVersion());
        assertEquals(10L, revision.getPreviousVersionId());
    }

    @Test
    void rejectsApprovalOutsideReviewStateAndRejectionWithoutReason() {
        ProcessingLegalBasisAssessment assessment = completeContractAssessment();
        assertThrows(PrivacyException.class, () -> assessment.approve(7L));
        assessment.submit();
        assertThrows(PrivacyException.class, () -> assessment.reject(7L, " "));
    }

    private ProcessingLegalBasisAssessment completeContractAssessment() {
        ProcessingLegalBasisAssessment assessment = new ProcessingLegalBasisAssessment(
                1L, "Administrar reserva", LegalBasisType.CONTRACT_OR_PRE_CONTRACT);
        assessment.updateDetails("Administrar reserva", LegalBasisType.CONTRACT_OR_PRE_CONTRACT,
                "Necessaria para executar a reserva", "Nome e telefone", "Somente dados operacionais",
                null, null, "Reserva solicitada pelo titular", null, null, null, null, null,
                null, "Controle de acesso", null, false, null, null);
        return assessment;
    }
}
