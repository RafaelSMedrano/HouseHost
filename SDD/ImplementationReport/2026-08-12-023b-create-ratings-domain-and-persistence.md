# Implementation Report — Task 023b

## Execution

- Task: `023b — Create Ratings Domain And Persistence`.
- Authorization: explicit user instruction `implementa 023b`.
- Completed task:
  `SDD/tasks/backendSpecs/023b-DONE-create-ratings-domain-and-persistence.md`.

## Documents Read

- `SDD/specs/sddSpec.md`;
- `SDD/specs/bookingServiceRatingSpec.md`;
- `SDD/plans/backendSpecs/bookingServiceRatingBackendPlan.md`;
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`;
- `SDD/specs/moduleArchitectureSpec.md`;
- `SDD/specs/lgpdGovernanceSpec.md`;
- `SDD/specs/backendSpecs/bookingStayHistoryRetentionSpec.md`;
- `SDD/specs/guestRegistrationPolishSpec.md`;
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`;
- `SDD/implementation/task-bootstrap.md`;
- `SDD/implementation/implementation-order.md`;
- current booking persistence, schema compatibility runner and related tests.

## Files Created

- `src/main/java/com/househost/ratings/domain/exception/RatingException.java`
  — ratings-specific domain failure.
- `src/main/java/com/househost/ratings/domain/model/Rating.java` — independent
  domain entity with booking association, six scores, observations and
  timestamps.
- `src/main/java/com/househost/ratings/application/port/out/RatingPersistencePort.java`
  — persistence contract expressed with domain types.
- `src/main/java/com/househost/ratings/adapter/out/persistence/entity/RatingJpaEntity.java`
  — JPA representation with one-to-one booking and score constraints.
- `src/main/java/com/househost/ratings/adapter/out/persistence/entity/RatingPersistenceMapper.java`
  — complete domain/JPA translation.
- `src/main/java/com/househost/ratings/adapter/out/persistence/RatingJpaRepository.java`
  — Spring Data repository restricted to the persistence adapter.
- `src/main/java/com/househost/ratings/adapter/out/persistence/RatingPersistenceAdapter.java`
  — implementation of the ratings persistence port.
- `src/test/java/com/househost/ratings/domain/model/RatingTest.java` — domain
  invariant, normalization and infrastructure-independence tests.
- `src/test/java/com/househost/ratings/adapter/out/persistence/entity/RatingPersistenceMapperTest.java`
  — complete mapper round-trip test.
- `src/test/java/com/househost/ratings/adapter/out/persistence/RatingPersistenceConstraintTest.java`
  — H2 integration tests for score and booking uniqueness constraints.
- `src/test/java/com/househost/config/DatabaseSchemaCompatibilityRunnerRatingsTest.java`
  — idempotent MySQL compatibility-DDL verification.
- `SDD/tasks/backendSpecs/023b-DONE-create-ratings-domain-and-persistence.md`
  — completed task document replacing its proposed filename.
- `SDD/ImplementationReport/2026-08-12-023b-create-ratings-domain-and-persistence.md`
  — this execution evidence.

## Files Changed

- `src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java`
  — invokes an idempotent `ratings` table creation with a non-null unique
  booking foreign key and six database score checks.
- `SDD/plans/backendSpecs/bookingServiceRatingBackendPlan.md` — records that
  only tasks `024b` through `027b` remain unauthorized.
- `SDD/tasks/backendSpecs/024b-DONE-build-ratings-use-cases-and-api.md` — points its
  dependency to the completed `023b` filename.
- `SDD/implementation/implementation-order.md` — points to the completed task
  and records that `023b` does not authorize subsequent tasks.
- `SDD/implementation/task-bootstrap.md` — records `023b` completion and
  leaves all later ratings tasks proposed.

## Files Removed

- `SDD/tasks/backendSpecs/023b-create-ratings-domain-and-persistence.md` — the
  proposed filename was replaced by the required `DONE` filename.

## Flows Implemented

- Construction of a rating validates the booking, evaluation timestamp and all
  six integer scores in the range `1..5`.
- Optional observations are trimmed, blank values become `null`, and content
  above 4,000 characters is rejected without truncation.
- Persistence follows `RatingPersistencePort → RatingPersistenceAdapter →
  RatingJpaRepository`, with an explicit mapper between the domain and JPA.
- MySQL compatibility startup creates `ratings` with
  `CREATE TABLE IF NOT EXISTS`; no generic guest/checkout value is read or
  transformed.

## Technical And MVP Decisions

- The domain owns a `Booking` relationship as required; the JPA entity uses
  `@OneToOne` and a unique non-null `booking_id`.
- The persistence port exposes save, lookup by rating, lookup by booking and
  existence by booking. Pagination and guest-derived queries remain in later
  tasks.
- The ordinary rating remains immutable at domain level: no score or
  observation mutation method was introduced.
- No controller, inbound use case, application service, audit adapter, checkout
  integration or legacy generic-rating removal was implemented because those
  responsibilities belong to tasks `024b` through `027b`.

## Difficulties, Problems And Resolutions

- The first mapper fixture used an ID-only booking, but the existing booking
  mapper reconstructs its guest and room. The fixture was corrected to contain
  a valid booking aggregate; production behavior was not weakened.
- The shared schema runner already contained unrelated user changes. The task
  preserved them and added only the ratings invocation and method.

## Tests And Verification

- `./mvnw -Dtest=RatingTest,RatingPersistenceMapperTest,RatingPersistenceConstraintTest,DatabaseSchemaCompatibilityRunnerRatingsTest test`
  — passed, 27 tests.
- `./mvnw test` — passed before the database integration test, 249 tests.
- `./mvnw -q test` — passed after all additions, 251 tests.
- `git diff --check` — passed.
- Tab scan across new ratings production/tests and the ratings schema test —
  passed with no matches.
- Production MySQL smoke test was not run because no production-like database
  mutation was required or authorized; idempotent SQL was verified directly
  and persistence constraints were exercised against H2.

## Prerequisite Review

The result conforms to the mother spec, module architecture, LGPD minimization,
booking-history retention and guest-polish boundaries. The ratings domain is
independent from Spring/JPA, adapters point inward through a persistence port,
observations are not logged, and the booking is not modified or cascade-deleted.

No contradiction was found. All `023b` acceptance criteria passed. Tasks
`024b` through `027b` and all ratings frontend tasks remain proposed and
unauthorized.
