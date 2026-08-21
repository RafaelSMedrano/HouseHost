# Implementation Report — Task 006b Legal Basis Assessments

## Task And Execution

- Task: `006b DONE — Implement Legal Basis Assessments Backend`.
- Execution date: 26 July 2026.
- Implementation file: `SDD/implementation/implementation-order.md`.

## Documents Read

- `SDD/specs/sddSpec.md`;
- `SDD/specs/cantinhoDasLavandasMainSpec.md`;
- `SDD/specs/lgpdGovernanceSpec.md`;
- `SDD/specs/moduleArchitectureSpec.md`;
- `SDD/specs/backendSpecs/legalBasisAssessmentSpec.md`;
- `SDD/plans/backendSpecs/legalBasisAssessmentBackendPlan.md`;
- `SDD/implementation/task-bootstrap.md`;
- `SDD/implementation/implementation-order.md`;
- `SDD/tasks/backendSpecs/006b-DONE-legal-basis-assessments.md`.

## Files Created

- lawful-basis domain types under `src/main/java/com/househost/privacy/domain/model/`:
  `ProcessingLegalBasisAssessment`, `LegalBasisType`,
  `SensitiveDataLegalBasisType`, `LegalBasisAssessmentStatus` and
  `LegalBasisReadiness`;
- assessment DTOs under `src/main/java/com/househost/privacy/application/dto/`:
  `ProcessingLegalBasisAssessmentRequestDTO`,
  `ProcessingLegalBasisAssessmentResponseDTO`,
  `LegalBasisAssessmentRejectionRequestDTO` and
  `DataProcessingOperationLegalBasisSummaryDTO`;
- application contracts `ProcessingLegalBasisAssessmentUseCase`,
  `ProcessingLegalBasisAssessmentCatalogUseCase`,
  `ProcessingLegalBasisAssessmentPersistencePort` and
  `PrivacyLegalBasisAuditPort`;
- application services `ProcessingLegalBasisAssessmentService`,
  `ProcessingLegalBasisAssessmentValidationService`,
  `ProcessingLegalBasisAssessmentCatalogService` and
  `LegalBasisAssessmentReadinessService`;
- REST adapter `ProcessingLegalBasisAssessmentController`;
- persistence entity, mapper, repository and adapter for
  `processing_legal_basis_assessments`;
- integration adapter `PrivacyLegalBasisAuditAdapter`;
- focused domain, validation, service, catalog, mapper, audit and authorization
  tests for the assessment flow;
- `SDD/ImplementationReport/2026-07-26-006b-legal-basis-assessments.md`.

## Files Changed

- `pom.xml`;
- `src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java`;
- `src/main/java/com/househost/security/adapter/in/config/SecurityConfig.java`;
- `src/main/java/com/househost/privacy/domain/model/DataProcessingOperationCodes.java`;
- `src/main/java/com/househost/privacy/domain/model/DataProcessingOperationNames.java`;
- `src/main/java/com/househost/privacy/application/service/DataProcessingOperationCatalogService.java`;
- `src/main/java/com/househost/privacy/adapter/in/config/DataProcessingOperationCatalogInitializer.java`;
- `src/main/java/com/househost/privacy/application/dto/DataProcessingOperationResponseDTO.java`;
- `src/main/java/com/househost/privacy/application/service/DataProcessingOperationService.java`;
- `src/main/java/com/househost/privacy/application/service/DataProcessingOperationReviewService.java`;
- existing privacy service and catalog tests;
- `SDD/implementation/task-bootstrap.md`;
- `SDD/implementation/implementation-order.md`;
- the task file, renamed with `DONE` after verification.

## Flows Implemented

The privacy module now stores one controlled ordinary-data basis for one
explicit purpose in each assessment. Drafts can be edited, complete evidence is
required before submission, authorized reviewers can approve or reject, and
approved records cannot be edited. A later change is represented by a linked
draft version; approving it supersedes the previous approved version while
preserving history.

Conditional evidence is enforced for legal obligation, contract or
pre-contract, consent, legitimate interest and sensitive-data treatment. The
existing free-text basis and generic operation review remain compatibility and
inventory information only; neither produces approved readiness.

The API exposes create, concise list, detail, update, submit, approve, reject
and revision routes. All routes are restricted to `CEO`, `CTO` and `ADMIN`.
Operation responses expose derived readiness and concise version summaries.

Startup creates idempotent draft candidates for the known active operations,
splitting combined legacy labels into separate purposes and bases where
applicable. Marketing remains inactive, is skipped by migration and cannot
receive a new assessment. Candidate records are deliberately incomplete and
never approved automatically.

Lifecycle events use the new `PRIVACY_GOVERNANCE` processing operation. Audit
metadata contains only assessment ID through the audited entity reference,
operation ID, basis, status and version; legal narratives and request payloads
are excluded.

## Technical And MVP Decisions

- `PRIVACY_GOVERNANCE` was added because no existing processing-operation code
  accurately classified the governance and lawful-basis audit purpose.
- Stable known operation codes drive migration. Custom operations receive no
  guessed basis from startup.
- Current readiness is calculated from the latest non-superseded version of
  each normalized purpose. An unresolved rejection, review or draft prevents
  approved readiness.
- The table uses a unique operation, normalized-purpose and version key.
  Repeated revision requests return an existing pending revision, while later
  revisions advance beyond rejected historical versions.
- The legacy `legalBasis` column is preserved and intentionally ignored by the
  readiness calculation.
- `spring-security-test` was added only in test scope to verify real route rules
  from `SecurityConfig`.

## Difficulties, Problems And Resolutions

The initial authorization test mocked the `OncePerRequestFilter` itself. A
mocked `GenericFilterBean` had no initialized logger and prevented MockMvc from
starting. The test was corrected to use the real JWT filter with only its
application dependency mocked, so the actual security chain is exercised.

The schema runner executes after Hibernate startup. Therefore the compatibility
method was made responsible for checking and adding named indexes and foreign
keys even when Hibernate has already created the table.

## Tests And Verification

- focused lifecycle, conditional validation, readiness, service,
  idempotent-catalog, mapper, minimized-audit and authorization tests: passed;
- full `HOUSEHOST_LOGIN_LIMIT_HMAC_SECRET=test-only-secret ./mvnw -q test`:
  90 tests passed, with zero failures and zero errors;
- `git diff --check`: passed;
- domain scan found no JPA or web annotations in privacy domain models.

A live MySQL startup was not executed because no disposable MySQL environment
was configured for this task. The MySQL DDL path is implemented idempotently and
the persistence mapper is covered, but the named indexes and foreign keys still
need exercise in deployment or an integration environment with MySQL.

## Human And Legal Work Still Required

Technical implementation is complete. Legal correctness is not approved by the
software or by this task. The seeded candidates remain drafts. An accountable
human must complete the evidence, confirm each purpose and applicable legal
basis, provide concrete legal references where required, perform legitimate
interest balancing where used, submit the assessment and record approval or
rejection through the protected workflow.

## Prerequisite And Acceptance Review

The result was compared with the mother spec, LGPD governance spec, module
architecture spec, lawful-basis spec, backend plan, task acceptance criteria and
implementation rules. The domain remains framework-free, persistence remains
behind a port and adapter, reviewer resolution reuses `PrivacyReviewerPort`,
audit crosses a privacy-owned port, DTOs use domain enums directly, marketing
remains inactive and no automated legal conclusion was introduced.

The review found no unresolved contradiction. All technical acceptance
criteria are satisfied subject to the explicitly recorded live-MySQL
verification and the separate human/legal approval of assessment content.
