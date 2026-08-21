# Task 030f DONE — Simplify And Reorder Guest Origin

## Status

Completed and verified on 2026-08-12 after explicit user authorization.

## Implementation Area

Frontend (`f`).

## Objective

Keep only the guest origin-channel dropdown and place its section before guest
preferences and restrictions.

## Required Specs And Plans

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/guestRegistrationPolishSpec.md`
- `SDD/plans/frontendSpecs/guestRegistrationPolishFrontendPlan.md`

## Dependencies

- `SDD/tasks/backendSpecs/022b-DONE-remove-guest-referrer-name.md`
- `SDD/tasks/frontendSpecs/029f-DONE-align-internal-notes-design-and-verify-flow.md`

## Scope

- Remove `Indicado por` from guest create/edit and profile presentation.
- Remove `referredBy` from loading and payload construction.
- Keep one full-width `Canal de origem` dropdown.
- Place `Origem & Canal` before `Preferências e restrições`.
- Update cache versions and integrated frontend tests.

## Acceptance Criteria

- The origin block contains exactly one dropdown and no writing field.
- The origin block precedes the care block.
- Guest payloads and profile presentation contain no referrer name.
- Reservation referral-origin behavior remains unchanged.
- Focused and complete frontend tests and `git diff --check` pass.

## Required Report

`SDD/ImplementationReport/2026-08-12-030f-simplify-and-reorder-guest-origin.md`
