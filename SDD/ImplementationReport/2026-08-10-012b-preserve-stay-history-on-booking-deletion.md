# Implementation Report — Task 012b Preserve Stay History On Booking Deletion

## Task And Execution

- Task: `012b DONE — Preserve Stay History On Booking Deletion`.
- Execution completed: 10 August 2026.
- Implementation file: `SDD/implementation/implementation-order.md`.
- Completion state: complete.

## Documents Read

- `AGENTS.md`;
- `SDD/specs/sddSpec.md`;
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`;
- `SDD/specs/moduleArchitectureSpec.md`;
- `SDD/specs/backendSpecs/bookingStayHistoryRetentionSpec.md`;
- `SDD/plans/backendSpecs/bookingStayHistoryRetentionBackendPlan.md`;
- `SDD/implementation/task-bootstrap.md`;
- `SDD/implementation/implementation-order.md`;
- `SDD/tasks/backendSpecs/012b-DONE-preserve-stay-history-on-booking-deletion.md`;
- current booking, check-in, checkout, schema compatibility, security,
  exception-handling and related test files required by the task.

## Files Created

- `src/test/java/com/househost/booking/booking/adapter/in/rest/BookingDeletionAuthorizationTest.java`;
- `src/test/java/com/househost/booking/booking/adapter/out/persistence/BookingStayHistoryDeletionIntegrationTest.java`;
- `src/test/java/com/househost/booking/booking/application/service/BookingServiceDeletionTest.java`;
- `src/test/java/com/househost/booking/checking/adapter/out/persistence/entity/CheckInDetachedBookingPersistenceMapperTest.java`;
- `src/test/java/com/househost/booking/checkout/adapter/out/persistence/entity/CheckOutDetachedBookingPersistenceMapperTest.java`;
- `src/test/java/com/househost/config/DatabaseSchemaCompatibilityRunnerStayHistoryTest.java`;
- `SDD/ImplementationReport/2026-08-10-012b-preserve-stay-history-on-booking-deletion.md`.

## Files Changed

- `pom.xml`;
- `src/main/java/com/househost/booking/booking/application/service/BookingService.java`;
- `src/main/java/com/househost/booking/checking/adapter/out/persistence/entity/CheckInJpaEntity.java`;
- `src/main/java/com/househost/booking/checking/adapter/out/persistence/entity/CheckInPersistenceMapper.java`;
- `src/main/java/com/househost/booking/checkout/adapter/out/persistence/entity/CheckOutJpaEntity.java`;
- `src/main/java/com/househost/booking/checkout/adapter/out/persistence/entity/CheckOutPersistenceMapper.java`;
- `src/main/java/com/househost/booking/checkout/application/dto/CheckOutResponseDTO.java`;
- `src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java`;
- `src/main/java/com/househost/security/adapter/in/config/SecurityConfig.java`;
- `SDD/implementation/task-bootstrap.md`;
- `SDD/implementation/implementation-order.md`;
- `SDD/tasks/backendSpecs/012b-DONE-preserve-stay-history-on-booking-deletion.md`.

## Flows Implemented

Check-in and checkout now declare their reservation association as optional.
Their `booking_id` columns remain unique but become nullable, and the MySQL
compatibility runner replaces incompatible foreign keys with `ON DELETE SET
NULL`. A compatible schema is detected before DDL, so later startups are
idempotent and existing indexes and valid associations are retained.

Deleting a reservation remains the booking application command and is now
explicitly transactional. The database detaches both operational records as
part of that deletion instead of cascading their removal. Guest, room, status,
timestamps and the operational facts owned by check-in and checkout remain in
their respective rows. Successful deletion retains the existing minimal audit
event; a persistence failure occurs before that success event is emitted.

Both persistence mappers accept an absent booking in both directions. Check-in
already exposed a nullable `bookingId`; checkout now mirrors that response
contract. Ordinary records that still reference a reservation continue using
the same mapping and endpoint shapes.

Only `DispatcherType.ERROR` is permitted before the ordinary HTTP authorization
rules. A container error dispatch can therefore preserve the original server
error instead of being replaced by HTTP 401, while ordinary booking deletion
still requires a management role. Missing or invalid authentication remains
HTTP 401 and insufficient permission remains HTTP 403.

## Technical And MVP Decisions

`ON DELETE SET NULL` is the authoritative detachment mechanism because it
updates check-in and checkout in the same database statement and transaction as
the booking deletion. No JPA remove cascade or cross-module history-deletion
call was added.

The existing startup compatibility runner was extended instead of introducing
a second migration framework. Metadata-derived identifiers are validated and
quoted before use. H2 was added only in test scope to exercise actual
referential behavior and rollback without making production depend on it.

No detached operational record can be created through a normal create API;
null booking exists only as the historical state resulting from reservation
deletion or legacy compatibility.

## Difficulties, Problems And Resolutions

The installed local MySQL server was stopped. Both the normal and escalated
startup attempts failed because `/usr/local/mysql/data` was not writable by the
server process, and `mysqladmin ping` confirmed that no socket was available.
Consequently, live MySQL before/after queries could not be executed in this
environment.

The unavailable server was not replaced by an unverified claim. MySQL-specific
metadata inspection and exact generated DDL are covered by focused runner
tests, including the no-DDL compatible-schema path. A relational H2 integration
test in MySQL mode verifies database state after deletion and verifies rollback
of the booking plus both automatic detachments. Live MySQL verification remains
an environment-level follow-up, not an implementation defect.

One initial ERROR-dispatch test expected HTTP 404 from `/error`; Spring's basic
error controller correctly returned HTTP 500. The assertion was corrected to
verify the intended contract: a server error is preserved and does not become
401.

## Tests And Verification

- focused mapper, DTO, schema, authorization, transaction and relational
  deletion tests: 15 passed, zero failures;
- final full Maven suite: 141 tests passed, zero failures, zero errors and zero
  skipped tests;
- `git diff --check`: passed;
- relational deletion test: booking absent; check-in and checkout present with
  null `booking_id`; guest, room, status and representative owned fields
  unchanged;
- relational rollback test: booking and both original associations restored;
- migration test: both columns changed to nullable and both foreign keys emitted
  with `ON DELETE SET NULL` only when incompatible;
- migration idempotence test: no DDL emitted for an already compatible schema;
- authorization tests: management delete 200, reception delete 403, ordinary
  unauthenticated delete 401 and unauthenticated ERROR dispatch remains 5xx;
- existing invalid-token JWT tests: passed;
- live MySQL schema and fixture verification: not run because the installed
  server could not start due to its data-directory permissions.

## Prerequisite And Acceptance Review

The result was compared with every required spec, prerequisite spec, plan,
task criterion and active SDD rule. Reservation deletion is atomic and
auditable; check-in and checkout are retained independently; mappers and DTOs
are null-safe; no remove cascade exists; migration behavior is idempotent; and
ordinary authentication and role boundaries remain unchanged.

No document contradiction was found. All implementation acceptance criteria
are satisfied. The conditional live-MySQL verification was recorded as not run
because MySQL was unavailable, as required by the task's verification rule.
