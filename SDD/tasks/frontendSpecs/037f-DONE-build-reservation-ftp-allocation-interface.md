# Task 037f DONE — Build Reservation FTP Allocation Interface

## Status

Completed and verified on 2026-08-20 after explicit execution approval. The
completed backend prerequisites through task `036b` were reviewed before
implementation.

## Implementation Area

Frontend (`f`).

## Objective

Replace the ambiguous reservation payment controls with accessible purpose-based
FTP allocation and cent-precise previews.

## Required Implementation Files

- `SDD/specs/sddSpec.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`
- `SDD/specs/frontendSpecs/financialTransactionPlanAdministrativeExperienceSpec.md`
- `SDD/plans/backendSpecs/financialTransactionPlanBackendPlan.md`
- `SDD/plans/frontendSpecs/financialTransactionPlanAdministrativeExperienceFrontendPlan.md`
- `SDD/tasks/backendSpecs/034b-DONE-build-ftp-use-cases-and-reservation-creation.md`

## Scope

- Build disabled-by-default signal, check-in and checkout allocation groups.
- Add total, allocated and remaining summary with explicit text states.
- Add signal received state, method, simple/installment choice and 2–12 quantity.
- Build cent-based allocation and residual-last-installment preview helpers.
- Keep future check-in/checkout structure unavailable during reservation creation.
- Add responsive, keyboard, label, live-region and inline-error behavior.
- Add focused pure helper and source-contract tests.

## Acceptance Criteria

- No purpose or automatic checkout remainder is enabled by default.
- Decimal-cent calculations are exact and never use floating equality.
- Conditional signal fields appear only for the applicable structure.
- Check-in/checkout accept only initial allocation values.
- Summary and errors remain understandable without color and on narrow screens.
- Existing guest, room, notes, rating-history and navigation behavior remains valid.
- Focused frontend tests, full Node suite, syntax checks and
  `git diff --check` pass.
