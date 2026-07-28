package com.househost.privacy.legalbasis.adapter.out.persistence;

import com.househost.privacy.legalbasis.adapter.out.persistence.entity.ProcessingLegalBasisAssessmentPersistenceMapper;
import com.househost.privacy.legalbasis.application.port.out.ProcessingLegalBasisAssessmentPersistencePort;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ProcessingLegalBasisAssessmentPersistenceAdapter
        implements ProcessingLegalBasisAssessmentPersistencePort {
    private final ProcessingLegalBasisAssessmentJpaRepository repository;

    public ProcessingLegalBasisAssessmentPersistenceAdapter(ProcessingLegalBasisAssessmentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProcessingLegalBasisAssessment save(ProcessingLegalBasisAssessment assessment) {
        if (assessment.getCreatedAt() == null) {
            assessment.prepareForCreation();
        }
        return ProcessingLegalBasisAssessmentPersistenceMapper.toDomain(repository.save(
                ProcessingLegalBasisAssessmentPersistenceMapper.toEntity(assessment)));
    }

    @Override
    public Optional<ProcessingLegalBasisAssessment> findById(Long assessmentId) {
        return repository.findById(assessmentId).map(ProcessingLegalBasisAssessmentPersistenceMapper::toDomain);
    }

    @Override
    public List<ProcessingLegalBasisAssessment> findAllByOperationId(Long operationId) {
        return repository.findAllByProcessingOperationIdOrderByPurposeAscAssessmentVersionDesc(operationId).stream()
                .map(ProcessingLegalBasisAssessmentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByOperationIdAndPurpose(Long operationId, String purpose) {
        return repository.existsByProcessingOperationIdAndPurposeKey(operationId,
                ProcessingLegalBasisAssessmentPersistenceMapper.purposeKey(purpose));
    }
}
