# Task 008b DONE — Extract Privacy Legal Basis Hexagonal Submodule

## Status

Completed on 27 July 2026 after explicit user approval, automated verification,
successful application startup and read-only confirmation that all 12 approved
assessment records remained unchanged.

## Implementation Area

Backend (`b`).

## Objective

Move lawful-basis definitions and assessment governance into the independent
`privacy.legalbasis` hexagon, use direct service collaboration with processing
and complete the two-submodule privacy boundary without altering approved legal
evidence.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/backendSpecs/legalBasisAssessmentSpec.md`
- `SDD/specs/backendSpecs/privacyHexagonalSubmodulesSpec.md`
- `SDD/specs/backendSpecs/privacyLegalBasisSubmoduleSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/privacyLegalBasisSubmoduleBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`

## Dependencies

- completed task `007b`;
- the existing 12 approved assessment records and their audit evidence;
- authenticated reviewer resolution and audit infrastructure.

## Scope

- Create the `com.househost.privacy.legalbasis` domain, application and adapter
  package structure.
- Move assessment, lifecycle, readiness, ordinary-basis and sensitive-basis
  domain types.
- Move assessment DTOs, ports, services, REST controller, persistence and
  integration adapters.
- Preserve ordinary-basis LGPD references and model sensitive-basis normative
  references as controlled immutable domain metadata where required.
- Replace processing persistence dependencies with direct calls to the
  processing application service capability from task `007b`.
- Complete the parent privacy response composition for operation details and
  readiness.
- Keep legal-basis audit behind its output port.
- Preserve catalog ordering and idempotency.
- Move and update all focused tests and add architecture checks.
- Verify the approved records before and after startup without mutating them.

## Out Of Scope

- Editing, resubmitting or reapproving assessment content.
- Creating a database entity for each LGPD article.
- Automatic legal conclusions.
- Database, endpoint or frontend redesign.
- Public privacy-policy version governance.

## Acceptance Criteria

- All legal-basis business types belong to `privacy.legalbasis` and form a
  complete hexagonal submodule.
- Legalbasis domain contains no JPA, HTTP, authentication or audit
  infrastructure imports.
- Legalbasis application imports no processing persistence, repository, JPA
  entity or adapter.
- Legalbasis services obtain minimum operation context through direct service
  collaboration with processing.
- The call graph contains no circular service dependency.
- `LegalBasisType` retains every current enum value and LGPD reference.
- Sensitive-data bases retain stable enum values and expose controlled
  normative references when required by the response contract.
- Assessment validation, lifecycle, revision, readiness and audit behavior are
  unchanged.
- The 12 approved assessments retain status, version, reviewer, timestamps and
  content.
- Startup does not duplicate or overwrite assessment records.
- Existing protected endpoints and JSON responses remain compatible with the
  frontend.
- Parent privacy packages contain only justified composition entry points and
  no unowned domain or persistence class.
- Focused tests, architecture checks and the full Maven suite pass.

## Verification Commands

At minimum:

```text
./mvnw test
rg -n "privacy\.processing\..*(persistence|adapter)" src/main/java/com/househost/privacy/legalbasis/application src/main/java/com/househost/privacy/legalbasis/domain
git diff --check
```

The implementation report must also include a read-only database comparison of
the approved assessment records before and after the migration.

## Required Report

Create after implementation:

```text
SDD/ImplementationReport/2026-07-27-008b-extract-privacy-legal-basis-submodule.md
```
