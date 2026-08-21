# Task 039f DONE — Materialize FTP Payment At Check-In

## Status

Completed and verified on 2026-08-20 after explicit execution approval and
completion of backend task `036b` and frontend task `038f`.

## Implementation Area

Frontend (`f`).

## Objective

Load and atomically materialize the scheduled check-in FTP payment through an
accessible simple-or-installment workflow.

## Required Implementation Files

- `SDD/specs/sddSpec.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/specs/frontendSpecs/financialTransactionPlanAdministrativeExperienceSpec.md`
- `SDD/plans/frontendSpecs/financialTransactionPlanAdministrativeExperienceFrontendPlan.md`
- `SDD/tasks/backendSpecs/036b-DONE-integrate-ftp-checkin-checkout-and-security.md`
- `SDD/tasks/frontendSpecs/038f-DONE-integrate-reservation-ftp-command-and-state.md`

## Scope

- Load the scheduled check-in component for the selected booking.
- Present loading, none, eligible, stale, completed and failure states.
- Collect method and simple/installment structure with 2–12 preview.
- Require purpose/amount/structure confirmation before replacement.
- Submit one idempotent check-in materialization command.
- Never optimistically remove the provisional transaction.
- Reload authoritative state after conflict or uncertain network outcome.

## Acceptance Criteria

- No scheduled payment allows check-in to continue without fabricated finance.
- Eligible payment shows minimized authoritative amount, due date and status.
- Installment controls and preview appear only when selected.
- Exactly one API command performs operational save and materialization.
- Success renders only the definitive returned transaction and current FTP.
- Conflict, timeout, authorization and validation outcomes remain recoverable
  and accessible.
- Focused check-in tests, full Node suite, syntax checks and
  `git diff --check` pass.
