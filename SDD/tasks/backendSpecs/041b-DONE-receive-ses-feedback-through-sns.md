# Task 041b DONE — Receive SES Feedback Through SNS

## Status

Complete after explicit implementation approval, verification and report on
2026-08-21.

## Implementation Area

Backend (`b`).

## Objective

Receive authenticated SNS messages directly in the notifier, normalize nested
SES delivery feedback, persist provider events idempotently and update only
notifier-owned delivery state.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/notifierModuleSpec.md`
- `SDD/specs/publicBookingNotificationSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/operationalLoggingSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/notifierModuleBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/tasks/backendSpecs/040b-DONE-integrate-aws-ses-outbound-adapter.md`

## Scope

- Create the dedicated notifier SNS HTTPS inbound adapter and security route.
- Enforce content type, request-size limit and exact configured topic/Region.
- Validate SNS signature version, certificate URL, certificate chain and signed
  fields before parsing the SES event.
- Support controlled subscription confirmation and ordinary notifications.
- Parse nested delivery, bounce, complaint, reject, rendering-failure and
  delivery-delay events.
- Correlate only through SES provider message identifier.
- Normalize provider data into `NotificationFeedbackRecord`.
- Persist one append-only provider event and apply deterministic intent state.
- Make SNS redelivery idempotent and handle unknown provider IDs safely.
- Add malformed, spoofed, wrong-topic, duplicate, out-of-order and privacy
  tests.

## Out Of Scope

- Firehose or S3 archival.
- Public booking status mutation.
- Automatic resend after permanent bounce or complaint.
- AWS SNS resource provisioning or production subscription activation.

## Acceptance Criteria

- Unsigned, invalid-signature, untrusted-certificate and unexpected-topic
  messages are rejected before application processing.
- Valid SNS messages parse the nested SES event and locate the intent by
  `providerMessageId`.
- Delivery, permanent bounce and complaint produce deterministic current state.
- Duplicate SNS delivery produces one provider event and one transition.
- No consumer repository is queried and no consumer state is changed.
- Complete envelopes, recipient addresses and raw payloads are absent from
  ordinary logs and provider-event persistence.
- Focused security, feedback, idempotency and integration tests pass with
  `git diff --check`.

## Required Report

Create after implementation:

`SDD/ImplementationReport/YYYY-MM-DD-041b-receive-ses-feedback-through-sns.md`
