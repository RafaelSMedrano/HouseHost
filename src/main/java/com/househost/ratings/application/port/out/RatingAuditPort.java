package com.househost.ratings.application.port.out;

import java.util.Map;

public interface RatingAuditPort {

    void record(
            String eventType,
            Long ratingId,
            Map<String, Object> metadataMap
    );
}
