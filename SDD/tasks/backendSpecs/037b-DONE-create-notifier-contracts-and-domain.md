# Task 037b DONE — Create Provider-Neutral Notifier Contracts And Domain

## Status

Completed and verified on 2026-08-20.

## Implementation Area

Backend (`b`).

## Objective

Create the reusable notifier hexagon's provider-neutral application contracts,
immutable records and domain state model without persistence, scheduling, AWS
or consumer-domain dependencies.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/notifierModuleSpec.md`
- `SDD/specs/publicBookingNotificationSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/operationalLoggingSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/notifierModuleBackendPlan.md`
- `SDD/plans/backendSpecs/publicBookingNotificationBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`

## Scope

- Create the notifier `domain`, `application/records`, `application/port/in`,
  `application/port/out` and `application/service` foundations.
- Model `NotificationIntent`, `NotificationProviderEvent`, statuses, event
  types and provider-neutral failure categories.
- Create request, message, delivery-result, feedback, claim and retry records.
- Create inbound request and feedback use-case contracts.
- Create outbound persistence and email-delivery contracts.
- Enforce one recipient per intent, immutable message snapshots, textual
  notification types and source-scoped idempotency keys.
- Add domain and architecture tests proving no consumer, AWS, Spring, JPA or
  transport dependency enters the core.

## Out Of Scope

- Database entities or schema changes.
- Scheduler and retry execution.
- AWS SES and SNS adapters.
- Public booking integration.

## Acceptance Criteria

- Core contracts represent every field required by the governing spec.
- No notifier core type contains `bookingId`, `guestId` or a consumer foreign
  identifier.
- Domain transitions reject invalid state changes and preserve terminal states.
- Records and identifiers follow project naming and suffix conventions.
- Forbidden-import architecture checks, focused tests and `git diff --check`
  pass.

## Required Report

Create after implementation:

`SDD/ImplementationReport/YYYY-MM-DD-037b-create-notifier-contracts-and-domain.md`
