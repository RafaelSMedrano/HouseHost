package com.househost.privacy.legalbasis.application.service;

import com.househost.privacy.legalbasis.domain.model.LegalBasisType;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import com.househost.privacy.legalbasis.domain.model.SensitiveDataLegalBasisType;
import com.househost.shared.exception.PrivacyException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProcessingLegalBasisAssessmentValidationServiceTest {
    private final ProcessingLegalBasisAssessmentValidationService service =
            new ProcessingLegalBasisAssessmentValidationService();

    @Test
    void requiresLegalReferenceAndExplanationForLegalObligation() {
        ProcessingLegalBasisAssessment assessment = base(LegalBasisType.LEGAL_OR_REGULATORY_OBLIGATION);
        assertThrows(PrivacyException.class, () -> service.validateForSubmission(assessment));
        assessment.updateDetails(assessment.getPurpose(), assessment.getLegalBasis(), assessment.getJustification(),
                assessment.getPersonalDataCategories(), assessment.getNecessityAssessment(), "Lei aplicavel",
                "Dever imposto pela norma", null, null, null, null, null, null, null,
                "Acesso restrito", null, false, null, null);
        assertDoesNotThrow(() -> service.validateForSubmission(assessment));
    }

    @Test
    void requiresAllLegitimateInterestBalancingEvidence() {
        ProcessingLegalBasisAssessment assessment = base(LegalBasisType.LEGITIMATE_INTEREST);
        assertThrows(PrivacyException.class, () -> service.validateForSubmission(assessment));
        assessment.updateDetails(assessment.getPurpose(), assessment.getLegalBasis(), assessment.getJustification(),
                assessment.getPersonalDataCategories(), assessment.getNecessityAssessment(), null, null, null,
                null, null, null, "Interesse concreto", "Expectativa compativel", "Impacto reduzido",
                "Acesso restrito", "Interesse prevalece com salvaguardas", false, null, null);
        assertDoesNotThrow(() -> service.validateForSubmission(assessment));
    }

    @Test
    void requiresConsentCollectionEvidenceAndWithdrawalMechanisms() {
        ProcessingLegalBasisAssessment assessment = base(LegalBasisType.CONSENT);
        assertThrows(PrivacyException.class, () -> service.validateForSubmission(assessment));
        assessment.updateDetails(assessment.getPurpose(), assessment.getLegalBasis(), assessment.getJustification(),
                assessment.getPersonalDataCategories(), assessment.getNecessityAssessment(), null, null, null,
                "Aceite destacado", "Registro de versao e data", "Canal de revogacao", null, null, null,
                "Acesso restrito", null, false, null, null);
        assertDoesNotThrow(() -> service.validateForSubmission(assessment));
    }

    @Test
    void requiresSeparateBasisIndispensabilityAndSafeguardsForSensitiveData() {
        ProcessingLegalBasisAssessment assessment = base(LegalBasisType.PROTECTION_OF_LIFE);
        assessment.updateDetails(assessment.getPurpose(), assessment.getLegalBasis(), assessment.getJustification(),
                assessment.getPersonalDataCategories(), assessment.getNecessityAssessment(), null, null, null,
                null, null, null, null, null, null, null, null, true, null, null);
        assertThrows(PrivacyException.class, () -> service.validateForSubmission(assessment));
        assessment.updateDetails(assessment.getPurpose(), assessment.getLegalBasis(), assessment.getJustification(),
                assessment.getPersonalDataCategories(), assessment.getNecessityAssessment(), null, null, null,
                null, null, null, null, null, null, "Acesso restrito", null, true,
                SensitiveDataLegalBasisType.PROTECTION_OF_LIFE, "Indispensavel para proteger a vida");
        assertDoesNotThrow(() -> service.validateForSubmission(assessment));
    }

    private ProcessingLegalBasisAssessment base(LegalBasisType legalBasis) {
        ProcessingLegalBasisAssessment assessment = new ProcessingLegalBasisAssessment(1L, "Finalidade", legalBasis);
        assessment.updateDetails("Finalidade", legalBasis, "Justificativa", "Nome e telefone",
                "Dados estritamente necessarios", null, null, null, null, null, null, null, null,
                null, null, null, false, null, null);
        return assessment;
    }
}
