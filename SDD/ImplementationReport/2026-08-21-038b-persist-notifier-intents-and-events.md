# Implementation Report — Task 038b

## Task And Implementation File

- Task: `038b — Persist Notifier Intents And Provider Events`.
- Completed task file:
  `SDD/tasks/backendSpecs/038b-DONE-persist-notifier-intents-and-events.md`.
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
- `SDD/tasks/backendSpecs/037b-DONE-create-notifier-contracts-and-domain.md`.
- `SDD/ImplementationReport/2026-08-20-037b-create-notifier-contracts-and-domain.md`.

## Files Created

- `src/main/java/com/househost/notifier/adapter/out/persistence/entity/NotificationIntentJpaEntity.java`.
- `src/main/java/com/househost/notifier/adapter/out/persistence/entity/NotificationProviderEventJpaEntity.java`.
- `src/main/java/com/househost/notifier/adapter/out/persistence/NotificationIntentJpaRepository.java`.
- `src/main/java/com/househost/notifier/adapter/out/persistence/NotificationProviderEventJpaRepository.java`.
- `src/main/java/com/househost/notifier/adapter/out/persistence/NotificationIntentPersistenceMapper.java`.
- `src/main/java/com/househost/notifier/adapter/out/persistence/NotificationProviderEventPersistenceMapper.java`.
- `src/main/java/com/househost/notifier/adapter/out/persistence/NotificationIntentPersistenceAdapter.java`.
- `src/main/java/com/househost/notifier/adapter/out/persistence/NotificationProviderEventPersistenceAdapter.java`.
- `src/test/java/com/househost/notifier/adapter/out/persistence/NotificationPersistenceIntegrationTest.java`.
- `src/test/java/com/househost/config/DatabaseSchemaCompatibilityRunnerNotifierTest.java`.
- `SDD/ImplementationReport/2026-08-21-038b-persist-notifier-intents-and-events.md`.

## Files Changed

- `src/main/java/com/househost/notifier/domain/model/NotificationIntent.java` —
  added persistence version restoration and the explicit representation of
  content anonymized after retention.
- `src/main/java/com/househost/notifier/application/port/out/NotificationIntentPersistencePort.java` —
  added the notifier-owned content-anonymization persistence operation.
- `src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java` —
  added idempotent MySQL schema compatibility for notifier tables,
  constraints, internal foreign key and query indexes.
- `src/test/java/com/househost/notifier/domain/model/NotificationIntentTest.java` —
  aligned restored-domain construction with optimistic persistence version.
- `src/test/java/com/househost/notifier/application/service/NotificationIntentServiceTest.java` —
  aligned the in-memory persistence port with the anonymization contract.
- `src/test/java/com/househost/notifier/architecture/NotifierCoreArchitectureTest.java` —
  kept framework bans on domain/application while allowing adapters to use
  Spring and JPA; consumer-identifier checks still cover the complete module.
- `SDD/tasks/backendSpecs/038b-DONE-persist-notifier-intents-and-events.md` —
  marked the verified task complete.
- `SDD/tasks/backendSpecs/039b-DONE-implement-notifier-dispatch-and-retry.md` —
  updated its completed prerequisite filename.
- `SDD/tasks/backendSpecs/042b-DONE-integrate-public-booking-with-notifier.md` —
  updated its completed prerequisite filename.
- `SDD/ImplementationReport/2026-08-20-037b-create-notifier-contracts-and-domain.md` —
  updated the historical reference to the renamed task file.
- `SDD/implementation/task-bootstrap.md` — recorded completion of `038b` and
  retained `039b` through `042b` as separately authorized work.
- `SDD/implementation/implementation-order.md` — updated the ordered task path
  and completion note.

## Flows Implemented

- Persisted complete, provider-neutral notification snapshots with notifier-
  owned UUIDs and no consumer foreign key.
- Made source-scoped request creation atomic and idempotent through the unique
  `(source_system, idempotency_key)` boundary.
- Added bounded eligible selection under pessimistic write lock, atomic claim,
  durable attempt count, lease and next-attempt state, including expired-lease
  recovery.
- Added optimistic versioning for subsequent state updates and unique optional
  provider-message correlation.
- Persisted normalized provider events append-only and idempotently by SNS
  transport identifier or optional provider event identifier.
- Added bounded retention selection and bulk erasure of recipient, subject,
  rendered bodies and correlation key while preserving operational evidence.

## Technical And MVP Decisions

- Production MySQL creation uses `ON DUPLICATE KEY UPDATE id = id`, so a
  duplicate intent or provider event remains successful inside the caller's
  transaction without poisoning it. H2 integration tests use a caught unique-
  constraint violation because H2 does not support that MySQL syntax.
- Claim selection and state transition execute in the same short database
  transaction. No network call was added to this transaction or this task.
- `notification_provider_events` references only `notification_intents`.
  Consumer identifiers remain opaque text inside self-contained intent fields.
- No complete SNS envelope or raw SES payload is stored. The optional raw event
  field is only an external storage key.
- Anonymized rows remain restorable as domain state but are excluded from both
  delivery claims and future retention selection.

## Difficulties, Problems And Resolutions

- H2 rejected MySQL `ON DUPLICATE KEY` syntax. Database-product selection kept
  the production atomic upsert and supplied a test-compatible equivalent whose
  concurrent idempotency behavior is independently verified.
- The `037b` architecture test scanned future adapter packages as if they were
  core. Its scope was corrected to domain/application for framework bans while
  preserving module-wide consumer coupling checks.
- The worktree contained extensive unrelated existing changes. They were
  preserved; implementation remained confined to notifier persistence and the
  SDD files listed here.
- Repository-wide `git diff --check` reported five pre-existing blank-line-at-
  EOF findings in unrelated staged documentation. The task-scoped diff check
  passed with no finding.

## Tests And Verification

- Focused notifier/domain/schema tests — passed, including concurrent request
  idempotency, concurrent claims, lease recovery, event deduplication,
  retention anonymization and schema constraints/indexes.
- `./mvnw -q test` — passed: 428 tests, 0 failures, 0 errors, 0 skipped.
- `./mvnw -q -DskipTests compile` — passed.
- Task-scoped whitespace verification — passed.
- Repository-wide `git diff --check` — only the five unrelated pre-existing
  documentation findings described above.

## Acceptance And Prerequisite Review

- Both notifier tables are owned by the notifier; their sole foreign key is
  provider event to notification intent.
- Duplicate source request keys and provider events persist exactly one row.
- Concurrent workers do not claim the same eligible intent.
- Expired processing leases are reclaimed on the same intent with an
  incremented attempt count.
- Mapping, normalized storage, provider-message uniqueness, optimistic version,
  bounded queries, indexes, retention selection and anonymization are covered.
- Scheduler, SES calls, SNS HTTP ingress and public-booking integration were
  not implemented.
- No authoritative prerequisite conflict remained. Task `039b` is the next
  proposed backend task and still requires explicit user authorization.
