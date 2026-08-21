# Implementation Report — Task 039b

## Task And Implementation File

- Task: `039b — Implement Notifier Dispatch And Retry`.
- Completed task file:
  `SDD/tasks/backendSpecs/039b-DONE-implement-notifier-dispatch-and-retry.md`.
- Implementation controls: `SDD/implementation/task-bootstrap.md` and
  `SDD/implementation/implementation-order.md`.
- Execution date: 2026-08-21.

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
- `SDD/tasks/backendSpecs/038b-DONE-persist-notifier-intents-and-events.md`.
- `SDD/ImplementationReport/2026-08-21-038b-persist-notifier-intents-and-events.md`.

## Files Created

- `src/main/java/com/househost/notifier/application/port/in/NotificationDispatchUseCase.java`.
- `src/main/java/com/househost/notifier/application/port/out/NotificationOperationalEventPort.java`.
- `src/main/java/com/househost/notifier/application/service/NotificationDispatchService.java`.
- `src/main/java/com/househost/notifier/application/service/NotificationRetryPolicy.java`.
- `src/main/java/com/househost/notifier/adapter/in/scheduling/NotificationDispatchScheduler.java`.
- `src/main/java/com/househost/notifier/adapter/in/scheduling/NotifierApplicationConfiguration.java`.
- `src/main/java/com/househost/notifier/adapter/in/scheduling/NotifierDispatchProperties.java`.
- `src/main/java/com/househost/notifier/adapter/out/integration/Slf4jNotificationOperationalEventAdapter.java`.
- `src/test/java/com/househost/notifier/application/service/NotificationRetryPolicyTest.java`.
- `src/test/java/com/househost/notifier/adapter/out/persistence/NotificationDispatchPersistenceIntegrationTest.java`.
- `src/test/java/com/househost/notifier/adapter/in/scheduling/NotificationDispatchSchedulerIntegrationTest.java`.
- `src/test/java/com/househost/notifier/adapter/in/scheduling/NotifierDisabledDispatchIntegrationTest.java`.
- `SDD/ImplementationReport/2026-08-21-039b-implement-notifier-dispatch-and-retry.md`.

## Files Changed

- `src/main/java/com/househost/notifier/application/port/out/NotificationIntentPersistencePort.java` —
  added intent lookup by notifier-owned identifier for short outcome and
  reprocessing transactions.
- `src/main/java/com/househost/notifier/adapter/out/persistence/NotificationIntentPersistenceAdapter.java` —
  implemented transactional intent lookup used after provider execution.
- `src/main/java/com/househost/notifier/domain/model/NotificationIntent.java` —
  added the explicit exhausted-to-pending requeue transition.
- `src/main/resources/application.properties` — externalized dispatch enablement,
  scheduler delays, batch, lease, attempt, backoff, jitter and retention
  settings; dispatch remains disabled by default until a provider exists.
- `src/test/java/com/househost/notifier/domain/model/NotificationIntentTest.java` —
  covered exhausted-intent requeue and state reset.
- `src/test/java/com/househost/notifier/application/service/NotificationIntentServiceTest.java` —
  aligned the in-memory persistence fake with identifier lookup.
- `src/test/java/com/househost/notifier/architecture/NotifierCoreArchitectureTest.java` —
  added a source-level privacy guard for operational logging.
- `SDD/tasks/backendSpecs/039b-DONE-implement-notifier-dispatch-and-retry.md` —
  marked the fully verified task complete.
- `SDD/tasks/backendSpecs/040b-DONE-integrate-aws-ses-outbound-adapter.md` — updated
  its completed prerequisite filename.
- `SDD/tasks/backendSpecs/042b-DONE-integrate-public-booking-with-notifier.md` —
  updated its completed prerequisite filename.
- `SDD/ImplementationReport/2026-08-21-038b-persist-notifier-intents-and-events.md` —
  updated the historical reference to the renamed task file.
- `SDD/implementation/task-bootstrap.md` — recorded completion of `039b` and
  retained `040b` through `042b` as separately authorized work.
- `SDD/implementation/implementation-order.md` — updated the ordered task path
  and completion note.

## Flows Implemented

- Registered `NotificationRequestUseCase` independently from provider
  activation, preserving intent creation while dispatch is disabled.
- Claimed bounded due batches in short persistence transactions and invoked the
  delivery port only after the claim transaction completed.
- Re-read claim ownership before persisting an outcome, so stale provider
  results cannot overwrite a lease already recovered by another worker.
- Persisted accepted, permanent-failure, retryable and exhausted outcomes.
- Calculated exponential retry delay from the durable attempt count, applied
  bounded random jitter and persisted the resulting `nextAttemptAt`.
- Reclaimed expired processing leases after restart through the existing
  persistence claim boundary.
- Exposed explicit exhausted-intent reprocessing that resets delivery state and
  makes the intent immediately eligible again.
- Scheduled automatic dispatch with Spring `@Scheduled`, external initial and
  fixed delays, and no overlap of one scheduler method's fixed-delay cycles.
- Emitted best-effort operational events containing identifiers, type, status,
  attempt and timing only; recipient, subject and body are excluded.

## Technical And MVP Decisions

- The scheduler, retry policy and application service remain provider-neutral.
  No AWS dependency or SES/SNS behavior was introduced.
- Dispatch defaults to disabled. Task `040b` must supply the SES adapter and
  production profile before activation.
- The delivery call has at-least-once semantics. A process loss after provider
  acceptance but before local outcome persistence can lead to another attempt
  after lease expiry; provider feedback correlation remains a later task.
- Jitter does not create a thread or delay the scheduler. It changes only the
  persisted next eligible instant inspected by later scheduler cycles.
- Operational logging is best effort and cannot turn a provider outcome into a
  delivery failure.

## Difficulties, Problems And Resolutions

- Full application context tests executed unrelated privacy and audit startup
  runners that require production-like data and MySQL syntax. Scheduler tests
  were narrowed to the JPA slice plus explicit notifier configuration, keeping
  them deterministic and focused.
- Fast scheduler polling initially emitted one log entry for every empty cycle.
  Empty batches are now silent while claimed batches and outcomes remain
  observable.
- The worktree contained extensive unrelated existing changes. They were
  preserved; implementation remained confined to notifier dispatch and the SDD
  files listed here.

## Tests And Verification

- Notifier-focused domain, retry, persistence, scheduler, disabled-mode and
  architecture tests — passed.
- Restart simulation verified that persisted retry timing is honored by a new
  dispatcher instance.
- Provider transaction assertion verified delivery executes without an active
  claim transaction.
- Fake-provider scheduler test verified automatic delivery and maximum
  concurrency of one for fixed-delay cycles.
- `./mvnw -q -DskipTests compile` — passed.
- `./mvnw -q test` — passed: 438 tests, 0 failures, 0 errors, 0 skipped.
- `git diff --check` — passed.

## Acceptance And Prerequisite Review

- Due intents are scheduled and claimed in bounded batches.
- Attempt and timing state survive dispatcher reconstruction and expired leases
  are reclaimable after restart.
- Retryable failures become durable future attempts; permanent or attempt-limit
  failures become visible `EXHAUSTED` state.
- Exhausted intents can be explicitly requeued without changing their stable
  identity or idempotency key.
- Disabled dispatch preserves pending intents and request use-case operation.
- Provider execution does not occur inside the claim transaction.
- Logs exclude destination and message content by port design and architecture
  test.
- AWS SES, SNS ingress and public-booking consumer integration were not
  implemented.
- No authoritative prerequisite conflict remained. Task `040b` is the next
  proposed backend task and still requires explicit user authorization.
