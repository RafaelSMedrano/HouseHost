package com.househost.privacy.legalbasis.domain.model;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensitiveDataLegalBasisTypeTest {

    @Test
    void exposesControlledLgpdReferenceForEverySensitiveDataBasis() {
        assertTrue(Arrays.stream(SensitiveDataLegalBasisType.values())
                .map(SensitiveDataLegalBasisType::getLgpdReference)
                .allMatch(reference -> reference.startsWith("Lei nº 13.709/2018, art. 11")));
        assertEquals(
                "Lei nº 13.709/2018, art. 11, II, g",
                SensitiveDataLegalBasisType.FRAUD_PREVENTION_AND_SECURITY.getLgpdReference()
        );
    }
}
