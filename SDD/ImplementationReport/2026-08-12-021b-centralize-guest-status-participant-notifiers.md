# Implementation Report — Task 021b

## Task And Implementation File

- Task: `021b-DONE-centralize-guest-status-participant-notifiers.md`
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
- `SDD/tasks/backendSpecs/020b-DONE-refactor-guest-status-notification.md`

## Files Created

- `SDD/tasks/backendSpecs/021b-DONE-centralize-guest-status-participant-notifiers.md`
- `SDD/ImplementationReport/2026-08-12-021b-centralize-guest-status-participant-notifiers.md`
- `src/main/java/com/househost/booking/booking/application/service/BookingParticipantNotifier.java`
- `src/main/java/com/househost/booking/checking/application/service/CheckInParticipantNotifier.java`
- `src/main/java/com/househost/booking/checking/application/service/CheckInBookingResolver.java`
- `src/main/java/com/househost/booking/checking/application/service/CheckInGuestResolver.java`
- `src/main/java/com/househost/booking/checking/application/service/CheckInRoomResolver.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutParticipantNotifier.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutBookingResolver.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutRoomResolver.java`
- `src/main/java/com/househost/publicapi/application/service/PublicBookingParticipantNotifier.java`
- `src/test/java/com/househost/booking/booking/application/service/BookingParticipantNotifierTest.java`
- `src/test/java/com/househost/booking/checking/application/service/CheckInParticipantResolverTest.java`
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutParticipantResolverTest.java`

## Files Changed

- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/guestRegistrationPolishSpec.md`
- `SDD/plans/backendSpecs/guestRegistrationPolishBackendPlan.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `src/main/java/com/househost/booking/booking/application/service/BookingService.java`
- `src/main/java/com/househost/booking/booking/application/service/BookingFormService.java`
- `src/main/java/com/househost/booking/booking/application/service/BookingGuestResolver.java`
- `src/main/java/com/househost/booking/checking/application/service/CheckInService.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutService.java`
- `src/main/java/com/househost/publicapi/application/service/PublicBookingService.java`
- `src/main/java/com/househost/publicapi/application/service/PublicBookingGuestResolver.java`
- `src/test/java/com/househost/booking/booking/application/service/BookingServiceDeletionTest.java`
- `src/test/java/com/househost/booking/booking/application/service/BookingServiceGuestStatusTest.java`
- `src/test/java/com/househost/booking/booking/architecture/CrossModuleServiceCommunicationTest.java`
- `src/test/java/com/househost/booking/checking/application/service/CheckInGuestStatusTest.java`
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutGuestStatusTest.java`
- `src/test/java/com/househost/publicapi/application/service/PublicBookingServiceTest.java`

## Files Removed

- `src/main/java/com/househost/booking/booking/application/service/BookingGuestNotifier.java`
- `src/main/java/com/househost/booking/booking/application/service/BookingRoomResolver.java`
- `src/main/java/com/househost/booking/checking/application/service/CheckInPartyNotifier.java`
- `src/main/java/com/househost/booking/checking/application/service/CheckInPartyResolver.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutPartyNotifier.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutPartyResolver.java`
- `src/main/java/com/househost/publicapi/application/service/PublicBookingGuestNotifier.java`
- `src/main/java/com/househost/publicapi/application/service/PublicBookingRoomResolver.java`
- `src/test/java/com/househost/booking/booking/application/service/BookingGuestNotifierTest.java`

## Flows Implemented

- Booking queries Guest and Room services directly and routes every guest-status
  consequence through `BookingParticipantNotifier` and
  `BookingGuestResolver`.
- Check-in queries Booking, Guest and Room directly, then routes completed
  transition effects through one `CheckInParticipantNotifier` and specialized
  Booking, Guest and Room Resolvers.
- Checkout queries Booking directly and routes completion through one
  `CheckOutParticipantNotifier` and specialized Booking and Room Resolvers.
- Public booking queries Room directly. Guest creation and status
  synchronization pass through `PublicBookingParticipantNotifier`; the target
  Booking service then uses its own participant flow for recomputation.
- Reservation-wide priority, transfer recomputation and bookingless check-in
  remain unchanged.

## Technical And MVP Decisions

- Query-only forwarding Resolvers were removed because simple reads are allowed
  to call the destination module's public service directly.
- Cross-module mutation Resolvers remain small and target-specific.
- Public booking enters Booking through `BookingService.synchronizeGuestStatus`
  instead of injecting Booking's internal Resolver across a module boundary.
- Existing transactions continue to contain persistence and participant
  effects; no database migration or asynchronous delivery was introduced.

## Difficulties, Problems And Resolutions

- Task `020b` used one Notifier per participant and mixed query forwarding with
  effects. The flow was split by responsibility and centralized in one
  `ParticipantNotifier` per source module.
- Public booking previously depended on Booking's internal guest Resolver. A
  public Booking service entry point now delegates to Booking's notifier.
- Existing tests asserted that no external concrete service could be injected.
  The architecture test now distinguishes permitted query dependencies from
  forbidden direct Resolver dependencies.

## Tests And Verification

- Focused notifier, resolver, booking, check-in, checkout, public-booking and
  architecture tests: passed.
- `./mvnw test`: passed, 226 tests with zero failures and zero errors.
- `git diff --check`: passed.
- Search confirmed the principal services inject no Resolver and obsolete
  participant-specific Notifiers and query-only Resolvers are absent.

## Prerequisite Review

- Guest status remains server-owned and restricted to the four specified
  English values.
- Booking-wide status priority and previous/current guest recomputation remain
  intact.
- Booking-backed check-in and checkout still obtain the guest consequence from
  `BookingService.setStatus`; bookingless check-in still assigns `IN_STAY`.
- Each source module in scope has one participant notifier for external effects.
- Financial, audit and supplier port/adapter exceptions remain unchanged.
- Domain models gained no framework, persistence or cross-module dependencies.
- No frontend, database, checkout-history or rating scope was introduced.
- All acceptance criteria and prerequisite documents are conformant.
