package com.househost.privacy.legalbasis.application.port.out;

public interface LegalBasisReviewerPort {
    Long findReviewerIdByEmail(String email);
}
