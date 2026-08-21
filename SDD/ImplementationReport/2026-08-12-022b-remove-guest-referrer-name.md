# Implementation Report — Task 022b

## Execution

- Task: `022b — Remove Guest Referrer Name`.
- Authorization: explicit user request to remove the field from guest and DB.
- Completed task: `SDD/tasks/backendSpecs/022b-DONE-remove-guest-referrer-name.md`.

## Documents Read

- `SDD/specs/sddSpec.md`;
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`;
- `SDD/specs/guestRegistrationPolishSpec.md` and all its prerequisites;
- `SDD/specs/moduleArchitectureSpec.md`;
- `SDD/plans/backendSpecs/guestRegistrationPolishBackendPlan.md`;
- active implementation files.

## Files Created

- `SDD/tasks/backendSpecs/022b-DONE-remove-guest-referrer-name.md` — executable backend task.
- `SDD/ImplementationReport/2026-08-12-022b-remove-guest-referrer-name.md` — execution evidence.

## Files Changed

- Guest request/response DTOs, domain model, service, masking service, JPA entity
  and persistence mapper — remove `referredBy` while preserving `originChannel`.
- `DatabaseSchemaCompatibilityRunner.java` — drops `guests.referred_by`
  idempotently.
- Guest contract, domain, mapper and schema compatibility tests — verify the
  reduced contract and migration.
- Guest-polish spec, backend plan and SDD implementation files — record product
  behavior and task completion.

## Decisions And Flow

- Legacy `referredBy` request input is ignored during rollout and cannot affect
  guest state.
- Existing values are discarded when the compatibility runner drops the
  obsolete column; no data transformation is performed.
- Reservation origin `INDICACAO` is outside this removal and remains valid.

## Verification

- Focused Maven tests for guest contract, domain, mapper and schema migration — passed.
- Complete Maven test suite — passed.
- `git diff --check` — passed.

## Prerequisite Review

The reduced guest contract satisfies data minimization, preserves the module
architecture and does not alter reservation behavior. No contradiction remains.
