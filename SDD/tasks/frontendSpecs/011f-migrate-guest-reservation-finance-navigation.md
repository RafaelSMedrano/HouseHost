# Task 011f — Migrate Guest Reservation And Finance Navigation

## Status

Superseded by task `017f`. Not authorized for implementation; retained as the
former route-registry task for historical traceability.

## Implementation Area

Frontend (`f`).

## Objective

Migrate the highest-risk related-record flows so guest, reservation and
financial screens return to their actual predecessors.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`

## Required Plans

- `SDD/plans/frontendSpecs/administrativeNavigationHistoryFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `frontend/admin/js/controllers/guestController.js`
- `frontend/admin/js/controllers/reservationController.js`
- `frontend/admin/js/controllers/financeController.js`
- `frontend/admin/js/views/guestProfileView.js`
- `frontend/admin/js/views/reservationProfileView.js`
- `frontend/admin/js/views/financialTransactionProfileView.js`

## Dependencies

- Task `010f` completed and reviewed.

## Scope

Migrate these already-separated controllers and their views to the central
navigation API:

```text
Reservas -> reserva -> hóspede
Caixa -> transação -> reserva
Caixa -> transação -> reserva -> hóspede
Hóspedes -> hóspede -> nova reserva
Perfil -> edição -> perfil
```

Update controller callbacks so views request a generic back action. Preserve
the relevant identifiers on every route. Use `replace` after successful edits
when returning to the resulting profile would otherwise duplicate the form.

The controllers must not pass an origin-specific list callback into a related
profile. The navigation stack, not the guest or finance controller, determines
the predecessor.

## Acceptance Criteria

- Financial transaction -> guest returns to the financial transaction.
- Financial transaction -> reservation -> guest returns one level at a time.
- Reservation -> guest returns to the reservation.
- Guest opened from the sidebar returns to the guests list.
- Reservation opened from the sidebar returns to the reservations list.
- Canceling guest, reservation or financial-related forms returns to the
  initiating page.
- Saving an edit does not create duplicate profile/form entries.
- Existing API loading, error and permission states remain intact.

## Expected Files

```text
frontend/admin/js/controllers/guestController.js
frontend/admin/js/controllers/reservationController.js
frontend/admin/js/controllers/financeController.js
frontend/admin/js/views/guestProfileView.js
frontend/admin/js/views/reservationProfileView.js
frontend/admin/js/views/financialTransactionProfileView.js
frontend/admin/tests/administrativeNavigationFlows.test.mjs
```

## Required Report

Create `SDD/ImplementationReport/YYYY-MM-DD-011f-migrate-guest-reservation-finance-navigation.md`
after implementation and verification.
