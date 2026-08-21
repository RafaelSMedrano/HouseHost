# Task 015b DONE — Remove Legacy Financial Booking Constraint

## Status

Completed on 12 August 2026 after the user's explicit request to correct
reservation deletion blocked by a financial transaction.

## Implementation Area

Backend (`b`).

## Objective

Allow reservation deletion to preserve financial history by completing the
migration from the obsolete `financial_transactions.booking_id` association to
the current financial source representation.

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
- current schema compatibility runner, financial transaction persistence and
  booking-deletion integration tests.

## Dependencies

- `SDD/tasks/backendSpecs/012b-DONE-preserve-stay-history-on-booking-deletion.md`
- Existing `source_type` and `source_id` financial transaction representation.

## Scope

- Backfill current financial source fields from a legacy `booking_id` when the
  current source is absent.
- Drop every foreign key attached to the legacy financial booking column.
- Drop the obsolete column after its values have been migrated.
- Make repeated startup a no-op after successful migration.
- Preserve financial transactions and their monetary, party, date, status and
  current source data.
- Add focused schema and relational deletion tests.

## Out Of Scope

- Deleting financial transactions.
- Changing financial retention rules or reservation-deletion permissions.
- Introducing a financial-to-booking JPA association.
- Frontend changes.
- Refactoring the general compatibility runner.

## Expected Files

```text
src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java
src/test/java/com/househost/config/DatabaseSchemaCompatibilityRunnerFinancialBookingTest.java
src/test/java/com/househost/booking/booking/adapter/out/persistence/BookingFinancialHistoryDeletionIntegrationTest.java
```

## Acceptance Criteria

- A legacy financial booking identifier is copied to `source_type = BOOKING`
  and `source_id` before the legacy association is removed.
- Existing non-null current source data is not overwritten.
- Every foreign key for `financial_transactions.booking_id` is removed before
  the column is dropped.
- Repeated startup does not repeat the migration after the column is absent.
- Deleting the booking succeeds and the financial transaction remains stored
  with its source identifier.
- No financial cascade removal or new JPA booking association is introduced.
- Focused tests, the full Maven suite and `git diff --check` pass.

## Verification Commands

```text
./mvnw test
git diff --check
```

## Required Report

Create after implementation:

```text
SDD/ImplementationReport/YYYY-MM-DD-015b-remove-legacy-financial-booking-constraint.md
```
