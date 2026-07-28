package com.househost.privacy.legalbasis.application.records;

import com.househost.privacy.legalbasis.application.dto.DataProcessingOperationLegalBasisSummaryDTO;
import com.househost.privacy.legalbasis.domain.model.LegalBasisReadiness;
import java.util.List;

public record LegalBasisAssessmentOverviewRecord(
        LegalBasisReadiness readiness,
        List<DataProcessingOperationLegalBasisSummaryDTO> legalBasisAssessmentSummaryDTOList
) {
    public LegalBasisAssessmentOverviewRecord {
        legalBasisAssessmentSummaryDTOList = List.copyOf(legalBasisAssessmentSummaryDTOList);
    }
}
