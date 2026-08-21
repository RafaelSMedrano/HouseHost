# Privacy Processing Submodule Backend Plan

## Governing Specs

- `SDD/specs/backendSpecs/privacyProcessingSubmoduleSpec.md`
- `SDD/specs/backendSpecs/privacyHexagonalSubmodulesSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- prerequisite: `SDD/specs/cantinhoDasLavandasMainSpec.md`

## Objective

Move processing-operation inventory behavior from the undivided privacy
packages into `com.househost.privacy.processing`, establish its complete
hexagonal ownership and prepare a service-level boundary that the legal-basis
submodule can use without accessing processing persistence.

This plan does not authorize implementation. Task `007b` executes it when
explicitly approved.

## Current Backend Context

The current processing capability is spread across shared privacy packages and
is coupled to legal-basis persistence in:

- `DataProcessingOperationService` (application/service), which queries
  `ProcessingLegalBasisAssessmentPersistencePort` to build readiness;
- `DataProcessingOperationReviewService` (application/service), which repeats
  the same cross-capability query;
- `DataProcessingOperationResponseDTO` (application/dto), which combines the
  operation and legal-basis summary;
- `DataProcessingOperationCatalogInitializer` (adapter/in/config), which starts
  both catalogs.

External audit adapters import `DataProcessingOperationCodes` from the current
shared privacy domain package.

## Target Package Migration

Move processing-owned types to:

```text
privacy/processing/domain/model/
  DataProcessingOperation
  DataProcessingOperationCodes
  DataProcessingOperationNames
  DataProcessingOperationStatus

privacy/processing/application/dto/
  DataProcessingOperationRequestDTO
  DataProcessingOperationResponseDTO or a processing-only equivalent
  DataProcessingOperationStatusRequestDTO

privacy/processing/application/port/in/
  DataProcessingOperationUseCase
  DataProcessingOperationReviewUseCase
  DataProcessingOperationCatalogUseCase

privacy/processing/application/port/out/
  DataProcessingOperationPersistencePort
  ProcessingOperationReviewerPort

privacy/processing/application/service/
  DataProcessingOperationService
  DataProcessingOperationReviewService
  DataProcessingOperationCatalogService
  DataProcessingOperationValidationService

privacy/processing/adapter/in/rest/
  processing-owned REST entry points where no aggregate composition is needed

privacy/processing/adapter/out/persistence/
  repository, adapter, mapper and entity
```

`PersonalDataMaskingService` must be assigned explicitly during implementation:
move it to the processing application only if processing has a real consumer;
otherwise preserve it temporarily and document the reason rather than treating
the parent package as a permanent miscellaneous folder.

## Service Collaboration Contract

Add a focused processing application query returning the minimum operation
context required by legal basis, for example an immutable application record
containing:

```text
operationId
operationCode
status
```

The type belongs in `processing/application/records` and ends in `Record`.
Identifiers using it follow the record naming conventions.

The legal-basis application may call this processing service directly. No
internal integration port is required for this in-process collaboration.

## Remove Reverse Core Coupling

Remove legal-basis persistence and readiness calculation from
`DataProcessingOperationService` and `DataProcessingOperationReviewService`.
They return processing-owned results only.

Preserve the existing HTTP JSON contract through a transitional parent privacy
composition seam. Task `008b` completes that seam when the legal-basis package
is moved. No frontend change or endpoint rename is authorized.

## Persistence And Data Preservation

Keep table `data_processing_operations`, column names, identifiers, status
values and stable operation codes unchanged. Package movement must not cause a
new table or destructive schema migration.

Update all audit adapters and tests that import processing codes. Event-to-code
mapping remains identical.

## Initialization

Keep processing catalog initialization idempotent and ordered before legal-basis
candidate initialization. The parent initializer may remain as a thin startup
composition because it coordinates both submodules.

## Verification Strategy

Verify:

- processing domain and application do not import legalbasis persistence;
- domain types contain no JPA or web annotations;
- JPA mapping reconstructs every field;
- catalog initialization and inactive marketing behavior remain unchanged;
- external audit adapters retain their operation codes;
- CRUD, review, status and lookup tests pass;
- existing endpoint paths and JSON properties remain compatible;
- full Maven suite and `git diff --check` pass.

## Out Of Scope

- moving legal-basis-owned classes;
- changing assessment content or approval status;
- database table or endpoint renaming;
- public privacy-policy governance;
- frontend redesign;
- functional changes to inventory content.
