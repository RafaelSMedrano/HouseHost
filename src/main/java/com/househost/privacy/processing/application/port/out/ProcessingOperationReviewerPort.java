package com.househost.privacy.processing.application.port.out;

public interface ProcessingOperationReviewerPort {
    Long findReviewerIdByEmail(String email);
}
