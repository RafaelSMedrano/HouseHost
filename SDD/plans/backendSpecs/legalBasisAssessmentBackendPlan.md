# Legal Basis Assessment Backend Plan

## Governing Specs

- `SDD/specs/backendSpecs/legalBasisAssessmentSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- prerequisite: `SDD/specs/cantinhoDasLavandasMainSpec.md`

## Objective

Add structured, versioned and reviewable lawful-basis assessments to the
existing hexagonal `privacy` module without making the application pretend to
perform legal analysis automatically or coupling operational business flows to
the governance workflow.

This plan does not authorize implementation. The corresponding backend task
must be approved and executed separately.

## Current Backend Context

Relevant existing components are:

- `DataProcessingOperation` (domain/model; processing-operation model);
- `markReviewed` (domain/model; DataProcessingOperation);
- `DataProcessingOperationUseCase` (application/port/in; interface);
- `DataProcessingOperationReviewUseCase` (application/port/in; interface);
- `DataProcessingOperationService` (application/service; operation service);
- `DataProcessingOperationReviewService` (application/service; inventory review service);
- `DataProcessingOperationValidationService` (application/service; current required-text validation);
- `DataProcessingOperationPersistencePort` (application/port/out; interface);
- `PrivacyReviewerPort` (application/port/out; authenticated reviewer resolution);
- `DataProcessingOperationController` (adapter/in/rest; controller);
- `DataProcessingOperationJpaEntity` (adapter/out/persistence/entity; JPA entity);
- `DataProcessingOperationPersistenceAdapter` (adapter/out/persistence; adapter);
- `DataProcessingOperationCatalogService` (application/service; initial catalog);
- `DatabaseSchemaCompatibilityRunner` (configuration/infrastructure; idempotent schema compatibility);
- `SecurityConfig` (security adapter/in/config; administrator authorization).

The current `legalBasis` string remains a compatibility summary during this
task. Readiness and approval derive only from the new assessment records.

## Domain Design

Create:

```text
ProcessingLegalBasisAssessment (domain/model; entity)
LegalBasisType (domain/model; enum)
SensitiveDataLegalBasisType (domain/model; enum)
LegalBasisAssessmentStatus (domain/model; enum)
LegalBasisReadiness (domain/model; enum)
```

`ProcessingLegalBasisAssessment` state includes:

```text
id
processingOperationId
previousVersionId
purpose
legalBasisType
justification
personalDataCategories
necessityAssessment
legalReference
contractualContext
consentCollectionMechanism
consentEvidenceMechanism
consentWithdrawalMechanism
legitimateInterestDescription
legitimateExpectationAssessment
rightsAndImpactAssessment
safeguards
balanceConclusion
sensitiveData
sensitiveDataLegalBasisType
sensitiveDataIndispensabilityAssessment
status
version
submittedAt
approvedByUserId
approvedAt
rejectedByUserId
rejectedAt
rejectionReason
createdAt
updatedAt
```

The model contains lifecycle behavior for draft update, submission, approval,
rejection and supersession. Domain methods reject invalid transitions. Approved,
rejected and superseded versions are immutable; changing approved content starts
a new draft revision.

The model contains no Spring MVC or JPA annotations.

## Validation Design

Create:

```text
ProcessingLegalBasisAssessmentValidationService (application/service; class)
```

Validation is separated from orchestration and enforces:

- base evidence common to every assessment;
- concrete reference for legal or regulatory obligation;
- contractual context for contract or pre-contract;
- consent collection, evidence and withdrawal mechanisms for consent;
- structured interest, expectation, impact, safeguard and conclusion fields for
  legitimate interest;
- separate sensitive-data basis and indispensability assessment when sensitive
  data are declared;
- field length limits and rejection of blank narratives;
- immutable approved historical versions;
- required rejection reason and valid lifecycle transitions.

DTOs receive enums directly. No status or basis parser is introduced.

## Application Contracts

Create:

```text
ProcessingLegalBasisAssessmentUseCase (application/port/in; interface)
ProcessingLegalBasisAssessmentService (application/service; class)
ProcessingLegalBasisAssessmentPersistencePort (application/port/out; interface)
PrivacyLegalBasisAuditPort (application/port/out; interface)
```

Primary use-case operations are:

```text
createDraft(operationId, request)
findByOperation(operationId)
findById(assessmentId)
updateDraft(assessmentId, request)
submit(assessmentId)
approve(assessmentId, authenticatedEmail)
reject(assessmentId, authenticatedEmail, request)
createRevision(assessmentId)
```

`ProcessingLegalBasisAssessmentService` verifies the owning operation through
`DataProcessingOperationPersistencePort`, resolves the reviewer through the
existing `PrivacyReviewerPort`, delegates evidence validation, persists through
its assessment port and records audit through `PrivacyLegalBasisAuditPort`.

Do not create a lookup service. Reviewer resolution remains an external
dependency expressed by the existing port, and assessment persistence queries
belong to the assessment persistence port.

The existing generic operation review remains separate. It can mark the whole
inventory entry reviewed, but cannot approve an assessment.

## DTO And Response Design

Create DTOs under `privacy/application/dto`, including:

```text
ProcessingLegalBasisAssessmentRequestDTO
ProcessingLegalBasisAssessmentResponseDTO
LegalBasisAssessmentRejectionRequestDTO
DataProcessingOperationLegalBasisSummaryDTO
```

Request DTOs use `LegalBasisType` and `SensitiveDataLegalBasisType` directly.
Responses include lifecycle evidence, version relationships and a calculated
readiness summary without exposing unnecessary authenticated-user details.

Extend operation detail/list responses with lawful-basis readiness and concise
current-assessment summaries. Long assessment narratives remain in assessment
detail responses rather than operation list responses.

## Persistence Design

Create:

```text
ProcessingLegalBasisAssessmentJpaEntity (adapter/out/persistence/entity; JPA entity)
ProcessingLegalBasisAssessmentJpaRepository (adapter/out/persistence; repository)
ProcessingLegalBasisAssessmentPersistenceMapper (adapter/out/persistence; mapper)
ProcessingLegalBasisAssessmentPersistenceAdapter (adapter/out/persistence; adapter)
```

Store assessments in `processing_legal_basis_assessments`. Use a foreign key to
`data_processing_operations`, explicit enum strings, timestamps and optimistic
versioning where consistent with the project. Enforce uniqueness of operation,
purpose identity and assessment version with an index/constraint that prevents
duplicate migration and concurrent duplicate versions.

The persistence port works only with domain models and uses identifiers with
`Optional`, `List` and other suffixes required by the architecture spec.

Add idempotent MySQL compatibility for the table, indexes and foreign keys in
`DatabaseSchemaCompatibilityRunner`. Startup must preserve approved content and
must not reset lifecycle state.

## Catalog Migration

Extend `DataProcessingOperationCatalogService` or introduce one clearly scoped
catalog collaborator so current operations receive idempotent candidate
assessment drafts by stable operation code.

Migration rules:

- no current assessment is approved automatically;
- existing approved human records are never overwritten;
- repeated startup creates no duplicate draft or version;
- combined legacy basis labels are split into candidate purpose/basis drafts;
- marketing remains inactive and is not made ready;
- assessment readiness is returned independently from operation status;
- the current `legalBasis` column remains available as a legacy summary during
  this task and is not treated as evidence.

The implementation report must list candidate content seeded and identify every
decision that still requires controller or legal confirmation.

## REST API

Create:

```text
ProcessingLegalBasisAssessmentController (adapter/in/rest; controller)
```

Expose protected operations:

```text
POST /data-processing-operations/{operationId}/legal-basis-assessments
GET  /data-processing-operations/{operationId}/legal-basis-assessments
GET  /legal-basis-assessments/{assessmentId}
PUT  /legal-basis-assessments/{assessmentId}
POST /legal-basis-assessments/{assessmentId}/submit
POST /legal-basis-assessments/{assessmentId}/approve
POST /legal-basis-assessments/{assessmentId}/reject
POST /legal-basis-assessments/{assessmentId}/revisions
```

The controller depends on the use case, extracts the authenticated email for
approval/rejection and contains no lifecycle or legal-evidence rules.

All routes are restricted to `CEO`, `CTO` and `ADMIN` by `SecurityConfig`.

## Audit Integration

Implement `PrivacyLegalBasisAuditPort` through a privacy-owned integration
adapter that calls the audit module. Record:

```text
LEGAL_BASIS_ASSESSMENT_CREATED
LEGAL_BASIS_ASSESSMENT_UPDATED
LEGAL_BASIS_ASSESSMENT_SUBMITTED
LEGAL_BASIS_ASSESSMENT_APPROVED
LEGAL_BASIS_ASSESSMENT_REJECTED
LEGAL_BASIS_ASSESSMENT_SUPERSEDED
```

Use the privacy-governance processing operation applicable to this governance
activity, adding or correcting a catalog code through SDD order if the
implementation review finds none adequate. Metadata is limited to IDs, basis,
status and version and excludes narratives, legal reasoning and full requests.

## Error Contract

Map expected validation, not-found, conflict and lifecycle errors through the
existing shared response contract. Invalid enum JSON follows the controlled
global client-error behavior. Do not return stack traces or persistence details.

## Verification Strategy

Add focused tests for:

- every supported enum and conditional evidence rule;
- separate sensitive-data evidence;
- valid and invalid lifecycle transitions;
- approved-version immutability and revision/supersession;
- reviewer identity and timestamps;
- readiness calculation;
- persistence mapping of every field;
- idempotent candidate migration without automatic approval;
- marketing remaining inactive;
- REST contracts and administrator authorization;
- audit facts and minimized metadata;
- full Maven suite and MySQL compatibility when available.

## Out Of Scope

- automatic legal advice, lawful-basis recommendation or compliance
  certification;
- automatic approval of current operations;
- public display of internal assessment narratives;
- policy-text generation;
- direct runtime shutdown of booking, authentication, finance or audit because
  an existing migrated assessment is pending;
- consent collection for marketing or reactivation of marketing;
- file uploads, legal opinions or contract-document storage;
- a general DPIA platform beyond the assessment evidence defined by the spec.
