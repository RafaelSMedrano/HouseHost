package com.househost.privacy.legalbasis.adapter.in.rest;

import com.househost.privacy.legalbasis.application.dto.LegalBasisAssessmentRejectionRequestDTO;
import com.househost.privacy.legalbasis.application.dto.ProcessingLegalBasisAssessmentRequestDTO;
import com.househost.privacy.legalbasis.application.port.in.ProcessingLegalBasisAssessmentUseCase;
import com.househost.shared.dto.ResponseDTO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessingLegalBasisAssessmentController {
    private final ProcessingLegalBasisAssessmentUseCase useCase;

    public ProcessingLegalBasisAssessmentController(ProcessingLegalBasisAssessmentUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/data-processing-operations/{operationId}/legal-basis-assessments")
    public ResponseDTO createDraft(@PathVariable Long operationId,
            @RequestBody ProcessingLegalBasisAssessmentRequestDTO request) {
        return new ResponseDTO("success", "Avaliacao de base legal criada como rascunho.",
                useCase.createDraft(operationId, request));
    }

    @GetMapping("/data-processing-operations/{operationId}/legal-basis-assessments")
    public ResponseDTO findByOperation(@PathVariable Long operationId) {
        return new ResponseDTO("success", "Avaliacoes de base legal encontradas.",
                useCase.findByOperation(operationId));
    }

    @GetMapping("/legal-basis-assessments/{assessmentId}")
    public ResponseDTO findById(@PathVariable Long assessmentId) {
        return new ResponseDTO("success", "Avaliacao de base legal encontrada.", useCase.findById(assessmentId));
    }

    @PutMapping("/legal-basis-assessments/{assessmentId}")
    public ResponseDTO updateDraft(@PathVariable Long assessmentId,
            @RequestBody ProcessingLegalBasisAssessmentRequestDTO request) {
        return new ResponseDTO("success", "Rascunho da avaliacao atualizado.",
                useCase.updateDraft(assessmentId, request));
    }

    @PostMapping("/legal-basis-assessments/{assessmentId}/submit")
    public ResponseDTO submit(@PathVariable Long assessmentId) {
        return new ResponseDTO("success", "Avaliacao enviada para revisao.", useCase.submit(assessmentId));
    }

    @PostMapping("/legal-basis-assessments/{assessmentId}/approve")
    public ResponseDTO approve(@PathVariable Long assessmentId, Authentication authentication) {
        return new ResponseDTO("success", "Avaliacao aprovada.",
                useCase.approve(assessmentId, authenticatedEmail(authentication)));
    }

    @PostMapping("/legal-basis-assessments/{assessmentId}/reject")
    public ResponseDTO reject(@PathVariable Long assessmentId, Authentication authentication,
            @RequestBody LegalBasisAssessmentRejectionRequestDTO request) {
        String reason = request == null ? null : request.reason;
        return new ResponseDTO("success", "Avaliacao rejeitada.",
                useCase.reject(assessmentId, authenticatedEmail(authentication), reason));
    }

    @PostMapping("/legal-basis-assessments/{assessmentId}/revisions")
    public ResponseDTO createRevision(@PathVariable Long assessmentId) {
        return new ResponseDTO("success", "Nova revisao criada como rascunho.",
                useCase.createRevision(assessmentId));
    }

    private String authenticatedEmail(Authentication authentication) {
        return authentication == null ? null : authentication.getName();
    }
}
