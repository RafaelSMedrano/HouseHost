# Task 041f DONE — Adapt Public Booking Form To Email Contract

## Status

Completed and verified on 2026-08-21 after explicit execution approval and
completion of backend task `042b`.

## Implementation Area

Frontend (`f`).

## Objective

Adapt the public reservation form to collect the guest's transactional email
required by the public booking notification contract, while preserving the
request-first flow in which the reservation remains `UNCONFIRMED` and final
confirmation and payment are handled through WhatsApp.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/publicBookingDataMinimizationSpec.md`
- `SDD/specs/publicBookingNotificationSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`

## Required Plans

- `SDD/plans/frontendSpecs/publicBookingDataMinimizationFrontendPlan.md`
- `SDD/plans/backendSpecs/publicBookingNotificationBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/tasks/backendSpecs/042b-DONE-integrate-public-booking-with-notifier.md`
- `frontend/public/cantinhoDasLavandas/js/views/reservaView.js`
- `frontend/public/cantinhoDasLavandas/js/api.js`
- `src/main/java/com/househost/publicapi/application/dto/PublicBookingRequestDTO.java`
- `src/main/java/com/househost/publicapi/application/service/PublicBookingService.java`

## Dependencies

- Backend task `042b` completed and its public contract verified before
  frontend integration.
- Public booking backend endpoint accepts the transactional guest email.
- The public flow continues to create a reservation request with status
  `UNCONFIRMED`.

## Scope

- Add a required, accessible email field to the public personal-data step.
- Apply immediate bounded format validation and normalize the value before
  serializing the request.
- Include `guest.email` in the payload sent by `createPublicBooking` without
  exposing management recipients, sender configuration or SES details.
- Update privacy and consent text to explain transactional reservation
  communication without creating marketing consent.
- Preserve the existing WhatsApp explanation, request-received confirmation
  screen and `UNCONFIRMED` semantics.
- Add or update frontend tests for validation, payload shape, loading,
  successful request submission and recoverable error behavior.

## Out Of Scope

- AWS SES integration.
- Email templates or delivery retry.
- Payment selection or reservation confirmation through the website.
- Administrative frontend changes.
- Collecting payment data, documents or marketing consent.

## Acceptance Criteria

- The public form visibly and accessibly collects a required email address.
- Invalid, blank or oversized email values prevent submission with clear
  feedback.
- The request payload contains the normalized `guest.email` field and no AWS,
  SES, management-recipient or sender fields.
- The success screen continues to describe a received request, WhatsApp
  follow-up and later confirmation rather than confirmed accommodation.
- Privacy text distinguishes transactional email from marketing communication.
- Existing room, date, guest-composition, privacy-policy and notes behavior is
  preserved.
- Focused frontend tests, the full frontend suite, syntax checks and
  `git diff --check` pass.

## Required Report

Create after implementation:

`SDD/ImplementationReport/YYYY-MM-DD-041f-adapt-public-booking-email-contract.md`
