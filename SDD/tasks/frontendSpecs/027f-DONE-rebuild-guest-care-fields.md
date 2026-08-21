# Task 027f DONE — Rebuild Guest Care Fields

## Status

Completed and verified on 2026-08-12 after explicit user authorization.

## Implementation Area

Frontend (`f`).

## Objective

Replace the structured guest preferences UI with exactly two accessible
free-text writing fields.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/guestRegistrationPolishSpec.md`

## Required Plans

- `SDD/plans/frontendSpecs/guestRegistrationPolishFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- current guest form, guest profile compatibility, API contract, CSS and tests.

## Dependencies

- `SDD/tasks/backendSpecs/016b-DONE-migrate-guest-status-and-care-fields.md`
- `SDD/tasks/backendSpecs/017b-DONE-refine-guest-domain-and-contract.md`
- `SDD/tasks/frontendSpecs/026f-DONE-simplify-guest-identification-and-status.md`

## Scope

- Name the block `Preferências e restrições`.
- Render only two full-width textareas named `Preferências e restrições` and
  `Necessidades de acessibilidade`.
- Bind them to `preferencesAndRestrictions` and `accessibilityNeeds` strings.
- Remove suggestions, chips, add/remove state and handlers.
- Remove pet, favorite-room and boolean accessibility controls/payload members.
- Preserve line breaks and enforce lengths consistent with backend validation.
- Load existing values returned in the two new text fields during edit mode and
  retain them after failures.
- Keep visible labels, focus states and accessible validation feedback.
- Update related read-only profile rendering only as required by the new API.

## Out Of Scope

- Status presentation completed by task `026f`.
- Registration history removal or checkout assessment.
- Internal-notes design.
- Backend or database changes.

## Expected Files

Expected changes include:

```text
frontend/admin/js/views/guestFormView.js
frontend/admin/js/views/guestProfileView.js
frontend/admin/css/home.css
frontend/admin/tests/guestRegistrationPolish.test.mjs
```

## Acceptance Criteria

- The block contains exactly two writable fields and no option-based control.
- Suggestions, chips, add button, pet fields, favorite room and accessibility
  switch are absent.
- Create/update payload uses two strings with the exact backend property names.
- Edit mode faithfully loads both fields, including line breaks.
- A failed save preserves both values and exposes accessible feedback.
- No obsolete structured care member is sent.
- Desktop and responsive layouts keep both textareas full-width and usable.
- Focused tests, the full frontend suite and `git diff --check` pass.

## Verification Commands

```text
node --test frontend/admin/tests/guestRegistrationPolish.test.mjs
node --test frontend/admin/tests/*.test.mjs
git diff --check
```

## Required Report

```text
SDD/ImplementationReport/2026-08-12-027f-rebuild-guest-care-fields.md
```
