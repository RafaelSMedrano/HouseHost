# Task 038f DONE — Integrate Reservation FTP Command And State

## Status

Completed and verified on 2026-08-20 after explicit execution approval and
completion of task `037f`.

## Implementation Area

Frontend (`f`).

## Objective

Connect reservation FTP allocation to the authenticated backend contract,
idempotent submission and navigation-preserved form state.

## Required Implementation Files

- `SDD/specs/sddSpec.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`
- `SDD/specs/frontendSpecs/financialTransactionPlanAdministrativeExperienceSpec.md`
- `SDD/plans/frontendSpecs/financialTransactionPlanAdministrativeExperienceFrontendPlan.md`
- `SDD/tasks/backendSpecs/034b-DONE-build-ftp-use-cases-and-reservation-creation.md`
- `SDD/tasks/frontendSpecs/037f-DONE-build-reservation-ftp-allocation-interface.md`

## Scope

- Replace the legacy payment payload with the nested minimized FTP allocation.
- Add API helpers for booking-owned FTP summary and command reconciliation.
- Generate, preserve and rotate reservation idempotence keys correctly.
- Preserve every FTP control in in-memory navigation form state.
- Reconcile backend totals and field/domain validation with inline sections.
- Reload authoritative state after uncertain response instead of blind retry.
- Add read-only minimized FTP summary to the reservation context.

## Acceptance Criteria

- Payload contains operator choices and excludes protected derived identity.
- One form submission creates at most one booking and FTP.
- Returning from related navigation restores all unsaved financial controls.
- Timeout recovery distinguishes committed and uncommitted outcomes safely.
- Backend discrepancies appear inline and replace local preview authority.
- Operational and management summary visibility follows semantic permissions.
- Focused API/state tests, full Node suite, syntax checks and
  `git diff --check` pass.
