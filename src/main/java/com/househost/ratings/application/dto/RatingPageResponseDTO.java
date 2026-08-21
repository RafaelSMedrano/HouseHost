package com.househost.ratings.application.dto;

import com.househost.ratings.application.records.RatingPageRecord;

import java.util.List;

public class RatingPageResponseDTO {

    private final List<RatingSummaryDTO> ratingSummaryDTOList;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    public RatingPageResponseDTO(RatingPageRecord ratingPageRecord) {
        this.ratingSummaryDTOList = ratingPageRecord.ratingSummaryRecordList()
                .stream()
                .map(RatingSummaryDTO::new)
                .toList();
        this.page = ratingPageRecord.page();
        this.size = ratingPageRecord.size();
        this.totalElements = ratingPageRecord.totalElements();
        this.totalPages = ratingPageRecord.totalPages();
    }

    public List<RatingSummaryDTO> getRatingSummaryDTOList() {
        return ratingSummaryDTOList;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
