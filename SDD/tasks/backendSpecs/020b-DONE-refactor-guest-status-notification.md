# Task 020b DONE — Refactor Guest Status Notification

## Status

Completed and verified on 2026-08-12.

## Implementation Area

Backend (`b`).

## Objective

Replace direct cross-module guest-status writes with module-local Notifier and
Resolver flows while restoring reservation-wide status derivation.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/guestRegistrationPolishSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/guestRegistrationPolishBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- current booking, public booking, check-in, checkout and guest application
  services, ports and transition tests.

## Dependencies

- `SDD/tasks/backendSpecs/018b-DONE-synchronize-guest-lifecycle-status.md`

## Scope

- Add `BookingGuestNotifier` and `BookingGuestResolver`.
- Derive guest status from all reservations with priority `IN_STAY`,
  `CONFIRMED`, `UNCONFIRMED`, then `INACTIVE`.
- Recompute after booking create, update, status change, transfer and delete.
- Remove direct `GuestService` and `RoomService` dependencies from Booking's
  principal and form services by using local Resolvers.
- Migrate check-in and checkout cross-module effects to local Notifier and
  Resolver flows.
- Let booking-backed check-in and checkout obtain the guest consequence through
  `BookingService.setStatus` without a duplicate guest write.
- Preserve bookingless completed check-in by using a check-in guest Notifier
  and Resolver.
- Migrate public booking guest creation and status synchronization to local
  Notifier and Resolver components.
- Keep effects inside the existing or newly required transaction boundaries.
- Add focused lifecycle, routing, rollback and architecture tests.

## Out Of Scope

- Guest checkout history, totals and rating from task `019b`.
- Frontend changes.
- Booking-status vocabulary changes.
- Database schema changes.
- Asynchronous event delivery.

## Acceptance Criteria

- Principal services in scope do not inject concrete services from other
  modules.
- Cross-module mutations in scope follow `Notifier → Resolver`.
- Unconfirmed, confirmed and in-stay priority is derived from all reservations.
- Cancellation, finalization and deletion preserve another active reservation.
- Transferring a booking recomputes both guests.
- Completed booking-backed check-in and checkout update the guest through the
  Booking notification flow.
- Completed bookingless check-in assigns `IN_STAY` through its local flow.
- Public booking produces `WITH_UNCONFIRMED_BOOKING` through Notifier/Resolver.
- Failures roll back the operational mutation and guest status consequence.
- Focused tests, the full Maven suite and `git diff --check` pass.

## Verification Commands

```text
./mvnw test
git diff --check
```

## Required Report

```text
SDD/ImplementationReport/YYYY-MM-DD-020b-refactor-guest-status-notification.md
```
