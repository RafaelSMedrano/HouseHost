# Task 042b DONE — Integrate Public Booking With Notifier

## Status

Complete after explicit implementation approval, verification and report on
2026-08-21.

## Implementation Area

Backend (`b`).

## Objective

Integrate successful public reservation requests with the completed reusable
notifier and verify the complete backend flow from transactional intent
creation through SES acceptance and SNS feedback isolation.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/publicBookingDataMinimizationSpec.md`
- `SDD/specs/publicBookingNotificationSpec.md`
- `SDD/specs/backendSpecs/notifierModuleSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/operationalLoggingSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/publicBookingNotificationBackendPlan.md`
- `SDD/plans/backendSpecs/notifierModuleBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/tasks/backendSpecs/037b-DONE-create-notifier-contracts-and-domain.md`
- `SDD/tasks/backendSpecs/038b-DONE-persist-notifier-intents-and-events.md`
- `SDD/tasks/backendSpecs/039b-DONE-implement-notifier-dispatch-and-retry.md`
- `SDD/tasks/backendSpecs/040b-DONE-integrate-aws-ses-outbound-adapter.md`
- `SDD/tasks/backendSpecs/041b-DONE-receive-ses-feedback-through-sns.md`

## Scope

- Add required normalized transactional email to the public guest request and
  persistence flow.
- Declare a public API outbound notification port.
- Implement a public API output adapter that calls the notifier inbound use
  case without importing notifier persistence or AWS types.
- Build minimized immutable guest request-received and management new-request
  message snapshots.
- Generate one source event ID and two stable independent idempotency keys.
- Create both intents transactionally with the `UNCONFIRMED` reservation
  request.
- Preserve WhatsApp confirmation and payment negotiation as later operations.
- Verify rollback, duplicate handling, SES unavailability and SNS feedback
  isolation from booking and guest state.
- Run complete architecture, privacy, schema and backend regression suites.

## Out Of Scope

- Public frontend email field; task `041f` owns that change.
- Payment or reservation confirmation through the public site.
- Firehose/S3 archival.
- Production AWS activation with real guest data.

## Acceptance Criteria

- A committed public request remains `UNCONFIRMED` and creates exactly two
  notifier intents.
- Rejected or rolled-back public requests create no intents.
- Duplicate handling produces one intent per idempotency key.
- Notifier persistence contains no booking foreign key or consumer model.
- SES or feedback failure does not roll back or mutate the reservation.
- Email wording describes a received request and later WhatsApp follow-up.
- Architecture tests prove consumer output-adapter and notifier input-port
  boundaries.
- Focused tests, full Maven suite and `git diff --check` pass.

## Required Report

`SDD/ImplementationReport/2026-08-21-042b-integrate-public-booking-with-notifier.md`
