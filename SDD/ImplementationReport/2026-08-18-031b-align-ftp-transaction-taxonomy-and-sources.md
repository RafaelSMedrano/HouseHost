# Implementation Report — Task 031b

## Task And Implementation File

- Task: `031b`
- Executed task file:
  `SDD/tasks/backendSpecs/031b-DONE-align-ftp-transaction-taxonomy-and-sources.md`
- Execution date: 2026-08-18
- Approval: explicit user instruction to execute task `031b`.

## Specs, Prerequisites And Plans Read

- `SDD/specs/sddSpec.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/plans/backendSpecs/financialTransactionPlanBackendPlan.md`
- `SDD/tasks/backendSpecs/029b-DONE-remove-financial-transaction-directional-amounts.md`
- `SDD/tasks/backendSpecs/030b-DONE-notify-financial-source-deletion.md`

The prerequisite chain reaches the degree-zero project spec through the module
architecture and LGPD governance specs. No undeclared prerequisite was needed.

## Files Created

- `src/test/java/com/househost/finance/financialtransaction/adapter/out/persistence/FinancialTransactionPersistenceMapperTest.java`
  verifies the seven-value domain, JPA and API round-trip, explicit block
  purposes and the PLAN-to-INSTALLMENT ownership boundary.
- `src/test/java/com/househost/finance/financialtransaction/application/service/FinancialTransactionClassificationValidationTest.java`
  verifies that public creation cannot claim FTP purposes or ownership sources.
- `SDD/ImplementationReport/2026-08-18-031b-align-ftp-transaction-taxonomy-and-sources.md`
  records this execution and its verification evidence.

The task file was renamed from
`031b-align-ftp-transaction-taxonomy-and-sources.md` to
`031b-DONE-align-ftp-transaction-taxonomy-and-sources.md`; this is an SDD
completion rename rather than a new implementation artifact.

## Files Changed

- `src/main/java/com/househost/finance/financialtransaction/domain/model/FinancialTransactionType.java`
  replaces the transitional vocabulary with the seven authoritative values.
- `src/main/java/com/househost/finance/financialtransaction/domain/model/FinancialTransactionSourceType.java`
  adds the PLAN ownership source while retaining INSTALLMENT.
- `src/main/java/com/househost/finance/financialtransaction/domain/model/InstallmentPlanTransaction.java`
  retains an explicit permitted direct purpose and synchronizes internal
  installments to the block's persisted identity.
