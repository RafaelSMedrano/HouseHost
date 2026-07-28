package com.househost.privacy.legalbasis.application.service;

import com.househost.privacy.legalbasis.application.records.LegalBasisAssessmentOverviewRecord;
import com.househost.privacy.legalbasis.application.port.out.ProcessingLegalBasisAssessmentPersistencePort;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LegalBasisAssessmentQueryService {
    private final ProcessingLegalBasisAssessmentPersistencePort persistencePort;
    private final LegalBasisAssessmentReadinessService readinessService;

    public LegalBasisAssessmentQueryService(
            ProcessingLegalBasisAssessmentPersistencePort persistencePort,
            LegalBasisAssessmentReadinessService readinessService
    ) {
        this.persistencePort = persistencePort;
        this.readinessService = readinessService;
    }

    @Transactional(readOnly = true)
    public LegalBasisAssessmentOverviewRecord findOverviewByOperationId(Long operationId) {
        List<ProcessingLegalBasisAssessment> processingLegalBasisAssessmentList =
                persistencePort.findAllByOperationId(operationId);
        return new LegalBasisAssessmentOverviewRecord(
                readinessService.readiness(processingLegalBasisAssessmentList),
                readinessService.summaries(processingLegalBasisAssessmentList)
        );
    }
}
