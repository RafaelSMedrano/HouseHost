# Task 035b DONE — Implement Atomic FTP Payment Replacement

## Status

Completed on 2026-08-19 after explicit user authorization, focused verification
and the complete Maven suite.

## Implementation Area

Backend (`b`).

## Objective

Implement idempotent destructive replacement of one eligible provisional FTP
component with its definitive simple or installment transaction.

## Required Implementation Files

- `SDD/specs/sddSpec.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/plans/backendSpecs/financialTransactionPlanBackendPlan.md`
- `SDD/tasks/backendSpecs/030b-DONE-notify-financial-source-deletion.md`
- `SDD/tasks/backendSpecs/032b-DONE-correct-cashier-schedule-semantics.md`
- `SDD/tasks/backendSpecs/034b-DONE-build-ftp-use-cases-and-reservation-creation.md`

## Scope

- Create the dedicated replacement service and authenticated command.
- Lock and validate FTP, purpose, old member, status, deadline and request key.
- Allow only unrealized `WAITING` or `OVERDUE` provisional components.
- Notify and remove every old participant effect, then physically delete it.
- Create the definitive simple transaction or installment block with new ID.
- Rebuild PLAN/INSTALLMENT sources, participant effects, order, totals and status.
- Roll back every effect after failure at any boundary.
- Audit only definitive transaction creation after commit, without old ID or
  replacement link.
- Return conflicts and idempotent replay outcomes deterministically.

## Acceptance Criteria

- Successful replacement leaves no old row, movement, source association or
  active participant effect.
- The definitive transaction has a new ID and no domain/audit replacement link.
- Realized Cashier balances are identical before and after scheduling replacement.
- Settled, canceled, partially realized, stale and deadline-invalid requests
  are rejected before mutation.
- Every injected participant, persistence and audit-boundary failure proves
  complete rollback or post-commit behavior as applicable.
- Concurrent or repeated requests create exactly one definitive transaction.
- Exactly one definitive creation audit exists; no provisional or deletion
  audit is produced.
- Focused replacement/concurrency tests, full Maven suite and
  `git diff --check` pass.