- `src/main/java/com/househost/finance/financialtransaction/domain/model/InstallmentTransaction.java`
  fixes the internal type and derives its source from the owning block.
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/entity/InstallmentPlanTransactionJpaEntity.java`
  persists the explicit direct purpose and stops copying its source to children.
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/entity/InstallmentTransactionJpaEntity.java`
  persists the internal type and synchronizes INSTALLMENT ownership during JPA
  persistence and update callbacks.
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/FinancialTransactionPersistenceMapper.java`
  preserves direct-purpose types and restores the two-level source hierarchy.
- `src/main/java/com/househost/finance/financialtransaction/application/service/FinancialTransactionService.java`
  applies the standalone-creation type boundary before creating a transaction.
- `src/main/java/com/househost/finance/financialtransaction/application/service/FinancialTransactionValidationService.java`
  reserves FTP purpose types and PLAN/INSTALLMENT sources for owning flows.
- `src/main/java/com/househost/finance/financialtransaction/application/service/InstallmentPlanValidationService.java`
  prevents the existing standalone installment API from claiming ownership
  sources that only future internal FTP flows may assign.
- `src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java`
  deterministically migrates legacy types, preserves later specific purposes
  across reruns and repairs internal installment source pairs.
- `src/test/java/com/househost/config/DatabaseSchemaCompatibilityRunnerFinancialClassificationTest.java`
  verifies migration order, mappings, subtype precedence and source repair SQL.
- `src/test/java/com/househost/finance/financialtransaction/architecture/FinancialTransactionLegacyClassificationRemovalTest.java`
  protects the exact authoritative enum vocabulary and legacy-value removal.
- `src/test/java/com/househost/finance/financialtransaction/application/service/InstallmentPlanTransactionServiceTest.java`
  updates creation audit expectations to the generic block classification.
- `frontend/admin/js/views/financialTransactionProfileView.js`
  replaces only the existing classification label map; no FTP workflow was
  introduced.
- `frontend/admin/tests/financialTransactionClassificationRemoval.test.mjs`
  verifies all authoritative labels and the removal of transitional labels.
- `SDD/implementation/implementation-order.md`
  points the ordered item to the completed task filename.
- `SDD/implementation/task-bootstrap.md`
  records task 031b as completed and leaves tasks 032b through 036b proposed.
- `SDD/tasks/backendSpecs/032b-DONE-correct-cashier-schedule-semantics.md`
  updates its prerequisite reference to the completed 031b filename.
- `SDD/tasks/backendSpecs/033b-DONE-create-ftp-domain-and-persistence.md`
  updates its prerequisite reference to the completed 031b filename.

## Flows Implemented

- Authoritative classification through enum, domain, persistence mapper and API
  serialization/deserialization.
- Explicit purpose retention for direct installment blocks.
- Mandatory `INSTALLMENT_TRANSACTION` classification for internal installments.
- Direct PLAN ownership on a block without propagation to internal installments.
- Internal INSTALLMENT ownership tied to the block ID after identity assignment.
- Deterministic legacy migration with safe repeated execution.
- Rejection of arbitrary FTP classification and ownership claims through the
  existing public transaction endpoints.

## Technical And MVP Decisions

- Existing standalone installment creation defaults to
  `INSTALLMENT_PLAN_BLOCK`; specific plan purposes require an explicit domain
  construction path.
- Valid direct block purposes are the three timed payment purposes,
  `PLAN_TRANSACTION` and `INSTALLMENT_PLAN_BLOCK`. STANDARD and the internal
  installment type are rejected for that concrete class.
- The migration first translates known legacy semantic types, then uses JOINED
  subtype tables to classify structural blocks and internal installments.
- Repeated compatibility execution preserves every already-authoritative direct
  block purpose instead of resetting it to the generic value.
- A JPA lifecycle synchronization covers the point where an identity-generated
  block ID becomes available; mapper and domain synchronization cover restored
  aggregates.
- No FTP aggregate, table, endpoint, command, replacement flow or new audit event
  was created.

## Difficulties, Problems And Resolutions

- The source ID of a new internal installment cannot be known during initial
  domain construction because the block uses generated persistence identity.
  The resolution combines structural association, post-ID domain restoration
  and a JPA persistence callback without inventing a temporary FTP identity.
- The first full Maven execution had one failure in the pre-existing
  `LogbackRollingPolicyTest`. The test passed immediately in isolation, and the
  complete Maven suite passed on repetition. No unrelated logging code was
  changed.

## Tests And Verification

- `./mvnw -q -DskipTests compile` — passed.
- Focused Maven tests for migration, mapper, architecture, validation and
  installment service — passed.
- `node --test frontend/admin/tests/financialTransactionClassificationRemoval.test.mjs`
  — 2 tests passed.
- First `./mvnw test` — 313 of 314 passed; one unrelated log-rotation timing
  assertion failed.
- `./mvnw -q -Dtest=LogbackRollingPolicyTest test` — passed.
- Repeated `./mvnw -q test` — all 314 tests passed.
- `node --test frontend/admin/tests/*.test.mjs` — all 141 tests passed.
- `git diff --check` — passed.

No required verification was omitted.

## Prerequisite And Acceptance Review

- The result conforms to the mother spec, module boundaries, LGPD governance,
  the FTP product specification and the backend plan.
- The seven authoritative types round-trip through domain, JPA and API.
- Migration behavior is deterministic, subtype-aware and removes stored legacy
  classifications while preserving later authoritative purpose assignments.
- Direct block and internal installment classification/source boundaries are
  explicit and verified.
- No premature FTP persistence, API, frontend workflow or audit lifecycle was
  implemented.
- Every acceptance criterion passed. No unresolved contradiction remains.
