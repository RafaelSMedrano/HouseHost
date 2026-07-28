package com.househost.privacy.processing.adapter.out.persistence;

import com.househost.privacy.processing.adapter.out.persistence.entity.DataProcessingOperationJpaEntity;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface DataProcessingOperationJpaRepository extends JpaRepository<DataProcessingOperationJpaEntity, Long> {
    List<DataProcessingOperationJpaEntity> findAllByOrderByOperationNameAsc();

    List<DataProcessingOperationJpaEntity> findAllByStatusOrderByOperationNameAsc(DataProcessingOperationStatus status);

    boolean existsByOperationNameIgnoreCase(String operationName);

    boolean existsByOperationNameIgnoreCaseAndIdNot(String operationName, Long id);

    Optional<DataProcessingOperationJpaEntity> findByOperationCode(String operationCode);

    boolean existsByOperationCode(String operationCode);
}
