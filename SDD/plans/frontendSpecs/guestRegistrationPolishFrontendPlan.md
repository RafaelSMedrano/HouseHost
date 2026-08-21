# Guest Registration Polish Frontend Plan

## Governing Specs

- `SDD/specs/guestRegistrationPolishSpec.md`
- prerequisite: `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`

## Objective

Simplify the administrative guest form to the information appropriate at
registration, align its internal-notes design and collect history assessment
only during checkout using the revised backend contract.

This plan does not authorize implementation. Tasks `026f` through `029f`
depend on the corresponding backend contracts and require explicit approval.

## Current Frontend Context

Relevant modules are:

```text
guestFormView.js (frontend/admin view; guest form)
guestsView.js (frontend/admin view; guest list)
guestProfileView.js (frontend/admin view; guest profile)
checkOperationFormView.js (frontend/admin view; check-in/out forms)
guestController.js (frontend/admin controller; guest flows)
operationsController.js (frontend/admin controller; operational flows)
api.js (frontend/admin API adapter; module)
home.css (frontend/admin stylesheet; administrative UI)
```

The application remains framework-free and follows the existing controller,
view, semantic-callback, API adapter and cache-busting conventions.

## Guest Identification And Status Presentation

Remove the status select from `guestFormView.js` in both create and edit modes.
Do not include `status` in the guest payload. A new unsaved preview may label
the lifecycle as `Inativo` only as explanatory server-default information; it
must not act as a control.

Update all guest status label, normalization, badge and filter maps affected by
the backend enum so they recognize only:

```text
WITH_UNCONFIRMED_BOOKING -> Com reserva não confirmada
WITH_CONFIRMED_BOOKING   -> Com reserva confirmada
IN_STAY                  -> Em estadia
INACTIVE                 -> Inativo
```

Do not re-derive a conflicting guest status in the browser when the backend
already returns the authoritative status. Compatibility aliases may exist only
at a controlled response boundary during deployment and must not appear in new
payloads.

## Preferences And Accessibility Block

Rename the block to `Preferências e restrições`. Render exactly two full-width
multiline writing fields:

- `Preferências e restrições` bound to `preferencesAndRestrictions`;
- `Necessidades de acessibilidade` bound to `accessibilityNeeds`.

Remove quick suggestions, chips, add/remove behavior, pet switch/type,
accessibility switch and favorite-room select. Load and submit plain strings,
preserve line breaks, apply reasonable `maxlength` limits matching backend
validation, and use visible labels with accessible focus/error behavior.

## Origin Channel Block

Place `Origem & Canal` before `Preferências e restrições`. Render only the
`originChannel` dropdown in that section and make it use the full available
width. Remove `referredBy` from create/edit loading, guest payloads and profile
presentation. The reservation-origin option for referrals remains unchanged.

## Remove Registration History Inputs

Remove the entire `Histórico e avaliação` block, its rating state, rating event
handlers and `stayCount`, `totalSpent`, `lastStayDate`, `rating` payload members
from guest creation/editing. Existing profile display can remain read-only and
must consume backend-maintained values.

## Checkout Stay-History Preview

Show the selected reservation/guest context and any read-only stay-history
preview that the backend contract makes available. The preview is not rendered
in check-in or ordinary guest registration.

This plan defines no generic guest or checkout rating. The former single-rating
control, state and payload are obsolete; structured booking-service evaluations
belong to the independent ratings spec and plan. Preserve values after a failed
request, prevent duplicate submission while saving and announce load,
validation and save outcomes accessibly.

## Internal Notes Design

Keep the `Observações internas` header and team-only description. Remove the
visible `Anotações` label while retaining an accessible name via `aria-label`
or visually hidden text.

Add a dedicated notes-textarea wrapper rather than depending on incidental
label spacing. Its horizontal inset matches the section header and other
section content, the textarea uses the full available width, and border,
radius, background, focus ring and vertical padding remain visually balanced
at desktop and existing responsive breakpoints.

## API, Cache Busting And Verification

Update affected API contract helpers and every changed static import version so
browsers do not mix old and new payload/rendering code.

Add DOM/module-level tests for:

- absence of the registration status control and status payload;
- exact presence of only the two care textareas in that block;
- removal of suggestions, chips and structured preference controls;
- absence of history/rating from guest registration and payload;
- checkout-only stay-history preview and absence of a generic rating payload;
- new guest status labels and filters;
- internal-notes accessible name and symmetric wrapper classes;
- origin block ordering, its single dropdown and absence of `referredBy`;
- preservation of form values and disabled-submit behavior on failure;
- full frontend test suite and `git diff --check`.

## Out Of Scope

- changing guest profile information architecture;
- adding public guest preference collection;
- browser-side calculation of authoritative stay count or total spent;
- changing checkout permissions or navigation history;
- introducing a frontend framework;
- unrelated administrative CSS cleanup.
