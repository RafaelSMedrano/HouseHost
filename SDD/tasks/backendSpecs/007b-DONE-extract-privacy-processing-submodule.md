# Task 007b DONE — Extract Privacy Processing Hexagonal Submodule

## Status

Completed on 27 July 2026 after explicit user approval, acceptance review and
successful full test execution.

## Implementation Area

Backend (`b`).

## Objective

Move processing-operation inventory behavior into the independent
`privacy.processing` hexagon, expose the minimum service capability required by
legal basis and remove legal-basis persistence from the processing core while
preserving external behavior.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/backendSpecs/privacyHexagonalSubmodulesSpec.md`
- `SDD/specs/backendSpecs/privacyProcessingSubmoduleSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/privacyProcessingSubmoduleBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`

## Dependencies

The completed processing inventory, security/audit catalog and legal-basis
assessment implementation. Task `006b` must remain complete and its approved
data must not be changed.

## Scope

- Create the `com.househost.privacy.processing` domain, application and adapter
  package structure.
- Move all processing-operation domain models, DTOs, ports, services, REST and
  persistence types to their owning layers.
- Add a focused processing query/service record with operation ID, stable code
  and status for direct service collaboration.
- Remove assessment persistence and legal readiness calculation from processing
  core services.
- Preserve the current combined HTTP response through the documented parent
  composition seam.
- Keep catalog initialization idempotent and marketing inactive.
- Update all imports of stable operation codes in external audit adapters.
- Preserve table, columns, IDs, enum values, endpoints and JSON properties.
- Move and update focused tests without weakening assertions.
- Add architecture checks for forbidden legalbasis persistence imports.

## Out Of Scope

- Moving legal-basis-owned domain or persistence classes.
- Changing assessment status, evidence or approval.
- Renaming tables, endpoints or JSON contracts.
- Frontend changes.
- Public privacy-policy governance.

## Acceptance Criteria

- Processing operation domain, application and persistence form a complete
  hexagonal submodule under `privacy.processing`.
- Processing domain contains no JPA, Spring MVC or legalbasis imports.
- Processing services access their own persistence only through processing
  ports.
- Processing core does not import assessment persistence or readiness services.
- A legal-basis consumer can obtain minimum operation context through a direct
  application-service capability without receiving a persistence port.
- No service dependency cycle is introduced.
- Stable operation codes and audit classifications are unchanged.
- Existing tables, rows, IDs, statuses and catalog behavior are preserved.
- Existing REST paths and JSON properties remain compatible.
- Marketing remains inactive.
- Focused tests, architecture checks and the full Maven suite pass.

## Verification Commands

At minimum:

```text
./mvnw test
rg -n "privacy\.legalbasis\..*(persistence|adapter)" src/main/java/com/househost/privacy/processing
git diff --check
```

The forbidden-import search must return no production matches.

## Required Report

Create after implementation:

```text
SDD/ImplementationReport/2026-07-27-007b-extract-privacy-processing-submodule.md
```
