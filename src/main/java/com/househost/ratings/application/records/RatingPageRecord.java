package com.househost.ratings.application.records;

import java.util.List;

public record RatingPageRecord(
        List<RatingSummaryRecord> ratingSummaryRecordList,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public RatingPageRecord {
        ratingSummaryRecordList = List.copyOf(ratingSummaryRecordList);
    }
}
