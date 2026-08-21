# Task 018b DONE — Synchronize Guest Lifecycle Status

## Status

Completed and verified on 2026-08-12.

## Implementation Area

Backend (`b`).

## Objective

Apply direct guest-status transitions from reservation, check-in and checkout
operations without manual guest-form status writes or a lifecycle component.

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
- current guest, booking, check-in and checkout domain/application ports,
  services, persistence queries and transition tests.

## Dependencies

- `SDD/tasks/backendSpecs/017b-DONE-refine-guest-domain-and-contract.md`

## Scope

- Rename the guest domain and service status operation to `setStatus`.
- Make reservation creation assign `WITH_UNCONFIRMED_BOOKING` or
  `WITH_CONFIRMED_BOOKING` from the initial booking status.
- Make `BookingService.setStatus` assign `WITH_CONFIRMED_BOOKING` when a
  booking becomes `CONFIRMED`.
- Make completed check-in assign `IN_STAY` through `GuestService.setStatus`.
- Make completed checkout assign `INACTIVE` through `GuestService.setStatus`.
- Keep the transitions in the existing application services, without creating
  lifecycle services, lifecycle ports or reservation scans.
- Add focused direct-transition tests.

## Out Of Scope

- Guest registration DTO and persistence migration completed by prior tasks.
- Checkout history counters, amount or rating.
- Booking-status enum changes.
- Frontend labels or filters.

## Expected Files

Expected changes may include:

```text
src/main/java/com/househost/guest/domain/model/Guest.java
src/main/java/com/househost/guest/application/service/GuestService.java
src/main/java/com/househost/booking/booking/application/service/BookingService.java
src/main/java/com/househost/booking/checking/application/service/CheckInPartyResolverService.java
src/main/java/com/househost/booking/checkout/application/service/CheckOutPartyResolverService.java
src/main/java/com/househost/publicapi/application/service/PublicBookingService.java
src/test/java/com/househost/guest/...
src/test/java/com/househost/booking/...
```

## Acceptance Criteria

- Creating an unconfirmed reservation assigns `WITH_UNCONFIRMED_BOOKING`.
- Creating a confirmed reservation assigns `WITH_CONFIRMED_BOOKING`.
- Changing a booking to `CONFIRMED` through `BookingService.setStatus` assigns
  `WITH_CONFIRMED_BOOKING`.
- Completed check-in assigns `IN_STAY` through `GuestService.setStatus`.
- Completed checkout assigns `INACTIVE` through `GuestService.setStatus`.
- No lifecycle service, lifecycle port or competing-reservation scan is added.
- A failure rolls back the operational change and its status consequence
  together where the existing transaction boundary requires atomicity.
- No domain class depends on booking persistence, Spring Data or JPA.
- Focused tests, the full Maven suite and `git diff --check` pass.

## Verification Commands

```text
./mvnw test
git diff --check
```

## Required Report

```text
SDD/ImplementationReport/YYYY-MM-DD-018b-synchronize-guest-lifecycle-status.md
```
