# Implementation Report — Task 042b

## Task And Implementation File

- Task: `042b — Integrate Public Booking With Notifier`.
- Completed task file:
  `SDD/tasks/backendSpecs/042b-DONE-integrate-public-booking-with-notifier.md`.
- Implementation controls: `SDD/implementation/task-bootstrap.md` and
  `SDD/implementation/implementation-order.md`.
- Execution date: 2026-08-21.

## Specs, Prerequisites And Plans Read

- `SDD/specs/sddSpec.md`.
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`.
- `SDD/specs/publicBookingDataMinimizationSpec.md`.
- `SDD/specs/publicBookingOwnerEmailNotificationSpec.md`.
- `SDD/specs/publicBookingNotificationSpec.md`.
- `SDD/specs/backendSpecs/notifierModuleSpec.md`.
- `SDD/specs/moduleArchitectureSpec.md`.
- `SDD/specs/operationalLoggingSpec.md`.
- `SDD/specs/lgpdGovernanceSpec.md`.
- `SDD/specs/backendSpecs/supplierManagementSpec.md`.
- `SDD/plans/backendSpecs/publicBookingNotificationBackendPlan.md`.
- `SDD/plans/backendSpecs/notifierModuleBackendPlan.md`.
- Completed notifier tasks `037b` through `041b`.

## Files Created

- `src/main/java/com/househost/publicapi/application/port/out/PublicBookingNotificationPort.java`.
- `src/main/java/com/househost/publicapi/application/records/PublicBookingNotificationRecord.java`.
- `src/main/java/com/househost/publicapi/application/service/PublicBookingNotificationResolver.java`.
- `src/main/java/com/househost/publicapi/adapter/out/integration/PublicBookingNotificationProperties.java`.
- `src/main/java/com/househost/publicapi/adapter/out/integration/NotifierPublicBookingAdapter.java`.
- `src/test/java/com/househost/publicapi/adapter/out/integration/NotifierPublicBookingAdapterTest.java`.
- `src/test/java/com/househost/publicapi/adapter/out/integration/PublicBookingNotificationPropertiesTest.java`.
- `src/test/java/com/househost/publicapi/adapter/out/integration/PublicBookingNotificationTransactionIntegrationTest.java`.
- `src/test/java/com/househost/publicapi/architecture/PublicBookingNotificationArchitectureTest.java`.
- `SDD/ImplementationReport/2026-08-21-042b-integrate-public-booking-with-notifier.md`.

## Files Changed

- `src/main/java/com/househost/publicapi/application/dto/PublicBookingRequestDTO.java` —
  added the required transactional guest email.
- `src/main/java/com/househost/publicapi/application/service/PublicBookingService.java` —
  validates and normalizes email, persists the request as `UNCONFIRMED` and
  starts notification-intent creation inside the existing transaction.
- `src/main/java/com/househost/publicapi/application/service/PublicBookingGuestResolver.java` —
  persists the normalized transactional email on the guest.
- `src/main/java/com/househost/publicapi/application/service/PublicBookingParticipantNotifier.java` —
  centralizes the new notification resolver with the existing guest resolver.
- `src/main/resources/application.properties` — added disabled-by-default
  public notification, trusted management recipient and delivery profile
  settings.
- `src/test/java/com/househost/publicapi/application/service/PublicBookingServiceTest.java` —
  updated the public contract, normalization, persistence and notifier flow
  coverage.
- `SDD/implementation/task-bootstrap.md` and
  `SDD/implementation/implementation-order.md` — recorded completion while
  preserving separate frontend and production activation authorization.
- `SDD/tasks/frontendSpecs/041f-DONE-adapt-public-booking-email-contract.md` —
  subsequently completed the public frontend integration and retains the
  completed backend prerequisite filename.
- Historical reports for tasks `037b` through `041b` — updated references to
  the renamed completed task file.
- `SDD/tasks/backendSpecs/042b-DONE-integrate-public-booking-with-notifier.md` —
  marked the fully verified task complete.

## Flows Implemented

- The public request requires a lowercase, trimmed, bounded valid email and
  persists it in the existing guest contact field.
- A successful public request remains `UNCONFIRMED`; WhatsApp confirmation and
  payment negotiation remain later manual operations.
- The public participant notifier delegates the external effect to one local
  resolver and consumer-owned output port.
- The consumer adapter renders immutable text and HTML snapshots and calls only
  `NotificationRequestUseCase`, creating guest and management intents with one
  shared external event and two stable independent idempotency keys.
- The guest message says the request was received and is not confirmed. The
  management message carries only approved operational follow-up fields and no
  notes, policy content, documents, payment credentials or domain object.
- Intent creation joins the booking transaction. A second-intent failure rolls
  back the first intent, booking and guest. Provider dispatch remains later and
  outside that transaction.
- SES unavailability changes only notifier retry state. SNS feedback remains
  notifier-owned and has no path back to booking or guest state.

## Technical And MVP Decisions

- `PUBLIC_BOOKING_REQUEST:<bookingId>` is the stable public event identifier.
  The two idempotency keys append their notification type, preventing the two
  recipients from blocking one another.
- `CL-<bookingId>` is the opaque correlation key and visible booking code. The
  notifier stores it without interpreting or foreign-keying it.
- Public-booking notification intent creation has its own disabled-by-default
  feature flag. When enabled, startup requires a valid externally configured
  management recipient. SES dispatch and SNS ingress keep their independent
  existing flags.
- The default delivery profile key is `HOUSEHOST_TRANSACTIONAL`; the public
  HTTP request cannot override source, recipient, sender or profile.
- Email rendering is owned by the consumer adapter because wording and allowed
  booking snapshot fields are consumer business concerns.

## Difficulties, Problems And Resolutions

- The existing public contract intentionally excluded email. It was extended
  only with the transactional field authorized by the governing specs and no
  sender, management recipient or provider properties.
- The public module already enforced one `ParticipantNotifier`. The integration
  was added through a specialized resolver instead of injecting the notifier
  use case into the principal service.
- Atomicity cannot be established by mock-only tests. A database integration
  test now runs the public service inside a real transaction and proves full
  rollback after the first intent is inserted and the second request fails.
- The worktree contained extensive unrelated existing changes. They were
  preserved and excluded from this task.

## Tests And Verification

- Focused public contract and service tests passed for email normalization,
  validation, guest persistence and unchanged public response semantics.
- Adapter tests passed for two recipients, shared event ID, independent stable
  keys, trusted management recipient, minimized content and disabled behavior.
- Transaction integration tests passed for exactly two committed intents, no
  intent after rejection, full rollback on second-intent failure, duplicate
  event handling and SES-unavailability isolation.
- Architecture tests passed for `ParticipantNotifier → Resolver → output port →
  notifier input port`, absence of AWS/persistence imports in public API and
  absence of consumer callbacks in notifier.
- Existing notifier persistence, dispatch, SES and SNS suites passed as part of
  the full regression.
- `./mvnw -q test` — passed: 493 tests, 0 failures, 0 errors, 0 skipped.
- `git diff --check` — passed for the task scope.
- No real SES send, SNS subscription, AWS provisioning or production data test
  was run because production activation is explicitly out of scope.

## Acceptance And Prerequisite Review

- Enabled successful public requests commit one `UNCONFIRMED` booking and
  exactly two notifier intents.
- Rejected and rolled-back requests leave no intent; duplicate event handling
  retains one intent per source-scoped idempotency key.
- Notifier persistence remains consumer-neutral and has no booking foreign key.
- Provider handoff failure changes notifier state to `RETRYABLE_FAILURE` while
  booking stays `UNCONFIRMED`; feedback remains isolated by architecture and
  the completed `041b` verification.
- Message wording and snapshots satisfy the request-received, WhatsApp follow-up,
  payment-negotiation and data-minimization rules.
- The public principal service retains one `ParticipantNotifier`, and no AWS,
  notifier persistence or feedback type crosses into its application core.
- Frontend task `041f` was subsequently completed. AWS infrastructure
  configuration and production flags remain separately controlled and were not
  executed by task `042b`.
- No authoritative prerequisite conflict remains. Task `042b` is complete.
