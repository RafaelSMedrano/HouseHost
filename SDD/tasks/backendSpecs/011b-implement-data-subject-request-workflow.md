# Task 011b — Implement Data-Subject Request Workflow

## Status

Proposed. Not started and not approved for implementation by creation of this
document alone.

## Implementation Area

Backend (`b`).

## Objective

Implement the `privacy.request` hexagon with secure invitations, official
protocols, lifecycle, identity gates, deadlines, accountable execution, safe
response delivery and minimized audit evidence.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/dataSubjectRequestWorkflowSpec.md`
- `SDD/specs/backendSpecs/privacyHexagonalSubmodulesSpec.md`
- `SDD/specs/backendSpecs/privacyPolicySubmoduleSpec.md`
- `SDD/specs/backendSpecs/dataSubjectRequestSubmoduleSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/dataSubjectRequestBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- implementation reports for tasks `009b` and `010b`.

## Dependencies

- tasks `009b` and `010b` completed;
- current policy and booking-policy contracts verified;
- authenticated administration and audit capabilities available.

## Scope

- Create the complete `privacy.request` domain/application/adapter structure.
- Persist requests, invitation digests, history, analysis, actions and
  communications with concurrency and idempotency controls.
- Implement opaque expiring/revocable invitations and safe token exchange.
- Implement official non-sequential protocols and proportional identity state.
- Implement server-controlled transitions, assignment and deadline calculation.
- Implement protected administrative invitation, query and command endpoints.
- Implement public submission, minimized status and safe-response endpoints.
- Create provider-neutral notification and identity/evidence ports.
- Create context discovery and action-execution ports/adapters without foreign
  repository access.
- Require execution evidence before reporting an action complete.
- Add minimized audit events, rate limits and secure delivery controls.
- Add idempotent schema compatibility and focused tests.

## Out Of Scope

- Frontend pages.
- WhatsApp-provider automation.
- Automatic legal decisions or integration with authorities/courts.
- Policy wording publication.
- Claiming deletion in contexts without a verified executor result.

## Acceptance Criteria

- The capability is a complete hexagonal submodule under `privacy.request`.
- Public invitations are opaque, hashed at rest, expiring and revocable.
- Unknown, invalid and expired public cases do not reveal record existence.
- Retried intake returns one request and one official protocol.
- Domain rules prevent invalid transitions and sensitive disclosure before the
  required identity verification.
- Every status, assignment, analysis, action and response has append-only
  history with actor and time.
- Due dates are calculated by a versioned server rule and overdue cases are
  queryable without silent status mutation.
- An action is complete only with owning-module execution evidence or a recorded
  and communicated exception.
- Administrative endpoints enforce `CEO`, `CTO` or `ADMIN` authorization.
- Notifications and response delivery do not expose sensitive packages through
  ordinary email/WhatsApp content.
- Tokens, identity documents and complete narratives are absent from logs and
  audit metadata.
- Focused tests, the full Maven suite, architecture checks and
  `git diff --check` pass.

## Verification Commands

At minimum:

```text
./mvnw test
rg -n "@Entity|@Table|org\.springframework|jakarta\.persistence" src/main/java/com/househost/privacy/request/domain
rg -n "adapter\.out\.persistence|JpaRepository|JpaEntity" src/main/java/com/househost/privacy/request/domain src/main/java/com/househost/privacy/request/application
git diff --check
```

Both forbidden-import searches must return no matches.

## Required Report

Create after implementation:

```text
SDD/ImplementationReport/YYYY-MM-DD-011b-implement-data-subject-request-workflow.md
```
