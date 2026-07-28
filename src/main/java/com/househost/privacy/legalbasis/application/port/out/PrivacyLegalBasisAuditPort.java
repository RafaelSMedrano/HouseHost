package com.househost.privacy.legalbasis.application.port.out;

import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;

public interface PrivacyLegalBasisAuditPort {
    void record(String eventType, ProcessingLegalBasisAssessment assessment);
}
