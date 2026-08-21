# Task 012b DONE — Preserve Stay History On Booking Deletion

## Status

Completed on 10 August 2026 after explicit user approval.

## Implementation Area

Backend (`b`).

## Objective

Allow an authorized reservation deletion to preserve related check-in and
checkout history, detach their booking association atomically and keep
non-authentication failures from terminating a valid frontend session.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/backendSpecs/bookingStayHistoryRetentionSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/bookingStayHistoryRetentionBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- current booking, check-in, checkout, schema compatibility, security and
  exception-handling implementation files;
- current tests for booking persistence, operational DTOs, authorization and
  JWT handling.

## Dependencies

- Existing reservation, check-in, checkout, JWT authentication and audit
  capabilities available.
- No dependency on task `011b`; this corrective task is ordered before it.

## Scope

- Make the check-in and checkout booking associations explicitly optional.
- Make both booking join columns nullable and configure their MySQL foreign
  keys with `ON DELETE SET NULL` through idempotent schema compatibility.
- Remove the legacy compatibility behavior that makes checkout `booking_id`
  mandatory again.
- Make check-in and checkout persistence mapping safe in both directions when
  no booking is associated.
- Make checkout response mapping return null `bookingId`, matching check-in.
- Preserve guest, room and every check-in/checkout-owned historical field.
- Keep reservation deletion transactional and auditable without cascade
  deletion of stay history.
- Preserve 401 exclusively for missing, invalid or expired authentication and
  prevent protected ERROR dispatch from masking application failures as 401.
- Add focused persistence, mapping, DTO, deletion, security and migration
  tests.

## Out Of Scope

- Frontend layout or interaction changes.
- Deleting check-in or checkout.
- Soft-deleting or restoring reservations.
- Changing guest, room, financial or audit retention.
- Changing reservation-deletion roles.
- Creating new detached operational records.

## Expected Files

Expected changes include:

```text
src/main/java/com/househost/booking/checking/adapter/out/persistence/entity/CheckInJpaEntity.java
src/main/java/com/househost/booking/checking/adapter/out/persistence/entity/CheckInPersistenceMapper.java
src/main/java/com/househost/booking/checkout/adapter/out/persistence/entity/CheckOutJpaEntity.java
src/main/java/com/househost/booking/checkout/adapter/out/persistence/entity/CheckOutPersistenceMapper.java
src/main/java/com/househost/booking/checkout/application/dto/CheckOutResponseDTO.java
src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java
src/main/java/com/househost/security/adapter/in/config/SecurityConfig.java
src/test/java/com/househost/booking/...
src/test/java/com/househost/security/...
```

`BookingService.java`, audit contracts or controlled exception files may change
only if required to preserve transactionality, minimal successful audit
evidence or a truthful non-401 failure contract.

## Acceptance Criteria

- Deleting a reservation with a check-in removes the reservation and preserves
  the check-in with null `bookingId`.
- Deleting a reservation with a checkout removes the reservation and preserves
  the checkout with null `bookingId`.
- A reservation with both records detaches and preserves both atomically.
- Preserved records retain guest, room, operational facts, status and
  timestamps.
- No booking-to-history association uses cascade removal.
- Check-in and checkout mappers accept a null booking in both directions.
- Check-in and checkout response DTOs serialize detached history without an
  exception and with null `bookingId`.
- Existing non-detached check-in and checkout behavior remains compatible.
- MySQL columns are nullable and both foreign keys report
  `DELETE_RULE = SET NULL` after migration.
- Repeated application startup does not repeatedly rebuild a compatible schema
  or damage existing values.
- Reservation deletion remains restricted to the existing management roles.
- Missing, invalid or expired authentication returns 401; insufficient role
  returns 403; persistence/application failures do not become 401.
- Successful deletion remains auditable and failed deletion does not record
  success.
- Focused tests, the full Maven suite and `git diff --check` pass.

## Verification Commands

At minimum:

```text
./mvnw test
git diff --check
```

When MySQL is available, the report must also record read-only before/after
queries proving column nullability, foreign-key delete rules and preservation
of check-in/checkout rows after deleting a fixture reservation.

## Required Report

Create after implementation:

```text
SDD/ImplementationReport/YYYY-MM-DD-012b-preserve-stay-history-on-booking-deletion.md
```
