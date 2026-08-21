# Task 006b DONE — Implement Legal Basis Assessments Backend

## Status

Completed on 26 July 2026 after acceptance and prerequisite review.

## Implementation Area

Backend (`b`).

## Objective

Create the structured lawful-basis assessment domain, validation, persistence,
version lifecycle, protected REST API, migration and audit integration in the
`privacy` module.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/backendSpecs/legalBasisAssessmentSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/legalBasisAssessmentBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`

## Dependencies

The existing privacy processing-operation inventory, authenticated reviewer
resolution and audit infrastructure. No legal conclusion is a technical
dependency: current operations can be migrated as drafts awaiting accountable
human review.

## Scope

- Create assessment, lawful-basis, sensitive-basis, lifecycle and readiness
  domain types without JPA annotations.
- Implement conditional evidence validation for contract, legal obligation,
  consent, legitimate interest and sensitive-data treatment.
- Implement immutable approved versions and draft revision/supersession.
- Create assessment request, response, rejection and readiness DTOs using domain
  enums directly.
- Create use case, application service, persistence port and privacy-owned audit
  port.
- Reuse `PrivacyReviewerPort`; do not create a reviewer lookup service.
- Create JPA entity, repository, mapper and persistence adapter.
- Add idempotent MySQL compatibility for tables, indexes and constraints.
- Add protected create/list/detail/update/submit/approve/reject/revision REST
  operations.
- Restrict all assessment endpoints to `CEO`, `CTO` and `ADMIN`.
- Extend operation responses with concise readiness without treating the legacy
  basis string or generic review as approval.
- Seed idempotent draft candidates for current non-marketing operations without
  automatic approval or overwrite of human evidence.
- Keep marketing inactive throughout migration.
- Audit material lifecycle facts with minimized metadata.
- Add focused domain, validation, service, persistence, migration, HTTP,
  authorization and audit tests.

## Out Of Scope

- Administrative frontend changes.
- Automatic legal advice, recommendation, approval or certification.
- Supplying final legal references or balance conclusions on behalf of the
  controller.
- Public disclosure of internal assessment narratives.
- File uploads or a general DPIA/document-management platform.
- Marketing reactivation or consent collection.
- Removing the legacy operation `legalBasis` column in the same task.
- Interrupting existing operational flows solely because migrated assessments
  await review.

## Expected Files

Expected additions or changes include:

```text
src/main/java/com/househost/privacy/domain/model/ProcessingLegalBasisAssessment.java
src/main/java/com/househost/privacy/domain/model/LegalBasisType.java
src/main/java/com/househost/privacy/domain/model/SensitiveDataLegalBasisType.java
src/main/java/com/househost/privacy/domain/model/LegalBasisAssessmentStatus.java
src/main/java/com/househost/privacy/domain/model/LegalBasisReadiness.java
src/main/java/com/househost/privacy/application/dto/...
src/main/java/com/househost/privacy/application/port/in/ProcessingLegalBasisAssessmentUseCase.java
src/main/java/com/househost/privacy/application/port/out/ProcessingLegalBasisAssessmentPersistencePort.java
src/main/java/com/househost/privacy/application/port/out/PrivacyLegalBasisAuditPort.java
src/main/java/com/househost/privacy/application/service/ProcessingLegalBasisAssessmentService.java
src/main/java/com/househost/privacy/application/service/ProcessingLegalBasisAssessmentValidationService.java
src/main/java/com/househost/privacy/adapter/in/rest/ProcessingLegalBasisAssessmentController.java
src/main/java/com/househost/privacy/adapter/out/persistence/...
src/main/java/com/househost/privacy/adapter/out/integration/PrivacyLegalBasisAuditAdapter.java
src/main/java/com/househost/privacy/application/service/DataProcessingOperationCatalogService.java
src/main/java/com/househost/privacy/application/dto/DataProcessingOperationResponseDTO.java
src/main/java/com/househost/security/adapter/in/config/SecurityConfig.java
src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java
src/main/java/com/househost/shared/exception/GlobalExceptionHandler.java
src/test/...
```

Names may change only to follow an existing convention or required architecture,
and every adjustment must be traceable in the implementation report.

## Acceptance Criteria

- One assessment belongs to one operation, one explicit purpose and one
  controlled ordinary-data lawful basis.
- Multiple purposes or bases are represented by separate assessments rather
  than a combined string.
- DTOs use domain enums directly and introduce no enum parser.
- Every assessment requires purpose, justification, data categories and
  necessity evidence.
- Legal obligation requires a concrete reference and obligation explanation.
- Contract/pre-contract requires contractual or data-subject-requested context.
- Consent requires collection, evidence and withdrawal mechanisms and cannot
  reactivate marketing.
- Legitimate interest requires interest, expectation, impact, safeguards and a
  balance conclusion.
- Sensitive data require their separate basis, indispensability and safeguards.
- Invalid lifecycle transitions are rejected.
- Approved versions are immutable; change creates a linked draft revision and
  approval supersedes the previous current version.
- Approval/rejection records authenticated reviewer, time and required reason.
- Generic operation review and legacy basis text do not produce approved
  readiness.
- Existing operations receive no automatic approval during migration.
- Repeated startup does not duplicate versions or overwrite approved evidence.
- Marketing remains inactive and has no current-purpose readiness.
- List responses stay concise; detail responses contain the permitted evidence
  and history.
- All assessment endpoints are restricted to administrator roles.
- Audit metadata excludes complete narratives, sensitive-data descriptions and
  request payloads.
- Domain models contain no JPA or web annotations, and persistence follows the
  module architecture spec.
- Relevant focused tests and the full Maven suite pass.

## Verification Commands

At minimum:

```text
./mvnw test
git diff --check
```

The report must identify any database-specific verification not run and why.

## Required Report

Create after implementation:

```text
SDD/ImplementationReport/YYYY-MM-DD-006b-legal-basis-assessments.md
```

The report must distinguish technical completion from outstanding human/legal
approval of assessment content and include the prerequisite review required by
`SDD/specs/sddSpec.md`.
