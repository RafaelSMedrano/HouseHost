# Task 038b DONE — Persist Notifier Intents And Provider Events

## Status

Complete after explicit implementation approval, verification and report on
2026-08-21.

## Implementation Area

Backend (`b`).

## Objective

Implement notifier-owned persistence for self-contained intents, atomic claims
and append-only provider events without foreign keys or repository access to a
consumer module.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/notifierModuleSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/operationalLoggingSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/notifierModuleBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/tasks/backendSpecs/037b-DONE-create-notifier-contracts-and-domain.md`

## Scope

- Create JPA entities, repositories, mappers and adapters for intents and
  provider events.
- Add idempotent schema compatibility for both notifier tables, indexes,
  uniqueness, internal foreign key and optimistic version.
- Enforce unique `(sourceSystem, idempotencyKey)` intent creation.
- Enforce unique provider-message correlation when present.
- Implement bounded eligible selection, atomic claims, durable `nextAttemptAt`,
  `leaseUntil`, abandoned-claim recovery and state persistence.
- Persist provider events idempotently by transport and provider identifiers.
- Add retention-selection and content-anonymization persistence operations.
- Add mapping, schema, idempotency, locking and concurrency integration tests.

## Out Of Scope

- Running a scheduler.
- Calling SES.
- Receiving SNS HTTP requests.
- Public booking integration.

## Acceptance Criteria

- Notifier tables have no foreign key to booking, guest, payment or another
  consumer table.
- Duplicate request keys resolve to one intent.
- Concurrent workers cannot claim the same eligible intent.
- Expired leases become recoverable without creating a new intent.
- Duplicate provider events create one append-only record.
- Persistence retains no complete SNS envelope or raw SES payload.
- Focused integration tests, schema checks and task-scoped `git diff --check`
  pass.

## Required Report

`SDD/ImplementationReport/2026-08-21-038b-persist-notifier-intents-and-events.md`
