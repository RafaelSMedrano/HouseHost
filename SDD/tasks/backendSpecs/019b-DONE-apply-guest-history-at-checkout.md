# Task 019b DONE — Apply Guest History At Checkout

## Status

Completed and verified on 2026-08-12.

## Implementation Area

Backend (`b`).

## Objective

Apply guest stay history and optional assessment atomically and exactly once
when checkout is completed.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/guestRegistrationPolishSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/guestRegistrationPolishBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- current checkout DTO/domain/service/persistence implementation;
- current guest operational-history mutation and authoritative booking/finance
  amount sources;
- checkout and transaction integration tests.

## Dependencies

- `SDD/tasks/backendSpecs/018b-DONE-synchronize-guest-lifecycle-status.md`
- `SDD/tasks/backendSpecs/021b-DONE-centralize-guest-status-participant-notifiers.md`

## Scope

- Extend checkout input with an optional bounded guest rating.
- Persist explicit evidence that a completed checkout's guest history was
  applied.
- On first completion, increment stay count, set last-stay date, incorporate
  the authoritative finalized amount and store the latest optional rating.
- Keep history unchanged for pending/cancelled checkout.
- Make create/update/retry of completed checkout idempotent.
- Apply checkout, party/status effects, guest history, evidence and audit in
  one transaction.
- Reject invalid rating without silently coercing it.
- Document in the implementation report which existing backend amount source
  is authoritative and why.
- Add migration for the evidence field if persistence requires it.
- Add service and database-backed atomicity/idempotence tests.

## Out Of Scope

- Client-supplied stay count, last-stay date or total-spent values.
- Multi-entry rating history, rating comments or public reviews.
- Redesigning financial calculations.
- Frontend checkout form implementation.

## Acceptance Criteria

- First transition to completed checkout updates guest count and last-stay date
  exactly once.
- Total spent changes only by an authoritative backend-calculated amount.
- A valid optional rating becomes the guest's latest assessment; absence
  preserves the prior rating.
- Pending/cancelled checkout changes none of those fields.
- Re-saving or updating a completed checkout does not double count.
- Persistent evidence survives restart and concurrent/retried calls.
- Failure while saving guest history rolls back checkout completion, evidence
  and success audit.
- Rating outside the accepted scale returns a controlled validation failure.
- Sensitive values are absent from audit metadata.
- Focused tests, database-backed integration tests, the full Maven suite and
  `git diff --check` pass.

## Verification Commands

```text
./mvnw test
git diff --check
```

## Required Report

```text
SDD/ImplementationReport/2026-08-12-019b-apply-guest-history-at-checkout.md
```
