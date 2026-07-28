package com.househost.privacy.processing.adapter.out.persistence;

import com.househost.privacy.processing.adapter.out.persistence.entity.DataProcessingOperationPersistenceMapper;
import com.househost.privacy.processing.adapter.out.persistence.entity.DataProcessingOperationJpaEntity;
import com.househost.privacy.processing.application.port.out.DataProcessingOperationPersistencePort;
import com.househost.privacy.processing.domain.model.DataProcessingOperation;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DataProcessingOperationPersistenceAdapter implements DataProcessingOperationPersistencePort {
    private final DataProcessingOperationJpaRepository repository;

    public DataProcessingOperationPersistenceAdapter(DataProcessingOperationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public DataProcessingOperation save(DataProcessingOperation operation) {
        if (operation.getCreatedAt() == null) {
            operation.prepareForCreation();
        } else {
            operation.markUpdated();
        }
        DataProcessingOperationJpaEntity operationJpaEntity =
                DataProcessingOperationPersistenceMapper.toEntity(operation);
        DataProcessingOperationJpaEntity savedOperationJpaEntity = repository.save(operationJpaEntity);
        return DataProcessingOperationPersistenceMapper.toDomain(savedOperationJpaEntity);
    }

    @Override
    public Optional<DataProcessingOperation> findById(Long id) {
        return repository.findById(id)
                .map(DataProcessingOperationPersistenceMapper::toDomain);
    }

    @Override
    public Optional<DataProcessingOperation> findByOperationCode(String operationCode) {
        return repository.findByOperationCode(operationCode)
                .map(DataProcessingOperationPersistenceMapper::toDomain);
    }

    @Override
    public List<DataProcessingOperation> findAllOrderedByName() {
        return map(repository.findAllByOrderByOperationNameAsc());
    }

    @Override
    public List<DataProcessingOperation> findAllByStatusOrderedByName(
            DataProcessingOperationStatus status
    ) {
        return map(repository.findAllByStatusOrderByOperationNameAsc(status));
    }

    @Override
    public boolean existsByOperationName(String operationName) {
        return repository.existsByOperationNameIgnoreCase(operationName);
    }

    @Override
    public boolean existsByOperationNameExcludingId(String operationName, Long id) {
        return repository.existsByOperationNameIgnoreCaseAndIdNot(operationName, id);
    }

    @Override
    public boolean existsByOperationCode(String operationCode) {
        return repository.existsByOperationCode(operationCode);
    }

    private List<DataProcessingOperation> map(
            List<DataProcessingOperationJpaEntity> operationJpaEntityList
    ) {
        return operationJpaEntityList.stream()
                .map(DataProcessingOperationPersistenceMapper::toDomain)
                .toList();
    }
}
