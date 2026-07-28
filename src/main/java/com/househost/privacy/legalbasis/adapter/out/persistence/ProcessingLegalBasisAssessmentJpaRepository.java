package com.househost.privacy.legalbasis.adapter.out.persistence;

import com.househost.privacy.legalbasis.adapter.out.persistence.entity.ProcessingLegalBasisAssessmentJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface ProcessingLegalBasisAssessmentJpaRepository
        extends JpaRepository<ProcessingLegalBasisAssessmentJpaEntity, Long> {
    List<ProcessingLegalBasisAssessmentJpaEntity> findAllByProcessingOperationIdOrderByPurposeAscAssessmentVersionDesc(
            Long processingOperationId);
    boolean existsByProcessingOperationIdAndPurposeKey(Long processingOperationId, String purposeKey);
}
