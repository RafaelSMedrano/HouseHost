# Privacy Legal Basis Submodule Spec

## Specification

The Privacy Legal Basis submodule is the hexagonal capability that owns the
project's controlled lawful-basis vocabulary and the structured, versioned and
human-approved assessments that connect a processing purpose to an applicable
LGPD basis.

It contains the domain objects that represent each supported ordinary-data and
sensitive-data lawful-basis hypothesis and their normative references. These
objects describe the law supported by the product; they do not decide by
themselves whether a basis is correct for a real treatment.

## Scope

This spec structurally specializes the behavior defined by
`legalBasisAssessmentSpec`. It owns assessment evidence, validation, lifecycle,
approval, revision, supersession, readiness, catalog candidates, persistence,
reviewer resolution and privacy-owned audit integration.

An assessment stores the owning processing-operation identifier but does not
import processing persistence infrastructure or assume ownership of the
operation. It confirms operation context through a direct application-service
call to the processing submodule.

This refactoring preserves the existing `processing_legal_basis_assessments`
table, all approved content and the protected REST workflow.

## Capabilities

### Apply A Complete Hexagon

The submodule uses this package root:

```text
com.househost.privacy.legalbasis
├── domain/model
├── application/dto
├── application/port/in
├── application/port/out
├── application/service
├── adapter/in/rest
├── adapter/out/integration
├── adapter/out/persistence
└── adapter/out/persistence/entity
```

Assessment models and lawful-basis definitions remain independent from JPA,
HTTP, authentication and audit infrastructure.

### Own Lawful-Basis Definitions

The following concepts belong to the legal-basis domain:

- `LegalBasisType` and the LGPD article associated with each supported ordinary
  basis;
- `SensitiveDataLegalBasisType` and the applicable sensitive-data normative
  reference;
- `ProcessingLegalBasisAssessment`;
- `LegalBasisAssessmentStatus`;
- `LegalBasisReadiness`.

Normative metadata may be represented by enum properties or an immutable domain
value object. The implementation must not create one JPA entity, table or
mutable administrative record for each LGPD article when the current closed
domain vocabulary already satisfies the requirement.

Concrete sector rules such as FNRH, tax obligations or a specific contract
remain evidence inside the applicable assessment. They are not promoted to a
global law catalog unless a later product spec requires editable legal-source
governance.

### Own Assessment And Approval Behavior

The submodule preserves all capabilities from `legalBasisAssessmentSpec`,
including conditional evidence, controlled enums, immutable approved versions,
reviewer and timestamp evidence, rejection, revision, supersession, readiness
and minimized audit facts.

### Collaborate Directly With Processing Services

`ProcessingLegalBasisAssessmentService` and assessment-catalog orchestration may
call the processing application service directly to:

- confirm that the operation exists;
- read its stable code and operational status;
- prevent a current marketing assessment;
- associate idempotent candidates with current catalog operations.

They do not receive `DataProcessingOperationPersistencePort`, JPA repositories
or JPA entities. The call direction remains legalbasis to processing. A parent
privacy composition supplies combined operation/readiness responses without
creating the opposite service dependency.

### Preserve Normative And Human Evidence

Moving lawful-basis objects must preserve enum names and database values. The
LGPD reference returned for a basis remains derived from the controlled domain
definition. Assessment-specific legal references, narratives and approval
evidence remain persisted exactly as reviewed.

The refactoring must not reseed, overwrite, resubmit or reapprove existing
assessments. Startup remains idempotent.

## Prerequisite Specs

- `SDD/specs/backendSpecs/legalBasisAssessmentSpec.md`
- `SDD/specs/backendSpecs/privacyHexagonalSubmodulesSpec.md`

## Spec Degree

3.
