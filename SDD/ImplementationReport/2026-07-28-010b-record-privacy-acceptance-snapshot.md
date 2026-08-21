# Implementation Report — Task 010b Independent Privacy Acceptance Snapshot

## Task And Execution

- Task: `010b DONE — Record Independent Privacy Acceptance Snapshot`.
- Execution completed: 28 July 2026.
- Implementation file: `SDD/implementation/implementation-order.md`.
- Completion state: complete.

## Documents Read

- `AGENTS.md`;
- `SDD/specs/sddSpec.md`;
- `SDD/specs/cantinhoDasLavandasMainSpec.md`;
- `SDD/specs/lgpdGovernanceSpec.md`;
- `SDD/specs/moduleArchitectureSpec.md`;
- `SDD/specs/publicBookingDataMinimizationSpec.md`;
- `SDD/specs/backendSpecs/privacyPolicySubmoduleSpec.md`;
- `SDD/specs/frontendSpecs/publicPrivacyPolicyExperienceSpec.md`;
- `SDD/plans/backendSpecs/publicBookingPrivacyAcceptanceBackendPlan.md`;
- `SDD/implementation/task-bootstrap.md`;
- `SDD/implementation/implementation-order.md`;
- `SDD/tasks/backendSpecs/010b-DONE-record-privacy-acceptance-snapshot.md`.

## Files Created

- `src/main/java/com/househost/privacy/policy/domain/exception/PrivacyPolicyConflictException.java`;
- `src/test/java/com/househost/booking/booking/domain/model/BookingPrivacyAcceptanceTest.java`;
- `src/test/java/com/househost/booking/booking/adapter/out/persistence/entity/BookingPrivacySnapshotPersistenceMapperTest.java`;
- `src/test/java/com/househost/booking/booking/architecture/BookingPrivacyPersistenceIsolationTest.java`;
- `src/test/java/com/househost/publicapi/adapter/in/rest/PublicBookingPolicyConflictHttpTest.java`;
- `src/test/java/com/househost/shared/exception/PrivacyPolicyConflictExceptionHandlerTest.java`;
- `SDD/ImplementationReport/2026-07-28-010b-record-privacy-acceptance-snapshot.md`.

## Files Changed

- `src/main/java/com/househost/publicapi/application/dto/PublicBookingRequestDTO.java`;
- `src/main/java/com/househost/publicapi/application/service/PublicBookingService.java`;
- `src/main/java/com/househost/privacy/policy/application/port/in/PublicPrivacyPolicyUseCase.java`;
- `src/main/java/com/househost/privacy/policy/application/service/PrivacyPolicyService.java`;
- `src/main/java/com/househost/booking/booking/domain/model/Booking.java`;
- `src/main/java/com/househost/booking/booking/application/dto/BookingResponseDTO.java`;
- `src/main/java/com/househost/booking/booking/adapter/out/persistence/entity/BookingJpaEntity.java`;
- `src/main/java/com/househost/booking/booking/adapter/out/persistence/entity/BookingPersistenceMapper.java`;
- `src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java`;
- `src/main/java/com/househost/shared/exception/GlobalExceptionHandler.java`;
- `src/test/java/com/househost/publicapi/application/service/PublicBookingServiceTest.java`;
- `src/test/java/com/househost/privacy/policy/application/service/PrivacyPolicyServiceTest.java`;
- `SDD/implementation/task-bootstrap.md`;
- `SDD/implementation/implementation-order.md`;
- `SDD/tasks/backendSpecs/010b-DONE-record-privacy-acceptance-snapshot.md`.

## Flows Implemented

The public booking input no longer accepts a client-declared policy version. It
accepts a transient `privacyPolicyId`, requires acknowledgement and asks the
policy application contract to resolve the current publication. Only the
server-resolved version and content hash enter the booking domain.

Acceptance validation obtains the same pessimistic current-policy lock used by
publication. Because booking creation and policy validation join one database
transaction, publication either waits for an accepted booking to commit or
completes first and causes the stale submission to receive HTTP 409. Unknown
IDs remain controlled HTTP 400 errors; absence of a current policy remains HTTP
503.

The booking stores policy version, content hash and acceptance time as an
immutable, self-contained snapshot. Re-registering acceptance is rejected.
Administrative booking responses expose the evidence fields, and the minimized
`PRIVACY_ACCEPTED` audit event records version and hash without policy ID,
policy content or guest contact.

## Persistence Isolation Decision

Only nullable `privacy_policy_content_hash varchar(71)` was added to `bookings`.
No `privacy_policy_id` column, foreign key, `@ManyToOne`, `@OneToOne` or Privacy
JPA type exists in Booking. The transient ID is absent from the domain, booking
response, persistence mapper, entity, table and booking-owned audit metadata.

This deliberate snapshot design keeps Booking readable if Privacy persistence
is unavailable or migrated independently. The version remains a string to
preserve existing historical values, while new public bookings store the
server version as its decimal string.

## Legacy Migration

The compatibility runner adds the hash column idempotently and performs no
backfill. Existing policy-version strings and acceptance timestamps are not
updated. A legacy acceptance is therefore represented by its original
version/time and a null hash; version 2 and its hash are never inferred.

## Difficulties, Problems And Resolutions

Concurrency could not be protected by comparing the policy before opening the
booking transaction, because publication could occur between validation and
save. The acceptance lookup was therefore added as a transactional pessimistic
read of the unique current-policy slot. Loading the current policy under that
lock gives booking and publication one consistent ordering.

The frontend still sends the old hard-coded version until proposed task `006f`
is executed. This backend task intentionally delivers the new contract first;
the public endpoint now fails closed with HTTP 400 when the transient policy ID
is absent instead of accepting unverifiable evidence.

## Tests And Verification

- focused booking, policy, persistence, HTTP and exception tests: passed;
- final full Maven suite: 126 tests passed, zero failures, zero errors and zero
  skipped tests;
- `git diff --check`: passed;
- architecture/source checks found no policy ID, policy JPA entity or Privacy
  import in Booking domain/persistence;
- live application startup against MySQL: successful;
- missing transient ID request: controlled HTTP 400;
- before migration: 3 bookings, legacy checksum `2578253035`, no hash column,
  no policy-ID column and no booking-to-policy FK;
- after migration: the same 3 bookings and checksum, zero non-null legacy
  hashes, one nullable hash column, no policy-ID column and no booking-to-policy
  FK;
- backend was shut down after verification and port 8080 was released.

## Prerequisite And Acceptance Review

The result was compared with every required spec, plan, task criterion and
active SDD rule. Client policy version/hash authority was removed, current
policy evidence is resolved by the server under transaction-safe locking,
legacy evidence is unchanged, the snapshot is independently persistent and no
relational or JPA coupling to Privacy was introduced.

No contradiction remains. Every acceptance criterion is satisfied.
