package com.househost.privacy.policy.adapter.out.integration;

import com.househost.audit.application.service.AuditEventService;
import com.househost.privacy.policy.application.port.out.PrivacyPolicyAuditPort;
import com.househost.privacy.policy.domain.model.PrivacyPolicy;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PrivacyPolicyAuditAdapter implements PrivacyPolicyAuditPort {
    private final AuditEventService auditEventService;

    public PrivacyPolicyAuditAdapter(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @Override
    public void record(String eventType, PrivacyPolicy privacyPolicy) {
        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("version", privacyPolicy.getVersion());
        metadataMap.put("status", privacyPolicy.getStatus().name());
        if (privacyPolicy.getContentHash() != null) {
            metadataMap.put("contentHash", privacyPolicy.getContentHash().value());
        }
        auditEventService.recordForJwtActor(
                DataProcessingOperationCodes.PRIVACY_GOVERNANCE,
                eventType,
                "PRIVACY_POLICY",
                privacyPolicy.getId(),
                metadataMap
        );
    }
}
