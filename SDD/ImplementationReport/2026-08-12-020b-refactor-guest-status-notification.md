# Implementation Report — Task 020b

## Task And Implementation File

- Task: `020b-DONE-refactor-guest-status-notification.md`
- Implementation control: `SDD/implementation/implementation-order.md`
- Execution date: 2026-08-12

## Specs, Prerequisites And Plans Read

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/guestRegistrationPolishSpec.md`
- `SDD/plans/backendSpecs/guestRegistrationPolishBackendPlan.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/tasks/backendSpecs/018b-DONE-synchronize-guest-lifecycle-status.md`

## Files Created

- `SDD/tasks/backendSpecs/020b-DONE-refactor-guest-status-notification.md`
- `SDD/ImplementationReport/2026-08-12-020b-refactor-guest-status-notification.md`
- `src/main/java/com/househost/booking/booking/application/service/BookingGuestNotifier.java`
- `src/main/java/com/househost/booking/booking/application/service/BookingGuestResolver.java`
- `src/main/java/com/househost/booking/booking/application/service/BookingRoomResolver.java`
- `src/main/java/com/househost/booking/checking/application/service/CheckInPartyNotifier.java`
- `src/main/java/com/househost/booking/checking/application/service/CheckInPartyResolver.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutPartyNotifier.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutPartyResolver.java`
- `src/main/java/com/househost/publicapi/application/service/PublicBookingGuestNotifier.java`
- `src/main/java/com/househost/publicapi/application/service/PublicBookingGuestResolver.java`
- `src/main/java/com/househost/publicapi/application/service/PublicBookingRoomResolver.java`
- `src/test/java/com/househost/booking/booking/application/service/BookingGuestNotifierTest.java`
- `src/test/java/com/househost/booking/booking/application/service/BookingGuestResolverTest.java`
- `src/test/java/com/househost/booking/booking/architecture/CrossModuleServiceCommunicationTest.java`

## Files Changed

- `SDD/specs/guestRegistrationPolishSpec.md`
- `SDD/plans/backendSpecs/guestRegistrationPolishBackendPlan.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `src/main/java/com/househost/booking/booking/application/service/BookingService.java`
- `src/main/java/com/househost/booking/booking/application/service/BookingFormService.java`
- `src/main/java/com/househost/booking/checking/application/service/CheckInService.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutService.java`
- `src/main/java/com/househost/publicapi/application/service/PublicBookingService.java`
- `src/test/java/com/househost/booking/booking/application/service/BookingServiceDeletionTest.java`
- `src/test/java/com/househost/booking/booking/application/service/BookingServiceGuestStatusTest.java`
- `src/test/java/com/househost/booking/checking/application/service/CheckInGuestStatusTest.java`
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutGuestStatusTest.java`
- `src/test/java/com/househost/publicapi/application/service/PublicBookingServiceTest.java`

## Files Removed

- `src/main/java/com/househost/booking/checking/application/service/CheckInPartyResolverService.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutPartyResolverService.java`

## Flows Implemented

- Booking creation, update, status change and deletion notify guest status
  recomputation through `BookingGuestNotifier` and `BookingGuestResolver`.
- Booking transfer recomputes the previous and current guest.
- Effective status derives from all reservations using `IN_STAY`, `CONFIRMED`,
  `UNCONFIRMED`, then `INACTIVE` priority.
- Booking principal and form services resolve Guest and Room without injecting
  their external concrete services.
- Booking-backed check-in and checkout update Booking through local party
  Notifiers and Resolvers; Booking then recomputes Guest.
- Bookingless completed check-in assigns `IN_STAY` through its local party
  Notifier and Resolver.
- Public booking creates and synchronizes Guest through local Resolver and
  Notifier components.

## Technical And MVP Decisions

- A generic lifecycle service was not reintroduced. Reservation truth is
  centralized in `BookingGuestResolver`.
- Notifiers decide when an effect occurs; Resolvers own cross-module calls.
- Check-in creation and update gained transactional boundaries so operational
  persistence and downstream status effects roll back together.
- The existing `BookingPersistencePort.findByGuestId` supports recomputation;
  no database migration or new persistence contract was required.

## Difficulties, Problems And Resolutions

- Existing tests instantiated principal services with external services
  directly. Fixtures were migrated to the local Resolver/Notifier contracts.
- Public booking previously persisted Guest status directly. A local guest
  Resolver and Notifier now route status derivation through Booking.
- Existing check-in and checkout party classes combined decision and external
  calls and ended in `ResolverService`. They were split into explicit
  `Notifier` and `Resolver` responsibilities.

## Tests And Verification

- Focused Resolver, Notifier, booking, check-in, checkout, public-booking and
  architecture tests: passed.
- `./mvnw test`: passed, 199 tests with zero failures and zero errors.
- `git diff --check`: passed.
- Search confirmed no direct `GuestService`, `RoomService` or `BookingService`
  dependency in the principal services covered by task `020b`.
- Cross-module mutation calls to `GuestService.setStatus` remain only inside
  Resolvers.

## Prerequisite Review

- Guest status remains server-owned and uses only the four specified English
  values.
- Reservation-wide priority now covers the guarantees previously provided by
  lifecycle orchestration.
- Principal services in scope comply with the new Notifier/Resolver architecture.
- Financial, audit and supplier decoupling rules remain unchanged.
- Domain models gained no framework, persistence or cross-module dependency.
- No frontend, database, checkout-history or rating scope was introduced.
- All acceptance criteria and revised prerequisite documents are conformant.
