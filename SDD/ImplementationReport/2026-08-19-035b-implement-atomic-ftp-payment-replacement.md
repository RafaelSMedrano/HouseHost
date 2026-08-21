# Implementation Report — Task 035b

## Task And Authorization

- Task: `035b`.
- Executed task file:
  `SDD/tasks/backendSpecs/035b-DONE-implement-atomic-ftp-payment-replacement.md`.
- Execution date: 2026-08-19.
- Authorization: explicit user instruction to execute task `035b`.

## Specs, Prerequisites And Plans Read

- `SDD/specs/sddSpec.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/plans/backendSpecs/financialTransactionPlanBackendPlan.md`
- `SDD/tasks/backendSpecs/030b-DONE-notify-financial-source-deletion.md`
- `SDD/tasks/backendSpecs/032b-DONE-correct-cashier-schedule-semantics.md`
- `SDD/tasks/backendSpecs/034b-DONE-build-ftp-use-cases-and-reservation-creation.md`

The prerequisite chain reaches the degree-zero project spec through module
architecture and LGPD governance. No undeclared prerequisite was required.

## Result

The backend now replaces one eligible provisional check-in or checkout payment
with a definitive simple transaction or installment block in one transaction.
The command locks the FTP, validates its authoritative member and lifecycle,
removes every old participant effect and physical row, creates a new identifier,
rebuilds sources and participant effects and returns the recalculated FTP.

Replacement commands are scoped by authenticated actor and idempotency key.
Completed repetitions replay the stored authoritative outcome; concurrent
attempts serialize through the FTP lock and create exactly one definitive
transaction. Realized Cashier balances remain unchanged because replacement
only reverses and recreates scheduled projections.

Only definitive creation is submitted to auditing, through an after-commit
boundary. The event does not expose the provisional identifier or a replacement
relationship. No creation or deletion audit is emitted for the provisional
transaction.

## Files Created

- `src/main/java/com/househost/finance/financialtransaction/application/dto/FinancialTransactionPlanReplacementRequestDTO.java`
  defines the client-controlled structure, method, quantity, candidate and key.
- `src/main/java/com/househost/finance/financialtransaction/application/dto/FinancialTransactionPlanReplacementOutcomeDTO.java`
  returns the definitive component, recalculated FTP and replay indicator.
- `src/main/java/com/househost/finance/financialtransaction/application/records/FinancialTransactionPlanReplacementCommandRecord.java`
  carries the authenticated, trusted application command.
- `src/main/java/com/househost/finance/financialtransaction/application/port/in/FinancialTransactionPlanReplacementUseCase.java`
  declares replacement and authoritative reconciliation operations.
