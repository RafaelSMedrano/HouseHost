package com.househost.privacy.legalbasis.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LegalBasisTypeTest {
    @Test
    void exposesTheLgpdReferenceForEverySupportedOrdinaryBasis() {
        assertEquals("Lei nº 13.709/2018, art. 7º, I", LegalBasisType.CONSENT.getLgpdReference());
        assertEquals("Lei nº 13.709/2018, art. 7º, II",
                LegalBasisType.LEGAL_OR_REGULATORY_OBLIGATION.getLgpdReference());
        assertEquals("Lei nº 13.709/2018, art. 7º, V",
                LegalBasisType.CONTRACT_OR_PRE_CONTRACT.getLgpdReference());
        assertEquals("Lei nº 13.709/2018, art. 7º, VI",
                LegalBasisType.REGULAR_EXERCISE_OF_RIGHTS.getLgpdReference());
        assertEquals("Lei nº 13.709/2018, art. 7º, VII",
                LegalBasisType.PROTECTION_OF_LIFE.getLgpdReference());
        assertEquals("Lei nº 13.709/2018, art. 7º, IX e art. 10",
                LegalBasisType.LEGITIMATE_INTEREST.getLgpdReference());
        assertEquals("Lei nº 13.709/2018, art. 7º, X",
                LegalBasisType.CREDIT_PROTECTION.getLgpdReference());
    }
}
