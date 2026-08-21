# Task 016b DONE — Migrate Guest Status And Care Fields

## Status

Completed on 12 August 2026 after explicit user authorization.

## Implementation Area

Backend (`b`), including database compatibility.

## Objective

Migrate the guest table to the four authoritative English lifecycle statuses
and two free-text care fields, discarding data from care fields that cease to
exist.

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
- current guest persistence mapping and schema-compatibility implementation;
- existing schema-compatibility tests and MySQL test fixtures.

## Dependencies

- Existing `guests`, `bookings` and schema compatibility capabilities.
- No dependency on another task in this sequence.

## Scope

- Add `preferences_and_restrictions` and `accessibility_needs` text columns.
- Migrate guest status to `WITH_UNCONFIRMED_BOOKING`,
  `WITH_CONFIRMED_BOOKING`, `IN_STAY` or `INACTIVE` using booking priority.
- Set the database default to `INACTIVE` and narrow the enum after migration.
- Preserve only values already stored in the two new text columns.
- Do not copy or transform legacy preference rows, pet information,
  favorite-room information or the boolean accessibility flag.
- Drop `guest_preferences` and the obsolete `travels_with_pets`, `pet_type`,
  `favorite_room` and `needs_accessibility` columns together with the legacy
  data they contain.
- Make the migration conditional and idempotent for fresh, partial and legacy
  installations.
- Add migration tests, including repeated execution.

## Out Of Scope

- Guest HTTP contract changes.
- Runtime reservation/check-in/checkout status transitions.
- Frontend changes.
- Checkout history or rating behavior.

## Expected Files

Expected changes include:

```text
src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java
src/test/java/com/househost/config/DatabaseSchemaCompatibilityRunnerTest.java
src/test/java/com/househost/config/DatabaseSchemaCompatibilityRunnerMySqlTest.java
```

Exact test filenames may follow the current config-test organization.

## Acceptance Criteria

- A fresh guest schema receives both text columns and default `INACTIVE`.
- Only the four specified English status values remain accepted after migration.
- Active booking data wins over ambiguous legacy guest status using the stated
  priority; a guest without an active booking becomes `INACTIVE`.
- Values already present in the new text columns remain unchanged.
- Legacy preferences, pet, favorite-room and boolean accessibility values are
  not copied into the new fields.
- `guest_preferences`, `travels_with_pets`, `pet_type`, `favorite_room` and
  `needs_accessibility` are removed.
- Missing optional legacy tables or columns do not break startup.
- A second compatibility run makes no destructive changes or duplicate text.
- Focused tests, the full Maven suite and `git diff --check` pass.

## Verification Commands

At minimum:

```text
./mvnw test
git diff --check
```

When MySQL is available, record before/after queries for columns, enum/default,
discarded obsolete storage, retained new-text values and the second startup.

## Required Report

Create after implementation:

```text
SDD/ImplementationReport/YYYY-MM-DD-016b-migrate-guest-status-and-care-fields.md
```
