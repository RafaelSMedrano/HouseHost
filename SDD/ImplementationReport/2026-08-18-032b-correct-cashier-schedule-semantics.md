# Implementation Report — Task 032b

## Task And Implementation File

- Task: `032b`
- Executed task file:
  `SDD/tasks/backendSpecs/032b-DONE-correct-cashier-schedule-semantics.md`
- Execution date: 2026-08-18
- Approval: explicit user instruction to execute task `032b`.

## Specs, Prerequisites And Plans Read

- `SDD/specs/sddSpec.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/plans/backendSpecs/financialTransactionPlanBackendPlan.md`
- `SDD/tasks/backendSpecs/031b-DONE-align-ftp-transaction-taxonomy-and-sources.md`

The prerequisite chain reaches the degree-zero project spec through the module
architecture and LGPD governance specs. No undeclared prerequisite was needed.

## Files Created

- `src/test/java/com/househost/config/DatabaseSchemaCompatibilityRunnerCashierTemporalTest.java`
  verifies deterministic legacy-date migration without inventing settlement
  evidence and verifies the absent-table no-op.
- `src/test/java/com/househost/finance/cashier/adapter/out/persistence/CashierPessimisticLockIntegrationTest.java`
  proves with two real transactions that a second Cashier mutation waits for
  the first pessimistic write lock.
- `src/test/java/com/househost/finance/cashier/adapter/out/persistence/CashierTemporalPersistenceMapperTest.java`
  verifies due and settlement dates across domain, JPA and response contracts,
  including the temporary legacy response aliases.
- `src/test/java/com/househost/finance/cashier/application/service/CashierMovementServiceTest.java`
  verifies scheduling, single settlement, idempotent repetition and waiting
  reversal without realized-balance mutation.
- `SDD/ImplementationReport/2026-08-18-032b-correct-cashier-schedule-semantics.md`
  records this execution and its verification evidence.

The task file was renamed from
`032b-correct-cashier-schedule-semantics.md` to
`032b-DONE-correct-cashier-schedule-semantics.md`; this is an SDD completion
rename rather than a new implementation artifact.

## Files Changed

- `src/main/java/com/househost/finance/cashier/domain/model/CashierEntry.java`
  separates mandatory due date from nullable actual settlement date and makes
  settlement idempotent.
- `src/main/java/com/househost/finance/cashier/domain/model/CashierExpense.java`
  applies the same temporal and settlement semantics to outflows.
- `src/main/java/com/househost/finance/cashier/adapter/out/persistence/entity/CashierEntryJpaEntity.java`
  persists entry due and settlement dates independently.
- `src/main/java/com/househost/finance/cashier/adapter/out/persistence/entity/CashierExpenseJpaEntity.java`
  persists expense due and settlement dates independently.
- `src/main/java/com/househost/finance/cashier/adapter/out/persistence/CashierEntryPersistenceMapper.java`
  restores both entry dates without inference.
- `src/main/java/com/househost/finance/cashier/adapter/out/persistence/CashierExpensePersistenceMapper.java`
  restores both expense dates without inference.
- `src/main/java/com/househost/finance/cashier/application/dto/CashierEntryResponseDTO.java`
  exposes official `dueDate` and `settlementDate` fields while retaining
  `entryDate` as a due-date compatibility alias for the current frontend.
- `src/main/java/com/househost/finance/cashier/application/dto/CashierExpenseResponseDTO.java`
  exposes official temporal fields while retaining `expenseDate` as the
  corresponding compatibility alias.
- `src/main/java/com/househost/finance/cashier/application/port/out/CashierPersistencePort.java`
  declares the aggregate-loading write-lock operation.
- `src/main/java/com/househost/finance/cashier/adapter/out/persistence/CashierJpaRepository.java`
  implements pessimistic write selection for one Cashier row.
- `src/main/java/com/househost/finance/cashier/adapter/out/persistence/CashierPersistenceAdapter.java`
  maps the new locking operation through the persistence boundary.
- `src/main/java/com/househost/finance/cashier/application/service/CashierMovementService.java`
  serializes aggregate mutation, copies transaction dates, makes repeated
  scheduling and settlement deterministic and reverses movements after lock
  acquisition against freshly loaded state.
- `src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java`
  migrates ambiguous legacy movement dates exclusively to due dates, leaves
  settlement null and enforces mandatory due dates after data migration.
- `src/main/java/com/househost/metrics/application/service/MetricsCalculationService.java`
  adopts the official due-date contract for monthly Cashier metrics.
- `SDD/implementation/implementation-order.md`
  points the ordered item to the completed task filename and records completion.
- `SDD/implementation/task-bootstrap.md`
  records task 032b as completed and leaves tasks 033b through 036b proposed.
- `SDD/ImplementationReport/2026-08-18-031b-align-ftp-transaction-taxonomy-and-sources.md`
  updates its task reference after the required completion rename.
- `SDD/tasks/backendSpecs/033b-DONE-create-ftp-domain-and-persistence.md`
  updates its prerequisite to the completed 032b filename.
- `SDD/tasks/backendSpecs/035b-DONE-implement-atomic-ftp-payment-replacement.md`
  updates its prerequisite to the completed 032b filename.

## Flows Implemented

- Waiting deposits and withdrawals use the financial transaction due date and
  change only waiting and expected projections.
- Settlement records the actual transaction settlement date, preserves the due
  date and updates realized balances once.
- Scheduling returns an existing transaction-and-Cashier movement without
  duplicating projections.
- Reversal locks every affected Cashier in deterministic ID order, reloads
  movement state, restores projections and safely repeats as a no-op.
- Legacy `entry_date` and `expense_date` values become due dates only;
  historical settlement dates remain unknown instead of fabricated.
- API responses expose official temporal names while keeping the existing
  frontend operational until its planned migration.

## Technical And MVP Decisions

- The Cashier aggregate is locked before idempotency lookup because two
  concurrent first schedules must not both change projections.
- Multi-Cashier reversal locks sorted IDs to reduce deadlock risk, then reloads
  movements so decisions use state observed after lock acquisition.
- Existing unique transaction-and-Cashier constraints remain the database
  backstop for movement identity.
- JPA permits the transitional due column to be initially nullable so
  `ddl-auto=update` can add it safely; the compatibility runner copies data and
  then changes the database column to `NOT NULL`.
- Legacy response names are aliases of due date, not extra temporal facts.
- No FTP aggregate, workflow, endpoint or audit event was introduced.

## Tests And Verification

- Focused Cashier service, participant, temporal mapper, schema migration and
  pessimistic-lock tests — 13 tests passed.
- `./mvnw test` — 323 tests passed with no failures, errors or skips.
- `git diff --check` — passed before SDD closeout and again after it.

No required verification was omitted.

## Prerequisite And Acceptance Review

- Waiting creation and reversal affect projections without mutating realized
  balances.
- Settlement is idempotent, changes realized values once and preserves due date
  while recording actual settlement date.
- Repeated scheduling and reversal are deterministic.
- Legacy temporal migration has one explicit meaning and never manufactures
  settlement history.
- Real pessimistic locking prevents concurrent lost updates.
- The result conforms to the mother spec, module boundaries, LGPD governance,
  FTP product specification and backend plan.
- Every acceptance criterion passed. No unresolved contradiction remains.
