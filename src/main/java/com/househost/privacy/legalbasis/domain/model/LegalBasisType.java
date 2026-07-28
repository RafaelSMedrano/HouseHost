package com.househost.privacy.legalbasis.domain.model;

public enum LegalBasisType {
    CONSENT("Lei nº 13.709/2018, art. 7º, I"),
    LEGAL_OR_REGULATORY_OBLIGATION("Lei nº 13.709/2018, art. 7º, II"),
    CONTRACT_OR_PRE_CONTRACT("Lei nº 13.709/2018, art. 7º, V"),
    REGULAR_EXERCISE_OF_RIGHTS("Lei nº 13.709/2018, art. 7º, VI"),
    PROTECTION_OF_LIFE("Lei nº 13.709/2018, art. 7º, VII"),
    LEGITIMATE_INTEREST("Lei nº 13.709/2018, art. 7º, IX e art. 10"),
    CREDIT_PROTECTION("Lei nº 13.709/2018, art. 7º, X");

    private final String lgpdReference;

    LegalBasisType(String lgpdReference) {
        this.lgpdReference = lgpdReference;
    }

    public String getLgpdReference() {
        return lgpdReference;
    }
}
