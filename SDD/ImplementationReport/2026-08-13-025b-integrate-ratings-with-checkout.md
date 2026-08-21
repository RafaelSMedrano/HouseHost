# Implementation Report — Task 025b Integrate Ratings With Checkout

## Task And Implementation File

- Task: `025b` — Integrate Ratings With Checkout.
- Executed task file:
  `SDD/tasks/backendSpecs/025b-DONE-integrate-ratings-with-checkout.md`.
- Implementation rules:
  `SDD/implementation/task-bootstrap.md` and
  `SDD/implementation/implementation-order.md`.

## Specs, Prerequisites And Plan Read

- `SDD/specs/sddSpec.md`;
- `SDD/specs/bookingServiceRatingSpec.md`;
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`;
- `SDD/specs/moduleArchitectureSpec.md`;
- `SDD/specs/lgpdGovernanceSpec.md`;
- `SDD/specs/backendSpecs/bookingStayHistoryRetentionSpec.md`;
- `SDD/specs/guestRegistrationPolishSpec.md`;
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`;
- `SDD/plans/backendSpecs/bookingServiceRatingBackendPlan.md`;
- dependency task
  `SDD/tasks/backendSpecs/024b-DONE-build-ratings-use-cases-and-api.md`.

## Flows Implemented

- Replaced the checkout aggregate integer rating with a nested request carrying
  all six required scores and optional observations.
- Integrated the state-changing flow as
  `CheckOutService → CheckOutParticipantNotifier → CheckOutRatingResolver → RatingUseCase`.
- Limited rating creation to the same persistent completion evidence already
  used to apply guest history exactly once.
- Kept checkout, booking, room, guest-history and rating effects in the same
  transaction so any participant or rating failure rolls the whole operation
  back.
- Kept pending and cancelled checkout free of rating effects and made a retry
  after rollback produce exactly one rating.
- Removed the obsolete generic rating from Guest and CheckOut models, DTOs,
  services, validation, persistence mappings and JPA entities.
- Added idempotent removal of `guests.rating` and `check_outs.rating`, without
  backfill into the six new criteria.

## Technical And MVP Decisions

- The nested checkout property remains named `rating`, but its type is the
  checkout-owned `CheckOutRatingRequestDTO`; it contains no client-controlled
  booking identifier or evaluation timestamp.
- `CheckOutRatingResolver` derives the booking identifier from the persisted
  checkout and translates the checkout payload into the public ratings use-case
  contract. The ratings module continues deriving `evaluatedAt` from the
  completed checkout.
- Rating creation is guarded by `guestHistoryApplied == false` while the
  checkout is completed. This reuses persistent completion evidence, avoids a
  second partial marker and prevents legacy or repeated completed checkout from
  creating a new rating.
- The Resolver injects the public `RatingUseCase` lazily to break the existing
  checkout eligibility-query bean cycle while preserving the required public
  use-case boundary.
- No frontend file was changed. Frontend task `032f` remains responsible for
  replacing the existing control and sending the new nested contract.

## Difficulties, Problems And Resolutions

- Ratings creation validation queries the completed checkout, while checkout
  now invokes ratings creation. Direct eager construction would create a Spring
  bean cycle. Lazy injection was confined to the Resolver boundary, and the
  complete application contexts in the backend suite verified startup.
- The checkout transaction test previously persisted the obsolete integer
  rating. Its schema and persistence stub were updated to represent a separate
  unique ratings table and to prove rating-failure rollback plus safe retry.
- The working tree contained extensive pre-existing changes from earlier
  tasks. They were preserved; only files required by task `025b` and its SDD
  completion references were edited.

## Files Created

