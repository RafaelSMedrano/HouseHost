# Implementation Report — Task 001b Login Failure State

## Task And Execution

- Task: `001b — Persist Login Failure State And Policy`.
- Implementation file: `SDD/implementation/implementation-order.md`.
- Execution date: 24 July 2026.

## Documents Read

- `SDD/specs/sddSpec.md`;
- `SDD/specs/cantinhoDasLavandasMainSpec.md`;
- `SDD/specs/lgpdGovernanceSpec.md`;
- `SDD/specs/moduleArchitectureSpec.md`;
- `SDD/specs/backendSpecs/loginFailureProtectionSpec.md`;
- `SDD/plans/backendSpecs/loginFailureProtectionBackendPlan.md`;
- `SDD/implementation/task-bootstrap.md`;
- `SDD/implementation/implementation-order.md`;
- `SDD/tasks/backendSpecs/001b-DONE-login-failure-state.md`.

## Files And Flows Implemented

Added the domain scope and control, validated policy properties, protected-key
port and HMAC-SHA-256 adapter, persistence port and JPA adapter/entity/repository/
mapper, `LoginSecurityService`, injected UTC `Clock`, MySQL compatibility table,
environment placeholders and domain/service/key tests.

The service checks all three protected scopes, records comparable failures,
clears pair/account state after success while preserving IP state, and removes
only stale state without an active restriction. The production login flow was
not changed in this task.

## Technical And MVP Decisions

- Persistence mutation is serialized with a pessimistic row lock and retries a
  concurrent unique-key race in a new programmatic transaction.
- HMAC keys are lowercase 64-character SHA-256 hexadecimal values and use a
  dedicated mandatory environment secret, independent from JWT configuration.
- Cleanup is exposed as an idempotent service operation; scheduling is deferred
  until an operational cadence is selected.

## Difficulties And Resolutions

The repository contains a large uncommitted hexagonal migration. New files were
added in its current architecture and edits to existing configuration/schema
files were kept localized. Concurrent first creation is handled through the
database unique constraint plus transaction retry.

## Tests And Verification

- `./mvnw -q -DskipTests compile` — passed.
- `./mvnw -q -Dtest=LoginSecurityControlTest,HmacLoginSecurityKeyAdapterTest,LoginSecurityServiceTest test` — passed.
- `HOUSEHOST_LOGIN_LIMIT_HMAC_SECRET=test-only-secret ./mvnw -q test` — passed.

The automated concurrency test exercises ten simultaneous service failures.
Database-specific restart durability is established by durable JPA state and
was not exercised against a live MySQL instance in this environment.

## Prerequisite Review

The result was reviewed against the mother spec, LGPD governance, module
architecture, login-failure spec, backend plan, task scope and acceptance
criteria. Domain state contains no JPA annotation; raw email, IP, password and
token are not persisted; defaults match the spec; zero/negative policy values
fail validation; scope uniqueness and cleanup protections are present; success
preserves IP state; and production login behavior remains unchanged.

No contradiction was found. Task `001b` is complete and conforms to its
prerequisites, subject to the documented absence of a live-MySQL restart test.
