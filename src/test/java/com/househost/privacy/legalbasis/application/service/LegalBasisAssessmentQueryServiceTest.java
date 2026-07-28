package com.househost.privacy.legalbasis.application.service;

import com.househost.privacy.legalbasis.application.port.out.ProcessingLegalBasisAssessmentPersistencePort;
import com.househost.privacy.legalbasis.application.records.LegalBasisAssessmentOverviewRecord;
import com.househost.privacy.legalbasis.domain.model.LegalBasisReadiness;
import com.househost.privacy.legalbasis.domain.model.LegalBasisType;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LegalBasisAssessmentQueryServiceTest {

    @Test
    void returnsReadinessAndSummariesForParentComposition() {
        ProcessingLegalBasisAssessment assessment = new ProcessingLegalBasisAssessment(
                3L,
                "Executar hospedagem",
                LegalBasisType.CONTRACT_OR_PRE_CONTRACT
        );
        ProcessingLegalBasisAssessmentPersistencePort persistencePort =
                mock(ProcessingLegalBasisAssessmentPersistencePort.class);
        when(persistencePort.findAllByOperationId(3L)).thenReturn(List.of(assessment));
        LegalBasisAssessmentQueryService service = new LegalBasisAssessmentQueryService(
                persistencePort,
                new LegalBasisAssessmentReadinessService()
        );

        LegalBasisAssessmentOverviewRecord overviewRecord = service.findOverviewByOperationId(3L);

        assertEquals(LegalBasisReadiness.DRAFT, overviewRecord.readiness());
        assertEquals(1, overviewRecord.legalBasisAssessmentSummaryDTOList().size());
    }
}