- `src/main/java/com/househost/booking/checkout/application/dto/CheckOutRatingRequestDTO.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutRatingResolver.java`
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutRatingContractTest.java`
- `SDD/ImplementationReport/2026-08-13-025b-integrate-ratings-with-checkout.md`

## Files Changed

- `src/main/java/com/househost/booking/checkout/application/dto/CheckOutRequestDTO.java`
- `src/main/java/com/househost/booking/checkout/application/dto/CheckOutResponseDTO.java`
- `src/main/java/com/househost/booking/checkout/domain/model/CheckOut.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutService.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutValidationService.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutParticipantNotifier.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutGuestResolver.java`
- `src/main/java/com/househost/booking/checkout/adapter/out/persistence/entity/CheckOutJpaEntity.java`
- `src/main/java/com/househost/booking/checkout/adapter/out/persistence/entity/CheckOutPersistenceMapper.java`
- `src/main/java/com/househost/guest/domain/model/Guest.java`
- `src/main/java/com/househost/guest/application/service/GuestService.java`
- `src/main/java/com/househost/guest/application/service/GuestDataSecurityService.java`
- `src/main/java/com/househost/guest/application/dto/GuestRegisterRequestDTO.java`
- `src/main/java/com/househost/guest/application/dto/GuestRegisterResponseDTO.java`
- `src/main/java/com/househost/guest/adapter/out/persistence/entity/GuestJpaEntity.java`
- `src/main/java/com/househost/guest/adapter/out/persistence/entity/GuestPersistenceMapper.java`
- `src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java`
- `src/test/java/com/househost/booking/checkout/adapter/out/persistence/entity/CheckOutDetachedBookingPersistenceMapperTest.java`
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutHistoryServiceTest.java`
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutGuestStatusTest.java`
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutParticipantResolverTest.java`
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutTransactionIntegrationTest.java`
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutValidationServiceTest.java`
- `src/test/java/com/househost/booking/booking/architecture/CrossModuleServiceCommunicationTest.java`
- `src/test/java/com/househost/config/DatabaseSchemaCompatibilityRunnerCheckOutHistoryTest.java`
- `src/test/java/com/househost/guest/domain/model/GuestProfileTest.java`
- `src/test/java/com/househost/guest/service/GuestRegistrationContractTest.java`
- `src/test/java/com/househost/guest/adapter/out/persistence/entity/GuestPersistenceMapperTest.java`
- `SDD/implementation/implementation-order.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/plans/backendSpecs/bookingServiceRatingBackendPlan.md`
- `SDD/tasks/backendSpecs/027b-DONE-secure-audit-and-verify-ratings-module.md`
- `SDD/tasks/frontendSpecs/032f-DONE-build-checkout-rating-stars.md`
- `SDD/ImplementationReport/2026-08-12-024b-build-ratings-use-cases-and-api.md`

## Files Renamed

- `SDD/tasks/backendSpecs/025b-integrate-ratings-with-checkout.md` to
  `SDD/tasks/backendSpecs/025b-DONE-integrate-ratings-with-checkout.md`.

## Files Removed

- None, apart from the old path consumed by the task-file rename above.

## Tests And Verification

- `./mvnw -q -DskipTests compile`: passed.
- Focused checkout, guest, ratings and schema tests: passed.
- Focused completion, rollback, retry, contract and architecture tests: passed.
- `./mvnw -q test`: 293 tests passed; 0 failures; 0 errors; 0 skipped.
- `git diff --check`: passed.
- No runtime migration was executed against MySQL; the idempotent DDL behavior
  was verified with focused schema compatibility tests.

## Prerequisite Review

- The checkout integration follows the module architecture's mandatory
  `MainService → ParticipantNotifier → Resolver → public capability` flow.
- Ratings remain independent from Guest and CheckOut aggregates and retain the
  one-to-one Booking ownership defined by the ratings spec.
- No observations or score payloads were added to checkout audit metadata,
  preserving LGPD minimization requirements.
- Stay-history evidence, detached historical checkout behavior and atomic
  completion remain consistent with the stay-history and guest-registration
  prerequisite specs.
- No contradiction was found among the required specs, plan, task acceptance
  criteria or active implementation rules. All acceptance criteria passed.
