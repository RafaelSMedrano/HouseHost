package com.househost.privacy.legalbasis.adapter.out.integration;

import com.househost.audit.application.service.AuditEventService;
import com.househost.privacy.legalbasis.application.port.out.PrivacyLegalBasisAuditPort;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PrivacyLegalBasisAuditAdapter implements PrivacyLegalBasisAuditPort {
    private final AuditEventService auditEventService;

    public PrivacyLegalBasisAuditAdapter(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @Override
    public void record(String eventType, ProcessingLegalBasisAssessment assessment) {
        auditEventService.recordForJwtActor(DataProcessingOperationCodes.PRIVACY_GOVERNANCE,
                eventType, "LEGAL_BASIS_ASSESSMENT", assessment.getId(), Map.of(
                        "processingOperationId", assessment.getProcessingOperationId(),
                        "legalBasis", assessment.getLegalBasis().name(),
                        "status", assessment.getStatus().name(),
                        "assessmentVersion", assessment.getAssessmentVersion()));
    }
}
