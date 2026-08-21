# Task 009b DONE — Implement Privacy Policy Hexagonal Submodule

## Status

Completed on 27 July 2026 after explicit user approval, prerequisite review,
implementation, automated verification and live MySQL validation.

## Implementation Area

Backend (`b`).

## Objective

Create the `privacy.policy` hexagon with immutable versions, safe canonical
content, publication, persistence, protected administration, public current
policy delivery, migration and audit evidence.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/backendSpecs/privacyHexagonalSubmodulesSpec.md`
- `SDD/specs/backendSpecs/privacyPolicySubmoduleSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/privacyPolicySubmoduleBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`

## Dependencies

- tasks `007b` and `008b` completed;
- current public policy version 2 remains the authoritative migration source;
- authenticated user and audit capabilities remain available through ports.

## Scope

- Create policy domain, status and immutable content-hash value object.
- Create safe canonical content validation and deterministic SHA-256 hashing.
- Create administrative and public use cases and application services.
- Create persistence port, JPA entity, repository, mapper and adapter.
- Enforce one current published policy under concurrent publication.
- Create protected draft, update, list, detail and publish endpoints.
- Create `GET /public/privacy-policy` with minimized public response.
- Protect administrative endpoints with `CEO`, `CTO` and `ADMIN`.
- Add policy-owned publisher-resolution and audit ports/adapters.
- Seed exact current policy as published version 2, effective 26 July 2026.
- Do not create version 1 or overwrite a conflicting existing version.
- Add idempotent schema compatibility and focused tests.

## Out Of Scope

- Booking acceptance linkage.
- Public frontend migration.
- Administrative editor UI.
- Policy wording changes.
- Scheduled publishing or external digital signatures.

## Acceptance Criteria

- The policy capability forms a complete hexagonal submodule under
  `privacy.policy`.
- Drafts are editable; published and superseded versions are immutable.
- Canonical content rejects executable or unsupported nodes and unsafe links.
- Hashing is deterministic and stored as `sha256:<hex>`.
- Exactly one policy can be current under sequential and concurrent publication.
- Publishing supersedes the previous current version atomically.
- Publisher, publication time and minimized audit evidence are recorded.
- Administrative endpoints require an authorized administrator.
- The public endpoint exposes only the current published policy contract.
- Version 2 contains the exact trustworthy current policy and is seeded
  idempotently.
- No version 1 or retroactive booking evidence is manufactured.
- Existing privacy and booking data are unchanged.
- Focused tests, full Maven suite and `git diff --check` pass.

## Verification Commands

At minimum:

```text
./mvnw test
rg -n "@Entity|@Table|org\.springframework|jakarta\.persistence" src/main/java/com/househost/privacy/policy/domain
git diff --check
```

The forbidden domain-import search must return no matches.

## Required Report

Create after implementation:

```text
SDD/ImplementationReport/YYYY-MM-DD-009b-implement-privacy-policy-submodule.md
```
