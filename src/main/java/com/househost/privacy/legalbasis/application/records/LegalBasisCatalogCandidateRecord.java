package com.househost.privacy.legalbasis.application.records;

import com.househost.privacy.legalbasis.domain.model.LegalBasisType;

public record LegalBasisCatalogCandidateRecord(
        String purpose,
        LegalBasisType legalBasis
) {
}