- `src/main/java/com/househost/finance/financialtransaction/application/port/out/FinancialPostCommitAuditPort.java`
  defines the post-commit definitive creation audit boundary.
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/integration/FinancialPostCommitAuditAdapter.java`
  registers definitive creation auditing with transaction synchronization.
- `src/main/java/com/househost/finance/financialtransaction/application/service/FinancialTransactionPlanReplacementService.java`
  implements locking, validation, replacement, replay and rollback semantics.
- `src/test/java/com/househost/finance/financialtransaction/application/service/FinancialTransactionPlanReplacementServiceTest.java`
  verifies application behavior and deterministic failures.
- `src/test/java/com/househost/finance/financialtransaction/adapter/out/persistence/FinancialTransactionPlanReplacementIntegrationTest.java`
  proves database rollback and concurrent same-key uniqueness.
- `src/test/java/com/househost/finance/financialtransaction/adapter/out/integration/FinancialPostCommitAuditAdapterTest.java`
  proves after-commit dispatch and active-transaction enforcement.
- `SDD/ImplementationReport/2026-08-19-035b-implement-atomic-ftp-payment-replacement.md`
  records execution decisions, artifacts and verification evidence.

The task file was renamed from
`035b-implement-atomic-ftp-payment-replacement.md` to
`035b-DONE-implement-atomic-ftp-payment-replacement.md`; this is an SDD
completion rename rather than a new implementation artifact.

## Files Changed

- `src/main/java/com/househost/finance/financialtransaction/application/records/FinancialCommandIdempotencyRecord.java`
  stores and completes replacement commands with the definitive transaction ID.
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/entity/FinancialCommandIdempotencyJpaEntity.java`
  persists the additional replacement outcome identifier.
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/FinancialCommandIdempotencyPersistenceAdapter.java`
  maps the expanded idempotency record.
- `src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java`
  adds the replacement outcome column and index compatibility repeatably.
- `src/main/java/com/househost/finance/financialtransaction/application/service/FinancialTransactionPlanValidationService.java`
  validates command shape, candidate lifecycle and installment deadline.
- `src/main/java/com/househost/finance/financialtransaction/application/service/FinancialParticipantNotifier.java`
  supports source-only deletion after participant cleanup and persistence.
- `src/main/java/com/househost/finance/financialtransaction/adapter/in/rest/FinancialTransactionPlanController.java`
  exposes authenticated replacement and reconciliation endpoints.
- `src/main/java/com/househost/security/adapter/in/config/SecurityConfig.java`
  authorizes the new operational endpoints.
- `src/main/java/com/househost/finance/financialtransaction/application/service/FinancialTransactionPlanService.java`
  aligns reservation idempotency construction with the expanded record.
- `src/test/java/com/househost/finance/financialtransaction/application/service/FinancialTransactionPlanServiceTest.java`
  aligns reservation fixtures with the expanded idempotency record.
- `src/test/java/com/househost/finance/financialtransaction/adapter/out/persistence/FinancialCommandIdempotencyPersistenceIntegrationTest.java`
  verifies persisted replacement outcomes.
- `src/test/java/com/househost/finance/financialtransaction/adapter/in/rest/FinancialTransactionPlanAuthorizationTest.java`
  verifies allowed and denied replacement/reconciliation roles.
- `src/test/java/com/househost/config/DatabaseSchemaCompatibilityRunnerFinancialTransactionPlanTest.java`
  verifies repeatable compatibility for the new outcome column.
- `src/test/java/com/househost/finance/cashier/application/service/CashierMovementServiceTest.java`
  proves realized Cashier balances remain invariant during rescheduling.
- `src/test/java/com/househost/finance/financialtransaction/architecture/FinancialTransactionPlanArchitectureTest.java`
  requires the dedicated replacement service boundary.
- `src/test/java/com/househost/finance/financialtransaction/application/service/FinancialParticipantCommunicationArchitectureTest.java`
  keeps replacement communication behind the participant notifier.
- `SDD/tasks/backendSpecs/035b-DONE-implement-atomic-ftp-payment-replacement.md`
  records verified completion in its title and status.
- `SDD/tasks/backendSpecs/036b-DONE-integrate-ftp-checkin-checkout-and-security.md`
  points to the completed prerequisite filename.
- `SDD/implementation/implementation-order.md`
  records 035b completion and leaves the remaining sequence proposed.
- `SDD/implementation/task-bootstrap.md`
  updates the current task-set completion state.
- `SDD/ImplementationReport/2026-08-18-032b-correct-cashier-schedule-semantics.md`
  updates its forward reference after the task rename.
- `SDD/ImplementationReport/2026-08-19-034b-build-ftp-use-cases-and-reservation-creation.md`
  records the subsequent completion of task 035b.

## Flows Implemented

- A valid `WAITING` or `OVERDUE` provisional component is selected by submitted
  identifier and purpose under a pessimistic FTP lock.
- Party effects are reversed, the aggregate replaces the old member with a new
  simple or installment member, and persistence physically deletes the old row.
- PLAN/INSTALLMENT source callbacks and new participant effects are rebuilt in
  deterministic order inside the same transaction.
- Any pre-commit participant, persistence or audit-registration failure rolls
  back the complete replacement.
- Successful commit triggers only the definitive creation audit.
- Same-key repetition returns the stored outcome, while stale or competing
  requests return deterministic conflicts.

## Design Decisions

- Selecting a payment method does not settle the new transaction. The
  definitive component remains scheduled until the operational materialization
  owned by task `036b`.
- The definitive amount, type, purpose and source are derived from the locked
  FTP and cannot be overridden by client input.
- Installments accept quantities from 2 through 12 and must fit entirely within
  the FTP deadline.
- Audit delivery after commit cannot invalidate an already committed financial
  replacement; registration itself remains part of the transactional boundary.

## Difficulties And Resolutions

- Source deletion could recursively mutate the same single-component FTP. The
  flow therefore reverses parties first, persists the aggregate replacement and
  invokes the idempotent PLAN source-only detach callback after the old member
  is absent.
- Same-key races could pass an idempotency check simultaneously. The service
  locks the owning FTP before claiming or replaying the command, serializing
  competing replacements for that aggregate.
- Audit had to be absent on rollback without coupling the audit transaction to
  financial persistence. A transaction synchronization registers the definitive
  event and dispatches it exclusively from `afterCommit`.

## Verification Coverage

- Service tests cover simple and installment success, replay, stale candidates,
  invalid lifecycle/deadline, rollback boundaries and audit metadata.
- JPA integration tests prove physical old-row removal rollback, concurrent
  same-key uniqueness and rollback on audit-registration failure.
- Audit adapter tests prove after-commit-only dispatch and rejection outside an
  active transaction.
- Cashier tests prove realized balances are invariant while scheduled
  projections are reversed and recreated.
- Authorization and architecture tests preserve roles, dedicated use-case
  ownership and centralized participant notification.

## Verification Results

- `./mvnw -q -DskipTests compile`: passed.
- `./mvnw -q -DskipTests test-compile`: passed.
- Focused and expanded 035b Maven test selections: passed.
- `./mvnw test`: 385 tests passed; 0 failures, 0 errors, 0 skipped.
- `git diff --check`: passed before and after SDD closeout.
- No required verification was omitted.

## Prerequisite And Acceptance Review

- Task `030b` central participant deletion and optional source notification are
  preserved; replacement delegates all party and source effects to the notifier.
- Task `032b` Cashier due-date, waiting-projection, reversal and locking
  semantics are reused without changing realized-balance behavior.
- Task `034b` FTP aggregate loading, authenticated actor, idempotency and
  reservation behavior remain compatible with the expanded replacement outcome.
- The backend plan and authoritative specs agree that provisional deletion is
  physical and unaudited, while only definitive creation is audited after
  commit without a replacement link.
- Task `036b` remains outside scope: no check-in or checkout orchestration was
  changed.
- No contradiction was found among the required documents, implementation or
  acceptance criteria; no prerequisite correction was necessary.

Every acceptance criterion is covered and passed. Task `036b` was outside this
execution and was subsequently implemented and verified in its own execution.
