# Task 021b DONE — Centralize Guest Status Participant Notifiers

## Status

Completed and verified on 2026-08-12.

## Implementation Area

Backend (`b`).

## Objective

Align the guest-status lifecycle delivered by task `020b` with the module
communication architecture: direct service calls for simple queries and one
module-local `ParticipantNotifier` coordinating specialized Resolvers for
cross-module mutations.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/guestRegistrationPolishSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/guestRegistrationPolishBackendPlan.md`

## Dependencies

- `SDD/tasks/backendSpecs/020b-DONE-refactor-guest-status-notification.md`

## Scope

- Let principal services call external module services directly for simple,
  side-effect-free queries.
- Replace participant-specific Notifiers with one `ParticipantNotifier` in
  Booking, Check-in, Checkout and Public Booking.
- Keep each cross-module mutation in a specialized Resolver invoked only by
  the source module's `ParticipantNotifier`.
- Preserve reservation-wide guest-status derivation, transfer handling and
  bookingless check-in behavior.
- Update focused lifecycle and architecture tests.

## Acceptance Criteria

- Each principal service in scope injects exactly one module-local
  `ParticipantNotifier` for cross-module effects and no Resolver.
- Query-only forwarding Resolvers are removed.
- Booking create, update, status change and delete recompute the affected guest
  status through `BookingParticipantNotifier`.
- Completed check-in and checkout route their Booking, Guest and Room effects
  through their respective `ParticipantNotifier` and specialized Resolvers.
- Public booking routes guest creation and status synchronization through
  `PublicBookingParticipantNotifier` while querying Room directly.
- Focused tests, the full Maven suite and `git diff --check` pass.

## Verification Commands

```text
./mvnw test
git diff --check
```

## Required Report

```text
SDD/ImplementationReport/2026-08-12-021b-centralize-guest-status-participant-notifiers.md
```
