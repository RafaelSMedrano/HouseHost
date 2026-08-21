# Implementation Report — Task 026b Query Ratings By Booking Guest

## Result

Task `026b` is complete. General and guest rating histories now materialize
minimized summaries directly from `Rating → Booking → Guest`, and booking
deletion is blocked while a rating references the booking.

## Governing Documents Reviewed

- `SDD/specs/sddSpec.md`;
- `SDD/specs/bookingServiceRatingSpec.md` and its prerequisite chain;
- `SDD/specs/moduleArchitectureSpec.md`;
- `SDD/specs/lgpdGovernanceSpec.md`;
- `SDD/plans/backendSpecs/bookingServiceRatingBackendPlan.md`;
- `SDD/implementation/task-bootstrap.md`;
- `SDD/implementation/implementation-order.md`;
- `SDD/tasks/backendSpecs/026b-DONE-query-ratings-by-booking-guest.md`.

## Implementation

- Replaced full rating aggregate loading in list endpoints with immutable JPQL
  constructor projections selecting only booking/guest link identifiers, guest
  display name, stay dates, evaluation date, six scores and observations.
- Kept newest-first deterministic ordering by `evaluatedAt DESC, id DESC` and
  retained bounded zero-based pagination.
- Resolved guest history exclusively through the rating's booking ownership;
  no rating collection was added to Guest or Booking.
- Removed the rating identifier from the summary response and verified the
  exact minimized response field set, excluding unrelated contact, document,
  room and financial data.
- Added Hibernate statistics coverage proving that page materialization and DTO
  mapping execute at most the page query plus its count query, without lazy
  per-row loading.
- Integrated the existing `RatingUseCase.existsByBookingId` query before booking
  deletion. A rated booking now returns the existing rating conflict response
  without deleting the booking, rating or recording a deletion audit.
- Preserved missing-record behavior: a missing booking fails before querying
  ratings, while a missing guest returns an empty bounded page.
- Added database-level coverage proving the rating foreign key also prevents
  direct deletion and preserves the rating row.

## Technical Decisions

- `RatingSummaryRecord` is an application carrier rather than a transport DTO;
  persistence returns it directly and the API layer maps it to
  `RatingSummaryDTO`.
- The projection joins only the ownership path required for display. It avoids
  loading Booking, Guest or Room entities and therefore cannot trigger their
  lazy relationships during response serialization.
- `BookingService` uses the public `RatingUseCase` for the simple read-only
  existence check. Lazy injection breaks the pre-existing reciprocal ratings
  eligibility dependency while retaining the public module boundary.
- Task `027b` remains responsible for ratings security and audit completion; no
  frontend task was executed.

## Files Created

- `src/main/java/com/househost/ratings/application/records/RatingSummaryRecord.java`
- `SDD/ImplementationReport/2026-08-13-026b-query-ratings-by-booking-guest.md`

## Files Changed

- `src/main/java/com/househost/ratings/application/records/RatingPageRecord.java`
- `src/main/java/com/househost/ratings/application/dto/RatingSummaryDTO.java`
- `src/main/java/com/househost/ratings/application/dto/RatingPageResponseDTO.java`
- `src/main/java/com/househost/ratings/adapter/out/persistence/RatingJpaRepository.java`
- `src/main/java/com/househost/ratings/adapter/out/persistence/RatingPersistenceAdapter.java`
- `src/main/java/com/househost/booking/booking/application/service/BookingService.java`
- `src/test/java/com/househost/ratings/application/service/RatingServiceTest.java`
- `src/test/java/com/househost/ratings/adapter/out/persistence/RatingPersistencePaginationTest.java`
- `src/test/java/com/househost/ratings/adapter/out/persistence/RatingPersistenceConstraintTest.java`
- `src/test/java/com/househost/booking/booking/application/service/BookingServiceGuestStatusTest.java`
- `src/test/java/com/househost/booking/booking/application/service/BookingServiceDeletionTest.java`
- `src/test/java/com/househost/booking/booking/adapter/in/rest/BookingDeletionAuthorizationTest.java`
- `SDD/implementation/implementation-order.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/plans/backendSpecs/bookingServiceRatingBackendPlan.md`
- `SDD/tasks/backendSpecs/027b-DONE-secure-audit-and-verify-ratings-module.md`
- `SDD/tasks/frontendSpecs/031f-DONE-create-ratings-navigation-and-api.md`
- `SDD/ImplementationReport/2026-08-12-024b-build-ratings-use-cases-and-api.md`

## Files Renamed

- `SDD/tasks/backendSpecs/026b-query-ratings-by-booking-guest.md` to
  `SDD/tasks/backendSpecs/026b-DONE-query-ratings-by-booking-guest.md`.

## Verification

- Focused ratings, booking-deletion, authorization and architecture tests:
  passed.
- `./mvnw -q test`: 300 tests passed; 0 failures; 0 errors; 0 skipped.
- Hibernate statistics query-bound assertion: passed with no per-row loading.
- `git diff --check`: passed.
- No runtime migration was executed against MySQL; referential integrity was
  verified through the focused persistence constraint test.

## Prerequisite Review

The result preserves the independent ratings aggregate, public cross-module
capability boundary, booking-derived ownership and LGPD response minimization.
Guest and Booking remain free of rating collections. All task acceptance
criteria passed, and no dependent security, audit or frontend task was
implicitly authorized.
