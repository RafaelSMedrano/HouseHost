# Task 028f DONE — Move Assessment To Checkout

## Status

Completed and verified on 2026-08-12 after explicit user authorization.

## Implementation Area

Frontend (`f`).

## Objective

Remove history and assessment inputs from guest registration and make guest
assessment available only while completing checkout.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/guestRegistrationPolishSpec.md`

## Required Plans

- `SDD/plans/frontendSpecs/guestRegistrationPolishFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- current guest form, checkout form, operational controller/API adapter, CSS
  and related frontend tests.

## Dependencies

- `SDD/tasks/backendSpecs/019b-DONE-apply-guest-history-at-checkout.md`
- `SDD/tasks/frontendSpecs/027f-DONE-rebuild-guest-care-fields.md`

## Scope

- Remove the entire history/assessment section from guest create/edit.
- Remove registration rating state/handlers and all history/assessment guest
  payload members.
- Add `Histórico e avaliação` to checkout only, never check-in.
- Show selected booking/guest context and backend-supported read-only history
  preview without calculating authoritative totals in the browser.
- Add optional rating input using the backend scale and submit it with checkout.
- Preserve checkout form values and disable duplicate submit while saving.
- Announce validation, request and success outcomes accessibly.
- Keep existing guest profile history read-only.
- Update cache-busting and focused tests.

## Out Of Scope

- Backend history calculation or idempotence.
- Public ratings, comments or rating history.
- Checkout permission or navigation changes.
- Guest-profile redesign.

## Expected Files

Expected changes include:

```text
frontend/admin/js/views/guestFormView.js
frontend/admin/js/views/checkOperationFormView.js
frontend/admin/js/api.js
frontend/admin/css/home.css
frontend/admin/tests/guestRegistrationPolish.test.mjs
frontend/admin/tests/checkOperationForm.test.mjs
```

## Acceptance Criteria

- Guest registration/edit contains no stay count, total spent, last-stay date or
  rating input.
- Guest create/update payload contains none of those properties.
- Checkout displays `Histórico e avaliação`; check-in does not.
- A valid optional rating is included in checkout payload using the exact
  backend property.
- History totals are not accepted from arbitrary browser input or calculated
  as authoritative client state.
- Invalid rating has clear accessible feedback before or after backend
  validation.
- Failed checkout preserves the operator's entries and duplicate submission is
  prevented while the request is active.
- Focused tests, the full frontend suite and `git diff --check` pass.

## Verification Commands

```text
node --test frontend/admin/tests/guestRegistrationPolish.test.mjs
node --test frontend/admin/tests/checkOperationForm.test.mjs
node --test frontend/admin/tests/*.test.mjs
git diff --check
```

## Required Report

```text
SDD/ImplementationReport/2026-08-12-028f-move-assessment-to-checkout.md
```
