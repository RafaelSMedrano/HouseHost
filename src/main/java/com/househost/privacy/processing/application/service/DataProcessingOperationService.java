package com.househost.privacy.processing.application.service;

import com.househost.privacy.processing.application.dto.DataProcessingOperationRequestDTO;
import com.househost.privacy.processing.application.dto.ProcessingOperationResponseDTO;
import com.househost.privacy.processing.application.port.in.DataProcessingOperationUseCase;
import com.househost.privacy.processing.application.port.out.DataProcessingOperationPersistencePort;
import com.househost.privacy.processing.application.records.ProcessingOperationRecord;
import com.househost.privacy.processing.domain.model.DataProcessingOperation;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import com.househost.shared.exception.PrivacyException;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataProcessingOperationService implements DataProcessingOperationUseCase {
    private final DataProcessingOperationPersistencePort persistencePort;
    private final DataProcessingOperationValidationService validationService;

    public DataProcessingOperationService(
            DataProcessingOperationPersistencePort persistencePort,
            DataProcessingOperationValidationService validationService
    ) {
        this.persistencePort = persistencePort;
        this.validationService = validationService;
    }

    @Override
    @Transactional
    public ProcessingOperationResponseDTO create(DataProcessingOperationRequestDTO request) {
        validationService.validate(request);

        String operationName = normalizeRequired(request.operationName);
        if (persistencePort.existsByOperationName(operationName)) {
            throw new PrivacyException("Ja existe uma operacao de tratamento com esse nome.");
        }
        String operationCode = uniqueOperationCode(operationName);

        DataProcessingOperation operation = new DataProcessingOperation(
                operationCode,
                operationName,
                normalizeRequired(request.description),
                normalizeRequired(request.purpose),
                normalizeRequired(request.legalBasis).toUpperCase(),
                normalizeRequired(request.dataSubjectCategories),
                normalizeRequired(request.personalDataCategories),
                normalizeRequired(request.dataSource),
                normalizeRequired(request.processingActions),
                normalizeRequired(request.internalAccessRoles),
                normalizeOptional(request.externalRecipients),
                Boolean.TRUE.equals(request.internationalTransfer),
                normalizeRequired(request.retentionPeriod),
                normalizeRequired(request.deletionMethod),
                normalizeRequired(request.securityMeasures),
                normalizeRequired(request.responsibleArea),
                normalizeRequired(request.systemName)
        );

        return toResponse(persistencePort.save(operation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessingOperationResponseDTO> findAll(DataProcessingOperationStatus status) {
        List<DataProcessingOperation> processingOperationList;
        if (status == null) {
            processingOperationList = persistencePort.findAllOrderedByName();
        } else {
            processingOperationList = persistencePort.findAllByStatusOrderedByName(status);
        }

        return processingOperationList.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessingOperationResponseDTO findById(Long id) {
        return toResponse(findOperationById(id));
    }

    @Override
    @Transactional
    public ProcessingOperationResponseDTO update(Long id, DataProcessingOperationRequestDTO request) {
        validationService.validate(request);

        DataProcessingOperation operation = findOperationById(id);
        String operationName = normalizeRequired(request.operationName);
        if (persistencePort.existsByOperationNameExcludingId(operationName, id)) {
            throw new PrivacyException("Ja existe uma operacao de tratamento com esse nome.");
        }

        operation.updateDetails(
                operationName,
                normalizeRequired(request.description),
                normalizeRequired(request.purpose),
                normalizeRequired(request.legalBasis).toUpperCase(),
                normalizeRequired(request.dataSubjectCategories),
                normalizeRequired(request.personalDataCategories),
                normalizeRequired(request.dataSource),
                normalizeRequired(request.processingActions),
                normalizeRequired(request.internalAccessRoles),
                normalizeOptional(request.externalRecipients),
                Boolean.TRUE.equals(request.internationalTransfer),
                normalizeRequired(request.retentionPeriod),
                normalizeRequired(request.deletionMethod),
                normalizeRequired(request.securityMeasures),
                normalizeRequired(request.responsibleArea),
                normalizeRequired(request.systemName)
        );

        return toResponse(persistencePort.save(operation));
    }

    @Override
    @Transactional
    public ProcessingOperationResponseDTO changeStatus(Long id, DataProcessingOperationStatus status) {
        if (status == null) {
            throw new PrivacyException("Status da operacao de tratamento e obrigatorio.");
        }

        DataProcessingOperation operation = findOperationById(id);
        operation.changeStatus(status);
        return toResponse(persistencePort.save(operation));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findIdByOperationCode(String operationCode) {
        Optional<ProcessingOperationRecord> processingOperationRecordOptional =
                findOperationRecordByCode(operationCode);
        return processingOperationRecordOptional.map(ProcessingOperationRecord::operationId);
    }

    @Transactional(readOnly = true)
    public ProcessingOperationRecord findOperationRecordById(Long operationId) {
        return toRecord(findOperationById(operationId));
    }

    @Transactional(readOnly = true)
    public Optional<ProcessingOperationRecord> findOperationRecordByCode(String operationCode) {
        if (operationCode == null || operationCode.isBlank()) {
            return Optional.empty();
        }
        return persistencePort.findByOperationCode(operationCode.trim())
                .map(this::toRecord);
    }

    @Transactional(readOnly = true)
    public List<ProcessingOperationRecord> findAllOperationRecords() {
        return persistencePort.findAllOrderedByName().stream()
                .map(this::toRecord)
                .toList();
    }

    private ProcessingOperationResponseDTO toResponse(DataProcessingOperation operation) {
        return new ProcessingOperationResponseDTO(operation);
    }

    private ProcessingOperationRecord toRecord(DataProcessingOperation operation) {
        return new ProcessingOperationRecord(
                operation.getId(),
                operation.getOperationCode(),
                operation.getStatus()
        );
    }

    private DataProcessingOperation findOperationById(Long id) {
        if (id == null) {
            throw new PrivacyException("Operacao de tratamento nao encontrada.");
        }

        return persistencePort.findById(id)
                .orElseThrow(() -> new PrivacyException("Operacao de tratamento nao encontrada."));
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String uniqueOperationCode(String operationName) {
        String baseCode = Normalizer.normalize(operationName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        String codePrefix = baseCode.isBlank() ? "PROCESSING_OPERATION" : baseCode;
        String candidate = codePrefix;
        int suffix = 2;
        while (persistencePort.existsByOperationCode(candidate)) {
            candidate = codePrefix + "_" + suffix++;
        }
        return candidate;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
