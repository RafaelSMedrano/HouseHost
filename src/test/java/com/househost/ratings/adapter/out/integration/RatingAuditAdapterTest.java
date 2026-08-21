package com.househost.ratings.adapter.out.integration;

import com.househost.audit.application.service.AuditEventService;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RatingAuditAdapterTest {

    @Test
    void recordsRatingEventUnderStayManagementWithoutFeedbackPayload() {
        AuditEventService auditEventService = mock(AuditEventService.class);
        RatingAuditAdapter ratingAuditAdapter = new RatingAuditAdapter(auditEventService);
        Map<String, Object> metadataMap = Map.of(
                "bookingId", 42L,
                "outcome", "CREATED"
        );

        ratingAuditAdapter.record("RATING_CREATED", 9L, metadataMap);

        verify(auditEventService).recordForJwtActor(
                DataProcessingOperationCodes.STAY_MANAGEMENT,
                "RATING_CREATED",
                "RATING",
                9L,
                metadataMap
        );
    }
}
