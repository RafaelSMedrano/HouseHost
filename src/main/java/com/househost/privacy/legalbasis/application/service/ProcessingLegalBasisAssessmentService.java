package com.househost.privacy.legalbasis.application.service;

import com.househost.privacy.legalbasis.application.dto.DataProcessingOperationLegalBasisSummaryDTO;
import com.househost.privacy.legalbasis.application.dto.ProcessingLegalBasisAssessmentRequestDTO;
import com.househost.privacy.legalbasis.application.dto.ProcessingLegalBasisAssessmentResponseDTO;
import com.househost.privacy.legalbasis.application.port.in.ProcessingLegalBasisAssessmentUseCase;
import com.househost.privacy.legalbasis.application.port.out.LegalBasisReviewerPort;
import com.househost.privacy.legalbasis.application.port.out.PrivacyLegalBasisAuditPort;
import com.househost.privacy.legalbasis.application.port.out.ProcessingLegalBasisAssessmentPersistencePort;
import com.househost.privacy.legalbasis.domain.model.LegalBasisAssessmentStatus;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import com.househost.privacy.processing.application.records.ProcessingOperationRecord;
import com.househost.privacy.processing.application.service.DataProcessingOperationService;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import com.househost.shared.exception.PrivacyException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessingLegalBasisAssessmentService implements ProcessingLegalBasisAssessmentUseCase {
    private final ProcessingLegalBasisAssessmentPersistencePort persistencePort;
    private final DataProcessingOperationService processingOperationService;
    private final LegalBasisReviewerPort reviewerPort;
    private final PrivacyLegalBasisAuditPort auditPort;
    private final ProcessingLegalBasisAssessmentValidationService validationService;
    private final LegalBasisAssessmentReadinessService readinessService;

    public ProcessingLegalBasisAssessmentService(
            ProcessingLegalBasisAssessmentPersistencePort persistencePort,
            DataProcessingOperationService processingOperationService,
            LegalBasisReviewerPort reviewerPort,
            PrivacyLegalBasisAuditPort auditPort,
            ProcessingLegalBasisAssessmentValidationService validationService,
            LegalBasisAssessmentReadinessService readinessService
    ) {
        this.persistencePort = persistencePort;
        this.processingOperationService = processingOperationService;
        this.reviewerPort = reviewerPort;
        this.auditPort = auditPort;
        this.validationService = validationService;
        this.readinessService = readinessService;
    }

    @Override
    @Transactional
    public ProcessingLegalBasisAssessmentResponseDTO createDraft(
            Long operationId,
            ProcessingLegalBasisAssessmentRequestDTO request
    ) {
        ProcessingOperationRecord processingOperationRecord = findOperation(operationId);
        if (DataProcessingOperationCodes.WHATSAPP_MARKETING.equals(
                processingOperationRecord.operationCode()
        )) {
            throw new PrivacyException("A operacao de marketing esta inativa e nao recebe avaliacao corrente.");
        }
        validationService.validateDraft(request);
        String purpose = request.purpose.trim();
        if (persistencePort.existsByOperationIdAndPurpose(operationId, purpose)) {
            throw new PrivacyException("Ja existe uma avaliacao para esta finalidade; crie uma revisao da versao aprovada.");
        }
        ProcessingLegalBasisAssessment assessment = new ProcessingLegalBasisAssessment(
                operationId,
                purpose,
                request.legalBasis
        );
        apply(assessment, request);
        ProcessingLegalBasisAssessment saved = persistencePort.save(assessment);
        auditPort.record("LEGAL_BASIS_ASSESSMENT_CREATED", saved);
        return new ProcessingLegalBasisAssessmentResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataProcessingOperationLegalBasisSummaryDTO> findByOperation(Long operationId) {
        findOperation(operationId);
        return readinessService.summaries(persistencePort.findAllByOperationId(operationId));
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessingLegalBasisAssessmentResponseDTO findById(Long assessmentId) {
        return new ProcessingLegalBasisAssessmentResponseDTO(findAssessment(assessmentId));
    }

    @Override
    @Transactional
    public ProcessingLegalBasisAssessmentResponseDTO updateDraft(
            Long assessmentId,
            ProcessingLegalBasisAssessmentRequestDTO request
    ) {
        validationService.validateDraft(request);
        ProcessingLegalBasisAssessment assessment = findAssessment(assessmentId);
        apply(assessment, request);
        ProcessingLegalBasisAssessment saved = persistencePort.save(assessment);
        auditPort.record("LEGAL_BASIS_ASSESSMENT_UPDATED", saved);
        return new ProcessingLegalBasisAssessmentResponseDTO(saved);
    }

    @Override
    @Transactional
    public ProcessingLegalBasisAssessmentResponseDTO submit(Long assessmentId) {
        ProcessingLegalBasisAssessment assessment = findAssessment(assessmentId);
        validationService.validateForSubmission(assessment);
        assessment.submit();
        ProcessingLegalBasisAssessment saved = persistencePort.save(assessment);
        auditPort.record("LEGAL_BASIS_ASSESSMENT_SUBMITTED", saved);
        return new ProcessingLegalBasisAssessmentResponseDTO(saved);
    }

    @Override
    @Transactional
    public ProcessingLegalBasisAssessmentResponseDTO approve(Long assessmentId, String reviewerEmail) {
        ProcessingLegalBasisAssessment assessment = findAssessment(assessmentId);
        validationService.validateForSubmission(assessment);
        assessment.approve(findReviewer(reviewerEmail));
        ProcessingLegalBasisAssessment saved = persistencePort.save(assessment);
        if (saved.getPreviousVersionId() != null) {
            ProcessingLegalBasisAssessment previous = findAssessment(saved.getPreviousVersionId());
            previous.supersede();
            ProcessingLegalBasisAssessment superseded = persistencePort.save(previous);
            auditPort.record("LEGAL_BASIS_ASSESSMENT_SUPERSEDED", superseded);
        }
        auditPort.record("LEGAL_BASIS_ASSESSMENT_APPROVED", saved);
        return new ProcessingLegalBasisAssessmentResponseDTO(saved);
    }

    @Override
    @Transactional
    public ProcessingLegalBasisAssessmentResponseDTO reject(Long assessmentId, String reviewerEmail, String reason) {
        ProcessingLegalBasisAssessment assessment = findAssessment(assessmentId);
        assessment.reject(findReviewer(reviewerEmail), normalize(reason));
        ProcessingLegalBasisAssessment saved = persistencePort.save(assessment);
        auditPort.record("LEGAL_BASIS_ASSESSMENT_REJECTED", saved);
        return new ProcessingLegalBasisAssessmentResponseDTO(saved);
    }

    @Override
    @Transactional
    public ProcessingLegalBasisAssessmentResponseDTO createRevision(Long assessmentId) {
        ProcessingLegalBasisAssessment approved = findAssessment(assessmentId);
        List<ProcessingLegalBasisAssessment> historyList = persistencePort.findAllByOperationId(
                approved.getProcessingOperationId());
        Optional<ProcessingLegalBasisAssessment> pendingRevisionOptional = historyList.stream()
                .filter(assessment -> assessmentId.equals(assessment.getPreviousVersionId()))
                .filter(assessment -> assessment.getStatus() == LegalBasisAssessmentStatus.DRAFT
                        || assessment.getStatus() == LegalBasisAssessmentStatus.UNDER_REVIEW)
                .findFirst();
        if (pendingRevisionOptional.isPresent()) {
            return new ProcessingLegalBasisAssessmentResponseDTO(pendingRevisionOptional.get());
        }
        int nextVersion = historyList.stream()
                .filter(assessment -> assessment.getPurpose().equalsIgnoreCase(approved.getPurpose()))
                .mapToInt(ProcessingLegalBasisAssessment::getAssessmentVersion)
                .max()
                .orElse(approved.getAssessmentVersion()) + 1;
        ProcessingLegalBasisAssessment revision = approved.createRevision(nextVersion);
        ProcessingLegalBasisAssessment saved = persistencePort.save(revision);
        auditPort.record("LEGAL_BASIS_ASSESSMENT_CREATED", saved);
        return new ProcessingLegalBasisAssessmentResponseDTO(saved);
    }

    private void apply(
            ProcessingLegalBasisAssessment assessment,
            ProcessingLegalBasisAssessmentRequestDTO request
    ) {
        assessment.updateDetails(
                request.purpose.trim(),
                request.legalBasis,
                normalize(request.justification),
                normalize(request.personalDataCategories),
                normalize(request.necessityAssessment),
                normalize(request.legalReference),
                normalize(request.legalObligationDescription),
                normalize(request.contractualContext),
                normalize(request.consentCollectionMechanism),
                normalize(request.consentEvidenceMechanism),
                normalize(request.consentWithdrawalMechanism),
                normalize(request.legitimateInterest),
                normalize(request.legitimateExpectation),
                normalize(request.rightsImpactAssessment),
                normalize(request.safeguards),
                normalize(request.balancingConclusion),
                request.sensitiveData,
                request.sensitiveDataLegalBasis,
                normalize(request.sensitiveDataIndispensability)
        );
    }

    private ProcessingOperationRecord findOperation(Long operationId) {
        if (operationId == null) {
            throw new PrivacyException("Operacao de tratamento nao encontrada.");
        }
        return processingOperationService.findOperationRecordById(operationId);
    }

    private ProcessingLegalBasisAssessment findAssessment(Long assessmentId) {
        if (assessmentId == null) {
            throw new PrivacyException("Avaliacao de base legal nao encontrada.");
        }
        return persistencePort.findById(assessmentId)
                .orElseThrow(() -> new PrivacyException("Avaliacao de base legal nao encontrada."));
    }

    private Long findReviewer(String reviewerEmail) {
        if (reviewerEmail == null || reviewerEmail.isBlank()) {
            throw new PrivacyException("Usuario autenticado nao identificado.");
        }
        return reviewerPort.findReviewerIdByEmail(reviewerEmail);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
