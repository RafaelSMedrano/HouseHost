package com.househost.supplier.application.service;

import com.househost.supplier.application.dto.*;
import com.househost.supplier.application.port.in.SupplierUseCase;
import com.househost.supplier.application.port.out.*;
import com.househost.supplier.domain.exception.SupplierException;
import com.househost.supplier.domain.model.*;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierService implements SupplierUseCase {
    private final SupplierPersistencePort persistencePort;
    private final SupplierAuditPort auditPort;
    private final SupplierReviewerPort reviewerPort;
    private final SupplierValidationService validationService;

    public SupplierService(SupplierPersistencePort persistencePort, SupplierAuditPort auditPort,
            SupplierReviewerPort reviewerPort, SupplierValidationService validationService) {
        this.persistencePort = persistencePort;
        this.auditPort = auditPort;
        this.reviewerPort = reviewerPort;
        this.validationService = validationService;
    }

    @Transactional
    public SupplierDetailResponseDTO create(SupplierRequestDTO request) {
        validationService.validate(request);
        String normalizedOfficialName = normalizeIdentity(request.officialName);
        validateDuplicates(normalizedOfficialName, normalizeIdentifier(request.registrationIdentifier), null);
        Supplier supplier = supplierFromRequest(null, request, relationshipsFromRequest(request.relationshipList));
        Supplier savedSupplier = persistencePort.save(supplier);
        auditPort.record("SUPPLIER_CREATED", savedSupplier.getId(), Map.of("status", savedSupplier.getStatus().name()));
        return new SupplierDetailResponseDTO(savedSupplier);
    }

    @Transactional(readOnly = true)
    public List<SupplierListResponseDTO> findAll(String name, SupplierDataRole role,
            SupplierRiskLevel risk, SupplierGovernanceStatus governanceStatus,
            SupplierStatus status) {
        return persistencePort.findAll(name, role, risk, governanceStatus, status).stream()
                .map(supplier -> new SupplierListResponseDTO(supplier, LocalDate.now())).toList();
    }

    @Transactional(readOnly = true)
    public SupplierDetailResponseDTO findById(Long id) {
        Supplier supplier = requireSupplier(id);
        auditPort.record("SUPPLIER_VIEWED", id, Map.of());
        return new SupplierDetailResponseDTO(supplier);
    }

    @Transactional
    public SupplierDetailResponseDTO update(Long id, SupplierRequestDTO request) {
        validationService.validate(request);
        Supplier supplier = requireSupplier(id);
        String normalizedOfficialName = normalizeIdentity(request.officialName);
        String registrationIdentifier = normalizeIdentifier(request.registrationIdentifier);
        validateDuplicates(normalizedOfficialName, registrationIdentifier, id);
        List<SupplierDataProcessingRelationship> relationshipList = mergeRelationships(supplier, request.relationshipList);
        supplier.updateDetails(required(request.officialName), normalizedOfficialName,
                optional(request.tradeName), registrationIdentifier, optional(request.website),
                required(request.countryOfEstablishment), optional(request.businessContact),
                optional(request.privacyContact), optional(request.incidentContact),
                request.internalOwnerUserId, request.status, relationshipList);
        Supplier savedSupplier = persistencePort.save(supplier);
        auditPort.record("SUPPLIER_UPDATED", id, Map.of("changedFieldList", List.of("supplier", "relationships")));
        return new SupplierDetailResponseDTO(savedSupplier);
    }

    @Transactional
    public SupplierDetailResponseDTO changeStatus(Long id, SupplierStatusRequestDTO request) {
        Supplier supplier = requireSupplier(id);
        SupplierStatus status = request == null ? null : request.status;
        if (status == SupplierStatus.INACTIVE && supplier.getRelationshipList().stream()
                .anyMatch(relationship -> relationship.getGovernanceStatus() != SupplierGovernanceStatus.INACTIVE)) {
            throw new SupplierException("Inative todas as relacoes e registre o destino dos dados antes do fornecedor.");
        }
        supplier.changeStatus(status);
        Supplier savedSupplier = persistencePort.save(supplier);
        auditPort.record("SUPPLIER_STATUS_CHANGED", id, Map.of("status", status.name()));
        return new SupplierDetailResponseDTO(savedSupplier);
    }

    @Transactional
    public SupplierDetailResponseDTO reviewRelationship(Long id, Long relationshipId,
            SupplierReviewRequestDTO request, String reviewerEmail) {
        validationService.validateReview(request);
        Supplier supplier = requireSupplier(id);
        Long reviewerId = reviewerPort.findReviewerIdByEmail(reviewerEmail);
        SupplierDataProcessingRelationship relationship = supplier.relationship(relationshipId);
        relationship.review(request.governanceStatus, request.riskLevel,
                optional(request.assessmentNotes), request.nextReviewDate,
                reviewerId, LocalDateTime.now());
        Supplier savedSupplier = persistencePort.save(supplier);
        auditPort.record("SUPPLIER_RELATIONSHIP_REVIEWED", id, Map.of(
                "relationshipId", relationshipId,
                "status", request.governanceStatus.name(),
                "risk", request.riskLevel.name()));
        return new SupplierDetailResponseDTO(savedSupplier);
    }

    private Supplier supplierFromRequest(Long id, SupplierRequestDTO request,
            List<SupplierDataProcessingRelationship> relationshipList) {
        String normalizedOfficialName = normalizeIdentity(request.officialName);
        return new Supplier(id, required(request.officialName), normalizedOfficialName,
                optional(request.tradeName), normalizeIdentifier(request.registrationIdentifier),
                optional(request.website), required(request.countryOfEstablishment),
                optional(request.businessContact), optional(request.privacyContact),
                optional(request.incidentContact), request.internalOwnerUserId,
                request.status, relationshipList);
    }

    private List<SupplierDataProcessingRelationship> mergeRelationships(Supplier supplier,
            List<SupplierRelationshipRequestDTO> relationshipRequestList) {
        Map<Long, SupplierDataProcessingRelationship> existingRelationshipMap = supplier.getRelationshipList().stream()
                .filter(item -> item.getId() != null)
                .collect(java.util.stream.Collectors.toMap(SupplierDataProcessingRelationship::getId, item -> item));
        List<SupplierDataProcessingRelationship> relationshipList = new ArrayList<>();
        for (SupplierRelationshipRequestDTO relationshipRequest : relationshipRequestList) {
            SupplierDataProcessingRelationship relationship;
            if (relationshipRequest.id == null) {
                relationship = relationshipFromRequest(relationshipRequest);
            } else {
                relationship = existingRelationshipMap.get(relationshipRequest.id);
                if (relationship == null) {
                    throw new SupplierException("Relacao do fornecedor nao encontrada.");
                }
                updateRelationship(relationship, relationshipRequest);
            }
            if (relationship.getId() != null && !existingRelationshipMap.containsKey(relationship.getId())) {
                throw new SupplierException("Relacao do fornecedor nao encontrada.");
            }
            relationshipList.add(relationship);
        }
        Set<Long> submittedIdSet = relationshipList.stream().map(SupplierDataProcessingRelationship::getId)
                .filter(Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        existingRelationshipMap.forEach((relationshipId, existingRelationship) -> {
            if (!submittedIdSet.contains(relationshipId)) relationshipList.add(existingRelationship);
        });
        return relationshipList;
    }

    private List<SupplierDataProcessingRelationship> relationshipsFromRequest(
            List<SupplierRelationshipRequestDTO> relationshipRequestList) {
        return new ArrayList<>(relationshipRequestList.stream().map(this::relationshipFromRequest).toList());
    }

    private SupplierDataProcessingRelationship relationshipFromRequest(SupplierRelationshipRequestDTO request) {
        return new SupplierDataProcessingRelationship(request.id, required(request.serviceName),
                optional(request.description), optional(request.purpose), optional(request.personalDataCategories),
                optional(request.dataSubjectCategories), optional(request.processingActions), request.role,
                optional(request.roleAssessment), optional(request.storageLocations),
                Boolean.TRUE.equals(request.internationalTransfer), optional(request.transferMechanism),
                optional(request.retentionCriteria), optional(request.deletionOrReturnProcedure),
                optional(request.securityMeasures), optional(request.incidentNotificationChannel),
                optional(request.incidentNotificationExpectation), optional(request.subOperatorInformation),
                request.contractStatus, optional(request.contractReference), request.contractStartDate,
                request.contractEndDate, optional(request.responsibilitySummary), request.riskLevel,
                request.governanceStatus, optional(request.assessmentNotes), request.nextReviewDate,
                request.endedAt, request.dataDispositionStatus, optional(request.dataDispositionNotes));
    }

    private void updateRelationship(SupplierDataProcessingRelationship relationship,
            SupplierRelationshipRequestDTO request) {
        relationship.updateDetails(required(request.serviceName), optional(request.description),
                optional(request.purpose), optional(request.personalDataCategories),
                optional(request.dataSubjectCategories), optional(request.processingActions),
                request.role, optional(request.roleAssessment), optional(request.storageLocations),
                Boolean.TRUE.equals(request.internationalTransfer), optional(request.transferMechanism),
                optional(request.retentionCriteria), optional(request.deletionOrReturnProcedure),
                optional(request.securityMeasures), optional(request.incidentNotificationChannel),
                optional(request.incidentNotificationExpectation), optional(request.subOperatorInformation),
                request.contractStatus, optional(request.contractReference), request.contractStartDate,
                request.contractEndDate, optional(request.responsibilitySummary), request.riskLevel,
                request.governanceStatus, optional(request.assessmentNotes), request.nextReviewDate,
                request.endedAt, request.dataDispositionStatus, optional(request.dataDispositionNotes));
    }

    private Supplier requireSupplier(Long id) {
        if (id == null) throw new SupplierException("Fornecedor nao encontrado.");
        return persistencePort.findById(id).orElseThrow(() -> new SupplierException("Fornecedor nao encontrado."));
    }

    private void validateDuplicates(String normalizedName, String registrationIdentifier, Long excludedId) {
        if (persistencePort.existsByNormalizedOfficialName(normalizedName, excludedId))
            throw new SupplierException("Ja existe um fornecedor com esse nome oficial.");
        if (registrationIdentifier != null
                && persistencePort.existsByRegistrationIdentifier(registrationIdentifier, excludedId))
            throw new SupplierException("Ja existe um fornecedor com esse identificador de registro.");
    }

    private String normalizeIdentity(String value) {
        return Normalizer.normalize(required(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
    private String normalizeIdentifier(String value) { return validationService.isBlank(value) ? null : value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT); }
    private String required(String value) { return value.trim(); }
    private String optional(String value) { return validationService.isBlank(value) ? null : value.trim(); }
}
