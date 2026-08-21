# Implementation Report — Task 037b

## Task And Implementation File

- Task: `037b — Create Provider-Neutral Notifier Contracts And Domain`.
- Completed task file:
  `SDD/tasks/backendSpecs/037b-DONE-create-notifier-contracts-and-domain.md`.
- Implementation controls: `SDD/implementation/task-bootstrap.md` and
  `SDD/implementation/implementation-order.md`.
- Execution date: 2026-08-20.

## Specs, Prerequisites And Plans Read

- `SDD/specs/sddSpec.md`.
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`.
- `SDD/specs/backendSpecs/notifierModuleSpec.md`.
- `SDD/specs/publicBookingNotificationSpec.md`.
- `SDD/specs/publicBookingDataMinimizationSpec.md`.
- `SDD/specs/publicBookingOwnerEmailNotificationSpec.md`.
- `SDD/specs/moduleArchitectureSpec.md`.
- `SDD/specs/operationalLoggingSpec.md`.
- `SDD/specs/lgpdGovernanceSpec.md`.
- `SDD/specs/backendSpecs/supplierManagementSpec.md`.
- `SDD/plans/backendSpecs/notifierModuleBackendPlan.md`.
- `SDD/plans/backendSpecs/publicBookingNotificationBackendPlan.md`.

## Files Created

- `src/main/java/com/househost/notifier/domain/exception/NotificationDomainException.java`.
- `src/main/java/com/househost/notifier/domain/model/NotificationChannel.java`.
- `src/main/java/com/househost/notifier/domain/model/NotificationStatus.java`.
- `src/main/java/com/househost/notifier/domain/model/NotificationEventType.java`.
- `src/main/java/com/househost/notifier/domain/model/NotificationFailureCategory.java`.
- `src/main/java/com/househost/notifier/domain/model/EmailDeliveryOutcome.java`.
- `src/main/java/com/househost/notifier/domain/model/NotificationIntent.java`.
- `src/main/java/com/househost/notifier/domain/model/NotificationProviderEvent.java`.
- `src/main/java/com/househost/notifier/application/records/NotificationRecordValidation.java`.
- `src/main/java/com/househost/notifier/application/records/EmailMessageRecord.java`.
- `src/main/java/com/househost/notifier/application/records/NotificationRequestRecord.java`.
- `src/main/java/com/househost/notifier/application/records/EmailDeliveryResultRecord.java`.
- `src/main/java/com/househost/notifier/application/records/NotificationFeedbackRecord.java`.
- `src/main/java/com/househost/notifier/application/records/NotificationClaimRecord.java`.
- `src/main/java/com/househost/notifier/application/records/NotificationRetryDecisionRecord.java`.
- `src/main/java/com/househost/notifier/application/port/in/NotificationRequestUseCase.java`.
- `src/main/java/com/househost/notifier/application/port/in/NotificationFeedbackUseCase.java`.
- `src/main/java/com/househost/notifier/application/port/out/NotificationIntentPersistencePort.java`.
- `src/main/java/com/househost/notifier/application/port/out/NotificationProviderEventPersistencePort.java`.
- `src/main/java/com/househost/notifier/application/port/out/EmailDeliveryPort.java`.
- `src/main/java/com/househost/notifier/application/service/NotificationIntentService.java`.
- `src/test/java/com/househost/notifier/domain/model/NotificationIntentTest.java`.
- `src/test/java/com/househost/notifier/domain/model/NotificationProviderEventTest.java`.
- `src/test/java/com/househost/notifier/application/records/NotificationRecordsTest.java`.
- `src/test/java/com/househost/notifier/application/service/NotificationIntentServiceTest.java`.
- `src/test/java/com/househost/notifier/architecture/NotifierCoreArchitectureTest.java`.
- `SDD/ImplementationReport/2026-08-20-037b-create-notifier-contracts-and-domain.md`.

## Files Changed

- `SDD/specs/lgpdGovernanceSpec.md` — aligned the public-collection boundary
  with the already approved transactional reservation-email purpose.
- `SDD/tasks/backendSpecs/037b-DONE-create-notifier-contracts-and-domain.md` —
  marked the verified task complete.
- `SDD/tasks/backendSpecs/038b-DONE-persist-notifier-intents-and-events.md` — updated
  the prerequisite reference after the task rename.
- `SDD/tasks/backendSpecs/042b-DONE-integrate-public-booking-with-notifier.md` —
  updated the prerequisite reference after the task rename.
- `SDD/implementation/task-bootstrap.md` — recorded completion of `037b` and
  retained `038b` through `042b` as proposed work.
- `SDD/implementation/implementation-order.md` — updated the ordered task path
  and completion note.

## Flows Implemented

- Created one provider-neutral inbound request contract per recipient and a
  normalized feedback contract with no provider JSON or consumer-domain type.
- Created a self-contained immutable message snapshot and a notifier-owned
  intent with source-scoped idempotency, retention and durable retry fields.
- Implemented domain transitions for claim, expired-lease recovery, retryable
  failure, exhaustion, provider acceptance, delivery, bounce and complaint.
- Preserved `EXHAUSTED`, `BOUNCED` and `COMPLAINT` as terminal states while
  allowing a delivered message to receive a later complaint.
- Created an append-only provider-event domain model associated only with a
  notifier intent.
- Declared persistence, delivery, request and feedback ports without Spring,
  JPA, AWS SDK, HTTP or consumer-domain dependencies.
- Implemented source-scoped idempotent intent creation with a server-controlled
  retention duration and a race-safe `createIfAbsent` persistence contract.

## Technical And MVP Decisions

- IDs are notifier-owned UUIDs, while `externalEventId`, `idempotencyKey` and
  optional `correlationKey` remain opaque textual values.
- Stable textual identifiers accept letters, digits, underscore, dot and
  hyphen; notification purpose remains extensible and is not a HouseHost enum.
- One intent stores exactly one email recipient and one final text/HTML
  snapshot. Body whitespace is preserved as rendered content; headers are
  trimmed and reject line breaks.
- The provider-neutral delivery outcome distinguishes accepted, retryable and
  permanent failures without importing SES exception types.
- Retry timing policy calculation and jitter remain for `039b`; `037b` defines
  only the state and decision contracts required by that task.
- Feedback orchestration remains for `041b`; `037b` defines its normalized
  inbound contract and append-only domain model only.

## Difficulties, Problems And Resolutions

- The LGPD governance prerequisite still described email as disallowed while
  no operational purpose existed. The newer notification and minimization
  specs establish that purpose, so the prerequisite was corrected before code
  implementation to permit only the required transactional reservation email
  and continue forbidding marketing or unrelated use.
- The worktree contained extensive unrelated existing changes. They were
  preserved; implementation was confined to the notifier paths and the SDD
  files listed in this report.

## Tests And Verification

- `./mvnw -q -Dtest='com.househost.notifier.**' test` — passed, 21 focused
  tests across five test classes.
- `./mvnw -q test` — passed after notifier integration with the complete Maven
  suite.
- `git diff --check` — passed before SDD completion and is repeated after the
  final documentation updates.

## Prerequisite Review

- The notifier core contains no booking, guest, payment, reservation or public
  API identifier, model, repository or foreign-key concept.
- Domain and application sources contain no Spring, JPA, AWS SDK, Jackson,
  adapter or transport dependency.
- Message and recipient data are bounded, excluded from logging code and
  retained behind a server-controlled boundary, conforming to LGPD and
  operational-logging minimization.
- The application records, ports and service follow the required architecture,
  `Record`, collection and `Optional` identifier conventions.
- No database schema, scheduler, AWS adapter, SNS endpoint or public-booking
  integration was introduced.
- Every acceptance criterion, governing plan and implementation control was
  reviewed after verification. No unresolved contradiction remains.
