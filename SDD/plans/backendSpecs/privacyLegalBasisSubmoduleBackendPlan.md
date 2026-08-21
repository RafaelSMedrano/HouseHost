# Privacy Legal Basis Submodule Backend Plan

## Governing Specs

- `SDD/specs/backendSpecs/privacyLegalBasisSubmoduleSpec.md`
- `SDD/specs/backendSpecs/privacyHexagonalSubmodulesSpec.md`
- `SDD/specs/backendSpecs/legalBasisAssessmentSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- prerequisite: `SDD/specs/cantinhoDasLavandasMainSpec.md`

## Objective

Move lawful-basis definitions and assessment behavior into
`com.househost.privacy.legalbasis`, replace direct access to processing
persistence with direct application-service collaboration and complete the
parent privacy composition without changing legal evidence or external API
behavior.

This plan does not authorize implementation. Task `008b` executes it after
`007b` when explicitly approved.

## Target Package Migration

Move legal-basis-owned types to:

```text
privacy/legalbasis/domain/model/
  ProcessingLegalBasisAssessment
  LegalBasisType
  SensitiveDataLegalBasisType
  LegalBasisAssessmentStatus
  LegalBasisReadiness

privacy/legalbasis/application/dto/
  ProcessingLegalBasisAssessmentRequestDTO
  ProcessingLegalBasisAssessmentResponseDTO
  LegalBasisAssessmentRejectionRequestDTO
  DataProcessingOperationLegalBasisSummaryDTO

privacy/legalbasis/application/port/in/
  ProcessingLegalBasisAssessmentUseCase
  ProcessingLegalBasisAssessmentCatalogUseCase

privacy/legalbasis/application/port/out/
  ProcessingLegalBasisAssessmentPersistencePort
  LegalBasisReviewerPort
  PrivacyLegalBasisAuditPort

privacy/legalbasis/application/service/
  ProcessingLegalBasisAssessmentService
  ProcessingLegalBasisAssessmentValidationService
  ProcessingLegalBasisAssessmentCatalogService
  LegalBasisAssessmentReadinessService

privacy/legalbasis/adapter/in/rest/
  ProcessingLegalBasisAssessmentController

privacy/legalbasis/adapter/out/persistence/
  repository, adapter, mapper and entity

privacy/legalbasis/adapter/out/integration/
  reviewer and audit adapters
```

## Normative Definition Design

Move `LegalBasisType` with its existing LGPD references and preserve enum names.
Add equivalent controlled normative references to sensitive-data types when the
current domain requires them for display or evidence.

Prefer enum properties or one immutable domain value object for normative
metadata. Do not create a JPA entity or database row per article. FNRH, tax,
contractual and other concrete references remain fields of the assessment that
the controller reviewed.

## Direct Processing Service Collaboration

Replace `DataProcessingOperationPersistencePort` dependencies inside
`ProcessingLegalBasisAssessmentService` and
`ProcessingLegalBasisAssessmentCatalogService` with the processing application
service capability created by task `007b`.

The call provides only operation ID, code and status. It supports existence
validation, inactive-marketing protection and candidate association. Legalbasis
does not import processing persistence or JPA packages.

## Parent Aggregate Composition

Complete a thin parent inbound composition for existing processing-operation
responses that include lawful-basis readiness and assessment summaries. The
composition calls the processing query and legal-basis readiness/query services
without putting legal-basis persistence back into the processing core.

Preserve current endpoint paths and JSON properties so the existing
administrative frontend needs no functional change. The parent composition has
no domain entity or repository and must not duplicate lifecycle rules.

## Persistence And Approved Evidence

Keep `processing_legal_basis_assessments`, constraints, enum strings and all
existing data unchanged. In particular, preserve:

- the 12 currently approved version-1 assessments;
- purposes, narratives and concrete legal references;
- reviewer identifiers;
- submission and review timestamps;
- previous-version links and future revision behavior.

The package migration must not run update, submit, approve, reject or revision
operations against existing records.

## Initialization And Audit

Keep candidate initialization after processing catalog initialization and make
it idempotent. Existing human-approved content is never overwritten.

Keep audit integration behind `PrivacyLegalBasisAuditPort`. The adapter may call
the audit module, but the legal-basis service does not depend directly on
`AuditEventService`. Event names and minimized metadata remain unchanged.

## Verification Strategy

Verify:

- conditional evidence and lifecycle tests remain green;
- all ordinary and sensitive basis values preserve serialization and JPA values;
- LGPD reference responses remain unchanged;
- 12 approved database records remain approved after startup verification;
- legalbasis application imports no processing persistence or JPA type;
- direct service calls have no circular dependency;
- REST authorization and response compatibility remain intact;
- catalog startup creates no duplicate or overwritten assessment;
- focused tests, full Maven suite and `git diff --check` pass.

## Out Of Scope

- changing or reapproving assessment narratives;
- creating an editable database of legislation;
- automatic legal advice or certification;
- public policy versioning;
- changing frontend behavior;
- changing stable endpoint paths or database schema names.
