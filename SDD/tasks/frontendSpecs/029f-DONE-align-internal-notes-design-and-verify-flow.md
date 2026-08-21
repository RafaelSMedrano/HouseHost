# Task 029f DONE — Align Internal Notes Design And Verify Guest Flow

## Status

Completed and verified on 2026-08-12 after explicit user authorization.

## Implementation Area

Frontend (`f`).

## Objective

Correct the internal-notes textarea symmetry, remove its redundant title and
verify the complete polished guest-registration and checkout experience.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/guestRegistrationPolishSpec.md`

## Required Plans

- `SDD/plans/frontendSpecs/guestRegistrationPolishFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- completed guest-polish tasks in this sequence;
- current guest/check operation views, controllers, CSS and frontend suites.

## Dependencies

- `SDD/tasks/frontendSpecs/026f-DONE-simplify-guest-identification-and-status.md`
- `SDD/tasks/frontendSpecs/027f-DONE-rebuild-guest-care-fields.md`
- `SDD/tasks/frontendSpecs/028f-DONE-move-assessment-to-checkout.md`

## Scope

- Remove the visible `Anotações` label from internal notes.
- Preserve an accessible textarea name without a redundant visible heading.
- Introduce/adjust a dedicated wrapper with symmetric section insets, full
  width, balanced padding, border, radius, background and focus ring.
- Verify desktop and existing responsive breakpoints.
- Add or complete integrated frontend tests covering the four polished flow
  requirements and cache-busting consistency.
- Perform a manual create/edit/checkout smoke test against the revised backend
  when the local environment is available.
- Correct only defects directly revealed in this scoped flow verification.

## Out Of Scope

- General redesign of form sections or the administrative theme.
- Backend/database changes.
- Unrelated CSS cleanup or visual regression work.
- New navigation or permission behavior.

## Expected Files

Expected changes include:

```text
frontend/admin/js/views/guestFormView.js
frontend/admin/css/home.css
frontend/admin/tests/guestRegistrationPolish.test.mjs
```

Cache-busting importers and checkout flow tests may change when required by
integrated verification.

## Acceptance Criteria

- `Observações internas` remains the sole visible title for the block.
- The textarea has a programmatically determinable accessible name.
- Its left/right inset aligns with the section header/content and the textarea
  occupies the usable width without the prior asymmetric appearance.
- Focus, resize and responsive behavior do not clip or overflow the section.
- Integrated tests prove: no registration status control, exactly two care
  textareas, no registration history block, and checkout-only assessment.
- Create and edit payloads match the backend guest contract; checkout payload
  matches the revised checkout contract.
- All changed import versions are consistent.
- Focused tests, the complete frontend suite and `git diff --check` pass.
- Manual smoke-test results, or the reason it could not run, are recorded in
  the implementation report.

## Verification Commands

```text
node --test frontend/admin/tests/guestRegistrationPolish.test.mjs
node --test frontend/admin/tests/*.test.mjs
git diff --check
```

## Required Report

```text
SDD/ImplementationReport/2026-08-12-029f-align-internal-notes-design-and-verify-flow.md
```
