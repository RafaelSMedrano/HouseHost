package com.househost.privacy.legalbasis.application.dto;

import com.househost.privacy.legalbasis.domain.model.LegalBasisType;
import com.househost.privacy.legalbasis.domain.model.SensitiveDataLegalBasisType;

public class ProcessingLegalBasisAssessmentRequestDTO {
    public String purpose;
    public LegalBasisType legalBasis;
    public String justification;
    public String personalDataCategories;
    public String necessityAssessment;
    public String legalReference;
    public String legalObligationDescription;
    public String contractualContext;
    public String consentCollectionMechanism;
    public String consentEvidenceMechanism;
    public String consentWithdrawalMechanism;
    public String legitimateInterest;
    public String legitimateExpectation;
    public String rightsImpactAssessment;
    public String safeguards;
    public String balancingConclusion;
    public boolean sensitiveData;
    public SensitiveDataLegalBasisType sensitiveDataLegalBasis;
    public String sensitiveDataIndispensability;
}
