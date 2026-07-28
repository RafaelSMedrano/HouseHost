package com.househost.privacy.legalbasis.domain.model;

public enum SensitiveDataLegalBasisType {
    SPECIFIC_CONSENT("Lei nº 13.709/2018, art. 11, I"),
    LEGAL_OR_REGULATORY_OBLIGATION("Lei nº 13.709/2018, art. 11, II, a"),
    PUBLIC_POLICY("Lei nº 13.709/2018, art. 11, II, b"),
    RESEARCH("Lei nº 13.709/2018, art. 11, II, c"),
    REGULAR_EXERCISE_OF_RIGHTS("Lei nº 13.709/2018, art. 11, II, d"),
    PROTECTION_OF_LIFE("Lei nº 13.709/2018, art. 11, II, e"),
    HEALTH_PROTECTION("Lei nº 13.709/2018, art. 11, II, f"),
    FRAUD_PREVENTION_AND_SECURITY("Lei nº 13.709/2018, art. 11, II, g");

    private final String lgpdReference;

    SensitiveDataLegalBasisType(String lgpdReference) {
        this.lgpdReference = lgpdReference;
    }

    public String getLgpdReference() {
        return lgpdReference;
    }
}
