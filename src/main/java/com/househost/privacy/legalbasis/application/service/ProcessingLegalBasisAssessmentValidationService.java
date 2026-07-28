package com.househost.privacy.legalbasis.application.service;

import com.househost.privacy.legalbasis.application.dto.ProcessingLegalBasisAssessmentRequestDTO;
import com.househost.privacy.legalbasis.domain.model.LegalBasisType;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import com.househost.shared.exception.PrivacyException;
import org.springframework.stereotype.Service;

@Service
public class ProcessingLegalBasisAssessmentValidationService {
    private static final int PURPOSE_LIMIT = 500;
    private static final int NARRATIVE_LIMIT = 4000;

    public void validateDraft(ProcessingLegalBasisAssessmentRequestDTO request) {
        if (request == null) {
            throw new PrivacyException("Os dados da avaliacao de base legal sao obrigatorios.");
        }
        required(request.purpose, "A finalidade", PURPOSE_LIMIT);
        if (request.legalBasis == null) {
            throw new PrivacyException("A base legal e obrigatoria.");
        }
        limitAll(request);
        if (!request.sensitiveData
                && (request.sensitiveDataLegalBasis != null || !isBlank(request.sensitiveDataIndispensability))) {
            throw new PrivacyException("Fundamentacao de dado sensivel exige sensitiveData igual a true.");
        }
    }

    public void validateForSubmission(ProcessingLegalBasisAssessment assessment) {
        required(assessment.getPurpose(), "A finalidade", PURPOSE_LIMIT);
        required(assessment.getJustification(), "A justificativa", NARRATIVE_LIMIT);
        required(assessment.getPersonalDataCategories(), "As categorias de dados pessoais", NARRATIVE_LIMIT);
        required(assessment.getNecessityAssessment(), "A avaliacao de necessidade", NARRATIVE_LIMIT);

        if (assessment.getLegalBasis() == LegalBasisType.LEGAL_OR_REGULATORY_OBLIGATION) {
            required(assessment.getLegalReference(), "A referencia legal", NARRATIVE_LIMIT);
            required(assessment.getLegalObligationDescription(), "A explicacao da obrigacao legal", NARRATIVE_LIMIT);
        }
        if (assessment.getLegalBasis() == LegalBasisType.CONTRACT_OR_PRE_CONTRACT) {
            required(assessment.getContractualContext(), "O contexto contratual", NARRATIVE_LIMIT);
        }
        if (assessment.getLegalBasis() == LegalBasisType.CONSENT) {
            required(assessment.getConsentCollectionMechanism(), "O mecanismo de coleta do consentimento", NARRATIVE_LIMIT);
            required(assessment.getConsentEvidenceMechanism(), "O mecanismo de prova do consentimento", NARRATIVE_LIMIT);
            required(assessment.getConsentWithdrawalMechanism(), "O mecanismo de revogacao do consentimento", NARRATIVE_LIMIT);
        }
        if (assessment.getLegalBasis() == LegalBasisType.LEGITIMATE_INTEREST) {
            required(assessment.getLegitimateInterest(), "O interesse legitimo", NARRATIVE_LIMIT);
            required(assessment.getLegitimateExpectation(), "A expectativa legitima", NARRATIVE_LIMIT);
            required(assessment.getRightsImpactAssessment(), "A avaliacao de impacto aos direitos", NARRATIVE_LIMIT);
            required(assessment.getSafeguards(), "As salvaguardas", NARRATIVE_LIMIT);
            required(assessment.getBalancingConclusion(), "A conclusao do teste de balanceamento", NARRATIVE_LIMIT);
        }
        if (assessment.isSensitiveData()) {
            if (assessment.getSensitiveDataLegalBasis() == null) {
                throw new PrivacyException("A base legal de dados sensiveis e obrigatoria.");
            }
            required(assessment.getSensitiveDataIndispensability(), "A demonstracao de indispensabilidade", NARRATIVE_LIMIT);
            required(assessment.getSafeguards(), "As salvaguardas para dados sensiveis", NARRATIVE_LIMIT);
        }
    }

    private void limitAll(ProcessingLegalBasisAssessmentRequestDTO request) {
        limit(request.justification, "A justificativa");
        limit(request.personalDataCategories, "As categorias de dados pessoais");
        limit(request.necessityAssessment, "A avaliacao de necessidade");
        limit(request.legalReference, "A referencia legal");
        limit(request.legalObligationDescription, "A explicacao da obrigacao legal");
        limit(request.contractualContext, "O contexto contratual");
        limit(request.consentCollectionMechanism, "O mecanismo de coleta do consentimento");
        limit(request.consentEvidenceMechanism, "O mecanismo de prova do consentimento");
        limit(request.consentWithdrawalMechanism, "O mecanismo de revogacao do consentimento");
        limit(request.legitimateInterest, "O interesse legitimo");
        limit(request.legitimateExpectation, "A expectativa legitima");
        limit(request.rightsImpactAssessment, "A avaliacao de impacto aos direitos");
        limit(request.safeguards, "As salvaguardas");
        limit(request.balancingConclusion, "A conclusao do teste de balanceamento");
        limit(request.sensitiveDataIndispensability, "A demonstracao de indispensabilidade");
    }

    private void required(String value, String label, int limit) {
        if (isBlank(value)) {
            throw new PrivacyException(label + " e obrigatoria.");
        }
        if (value.trim().length() > limit) {
            throw new PrivacyException(label + " excede o limite de " + limit + " caracteres.");
        }
    }

    private void limit(String value, String label) {
        if (value != null && value.trim().length() > NARRATIVE_LIMIT) {
            throw new PrivacyException(label + " excede o limite de " + NARRATIVE_LIMIT + " caracteres.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
