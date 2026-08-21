# Task 018f DONE — Migrate Guest Reservation And Finance Navigation

## Status

Completed on 10 August 2026 after implementation, automated verification and
prerequisite review.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`

## Required Plans

- `SDD/plans/frontendSpecs/administrativeNavigationHistoryFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `frontend/admin/js/controllers/guestController.js`
- `frontend/admin/js/controllers/reservationController.js`
- `frontend/admin/js/controllers/financeController.js`
- related guest, reservation and finance views

## Dependencies

- Task `017f` completed and reviewed.

## Scope Executed

Completed direct navigation-history integration for:

```text
Reservas -> reserva -> hóspede
Caixa -> transação -> hóspede
Caixa -> transação -> reserva -> hóspede
Hóspedes -> hóspede -> nova reserva
Perfil -> edição -> perfil
```

Financial transaction profiles now expose guest participants as semantic
links and submit `guestProfile` entries through the finance controller.
Reservation edit and creation saves use `back` or `replace` according to their
actual predecessor, preventing obsolete forms and duplicate profiles.

## Acceptance Criteria Review

- Transaction -> guest returns to transaction: passed.
- Transaction -> reservation -> guest returns one level at a time: passed.
- Reservation -> guest returns to reservation: passed.
- Sidebar guest/reservation roots return to their lists: passed.
- Form cancellation returns to the initiating entry: passed.
- Save does not create duplicate profile/form history: passed.
- No guest, reservation or finance controller uses the removed facade: passed.

## Files Created

- `frontend/admin/tests/administrativeNavigationFlows.test.mjs`

## Files Changed

- `frontend/admin/js/controllers/UICOntroller.js`
- `frontend/admin/js/controllers/main.js`
- `frontend/admin/js/controllers/reservationController.js`
- `frontend/admin/js/controllers/financeController.js`
- `frontend/admin/js/views/financialTransactionProfileView.js`
- `frontend/admin/tests/administrativeNavigationFlows.test.mjs`

## Tests And Verification

```text
node --test frontend/admin/tests/navigationController.test.mjs frontend/admin/tests/directNavigationInjection.test.mjs frontend/admin/tests/administrativeNavigationFlows.test.mjs
node --test frontend/admin/tests/*.mjs
node --check frontend/admin/js/controllers/reservationController.js
node --check frontend/admin/js/controllers/financeController.js
node --check frontend/admin/js/views/financialTransactionProfileView.js
git diff --check
```

Results:

- Navigation-specific tests: 15 passed, 0 failed.
- Full frontend suite: 36 passed, 0 failed.
- JavaScript syntax checks: passed.
- Git whitespace check: passed.

Unavailable commands: None.

## Technical And MVP Decisions

- Saving an edit opened from an existing profile calls `back`, causing the
  stored profile renderer to reload without creating a duplicate profile.
- Saving an edit opened directly from a list uses `replace` so the form becomes
  the resulting profile and back returns to the list.
- Saving a newly created reservation uses `replace` with its profile when the
  response contains an identifier; otherwise it safely replaces the form with
  the reservations list.
- Guest participants in a financial transaction are rendered as buttons and
  emit `onOpenGuest`; the view does not access navigation directly.

## Prerequisite Review

- Governing SDD, frontend spec and frontend plan were read.
- Required related-record paths preserve every immediate predecessor.
- Views remain independent from navigation internals.
- No facade or complete route registry was reintroduced.
- No backend, permission, storage or browser-history behavior changed.
- All acceptance criteria passed and the task was renamed with `DONE`.

Final status: complete.
