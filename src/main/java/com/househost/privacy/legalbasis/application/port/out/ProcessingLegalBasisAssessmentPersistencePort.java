package com.househost.privacy.legalbasis.application.port.out;

import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import java.util.List;
import java.util.Optional;

public interface ProcessingLegalBasisAssessmentPersistencePort {
    ProcessingLegalBasisAssessment save(ProcessingLegalBasisAssessment assessment);
    Optional<ProcessingLegalBasisAssessment> findById(Long assessmentId);
    List<ProcessingLegalBasisAssessment> findAllByOperationId(Long operationId);
    boolean existsByOperationIdAndPurpose(Long operationId, String purpose);
}
