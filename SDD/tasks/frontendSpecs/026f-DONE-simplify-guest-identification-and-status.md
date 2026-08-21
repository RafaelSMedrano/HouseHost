# Task 026f DONE — Simplify Guest Identification And Status

## Status

Completed and verified on 2026-08-12 after explicit user authorization.

## Implementation Area

Frontend (`f`).

## Objective

Remove manual status selection from guest registration and align every guest
status presentation with the authoritative English backend vocabulary.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/guestRegistrationPolishSpec.md`

## Required Plans

- `SDD/plans/frontendSpecs/guestRegistrationPolishFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- current guest form, list, profile, controller, API and frontend tests.

## Dependencies

- `SDD/tasks/backendSpecs/017b-DONE-refine-guest-domain-and-contract.md`
- `SDD/tasks/backendSpecs/021b-DONE-centralize-guest-status-participant-notifiers.md`

## Scope

- Remove the status select from guest create/edit identification.
- Remove status from guest payload collection and edit-field loading.
- Treat the new guest state as non-editable `INACTIVE` explanatory information
  when a preview needs a status.
- Update list/profile/form status labels, badges, normalization and filters for
  the four new enum values.
- Stop browser-side booking scans from overriding an authoritative guest status.
- Keep compatibility aliases only at a controlled response boundary if rollout
  ordering requires them.
- Update cache-busting versions and add focused frontend tests.

## Out Of Scope

- Preferences/restrictions form redesign.
- History/assessment relocation.
- Backend implementation.
- Guest profile information-architecture redesign.

## Expected Files

Expected changes include:

```text
frontend/admin/js/views/guestFormView.js
frontend/admin/js/views/guestsView.js
frontend/admin/js/views/guestProfileView.js
frontend/admin/js/controllers/guestController.js
frontend/admin/tests/guestRegistrationPolish.test.mjs
```

Importers of changed modules may also require cache-busting updates.

## Acceptance Criteria

- No status control appears in create or edit guest forms.
- A guest create/update payload has no `status` member.
- UI labels map exactly to Com reserva não confirmada, Com reserva confirmada,
  Em estadia and Inativo.
- Filters and badges distinguish both booking states.
- Backend-returned status is not replaced by contradictory browser inference.
- Legacy aliases, if temporarily supported, are never submitted.
- Existing save, cancel, edit and navigation behavior remains operational.
- Focused tests, the full frontend suite and `git diff --check` pass.

## Verification Commands

```text
node --test frontend/admin/tests/guestRegistrationPolish.test.mjs
node --test frontend/admin/tests/*.test.mjs
git diff --check
```

## Required Report

```text
SDD/ImplementationReport/2026-08-12-026f-simplify-guest-identification-and-status.md
```
