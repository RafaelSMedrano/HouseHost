package com.househost.ratings.adapter.out.integration;

import com.househost.audit.application.service.AuditEventService;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import com.househost.ratings.application.port.out.RatingAuditPort;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RatingAuditAdapter implements RatingAuditPort {

    private final AuditEventService auditEventService;

    public RatingAuditAdapter(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @Override
    public void record(
            String eventType,
            Long ratingId,
            Map<String, Object> metadataMap
    ) {
        auditEventService.recordForJwtActor(
                DataProcessingOperationCodes.STAY_MANAGEMENT,
                eventType,
                "RATING",
                ratingId,
                metadataMap
        );
    }
}
