# Implementation Report — Task 033b

## Task And Implementation File

- Task: `033b`
- Executed task file:
  `SDD/tasks/backendSpecs/033b-DONE-create-ftp-domain-and-persistence.md`
- Execution date: 2026-08-18
- Approval: explicit user instruction to execute task `033b`.

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
- `SDD/tasks/backendSpecs/032b-DONE-correct-cashier-schedule-semantics.md`

The prerequisite chain reaches the degree-zero project spec through the module
architecture and LGPD governance specs. No undeclared prerequisite was needed.

## Files Created

- `src/main/java/com/househost/finance/financialtransaction/domain/model/FinancialTransactionPlan.java`
  defines the FTP aggregate, invariants, derived lifecycle, immutable queries
  and controlled composition mutations.
- `src/main/java/com/househost/finance/financialtransaction/domain/model/FinancialTransactionPlanStatus.java`
  defines the five derived lifecycle states.
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/entity/FinancialTransactionPlanJpaEntity.java`
  persists the aggregate header, version and timestamps.
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/FinancialTransactionPlanJpaRepository.java`
  provides header persistence and pessimistic write lookup.
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/InstallmentPlanTransactionJpaRepository.java`
  fetches direct blocks and their internal installments as one component query.
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/FinancialTransactionPlanPersistenceMapper.java`
  maps the aggregate header and reconstructs a verified complete domain model.
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/FinancialTransactionPlanPersistenceAdapter.java`
  atomically persists and loads headers plus owned direct components.
- `src/main/java/com/househost/finance/financialtransaction/application/port/out/FinancialTransactionPlanPersistencePort.java`
  declares aggregate save, load, locked load and deletion operations.
- `src/test/java/com/househost/finance/financialtransaction/domain/model/FinancialTransactionPlanTest.java`
  verifies invariants, status precedence, queries, mutations and installment
  behavior.
- `src/test/java/com/househost/finance/financialtransaction/adapter/out/persistence/FinancialTransactionPlanPersistenceIntegrationTest.java`
  verifies round trips, stable ordering, bounded loading, optimistic conflicts,
  deletion and real pessimistic locking.
- `src/test/java/com/househost/config/DatabaseSchemaCompatibilityRunnerFinancialTransactionPlanTest.java`
  verifies repeatable FTP schema creation without historical data conversion.
- `src/test/java/com/househost/finance/financialtransaction/architecture/FinancialTransactionPlanArchitectureTest.java`
  protects the domain boundary and confirms no endpoint or use-case workflow was
  introduced prematurely.
- `SDD/ImplementationReport/2026-08-18-033b-create-ftp-domain-and-persistence.md`
  records this execution and its verification evidence.

The task file was renamed from
`033b-create-ftp-domain-and-persistence.md` to
`033b-DONE-create-ftp-domain-and-persistence.md`; this is an SDD completion
rename rather than a new implementation artifact.

## Files Changed

- `src/main/java/com/househost/finance/financialtransaction/domain/model/FinancialTransaction.java`
  stores stable PLAN component order and exposes controlled membership restore.
- `src/main/java/com/househost/finance/financialtransaction/domain/model/InstallmentPlanTransaction.java`
  enforces two-to-twelve installments for new blocks while retaining a bounded
  historical restoration path and last-installment cent residual.
- `src/main/java/com/househost/finance/financialtransaction/application/service/InstallmentPlanValidationService.java`
  applies the same two-to-twelve boundary to the existing creation service.
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/entity/FinancialTransactionJpaEntity.java`
  persists PLAN component order and exposes due-date restoration for blocks.
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/FinancialTransactionPersistenceMapper.java`
  round-trips component order and due date and preserves historical installment
  plans through the restoration factory.
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/FinancialTransactionJpaRepository.java`
  selects direct PLAN components in stable due-date and component order.
- `src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java`
  creates the FTP header schema, extends PLAN-compatible source storage and adds
  component-order indexing without rewriting historical installment plans.
- `src/test/java/com/househost/finance/financialtransaction/adapter/out/persistence/FinancialTransactionPersistenceMapperTest.java`
  proves that a historical one-installment record remains readable while new
  one-installment construction is rejected.
- `src/test/java/com/househost/finance/financialtransaction/application/service/InstallmentPlanTransactionServiceTest.java`
  aligns service fixtures with the new minimum of two installments.
- `SDD/implementation/implementation-order.md`
  points the ordered item to the completed filename and records completion.
- `SDD/implementation/task-bootstrap.md`
  records task 033b as completed and leaves tasks 034b through 036b proposed.
- `SDD/ImplementationReport/2026-08-18-031b-align-ftp-transaction-taxonomy-and-sources.md`
  updates its task reference after the required completion rename.
- `SDD/ImplementationReport/2026-08-18-032b-correct-cashier-schedule-semantics.md`
  updates its task reference after the required completion rename.
- `SDD/tasks/backendSpecs/034b-DONE-build-ftp-use-cases-and-reservation-creation.md`
  updates its prerequisite to the completed 033b filename.

## Flows Implemented

- Defensive FTP construction validates participants, external source,
  transaction ownership, classification, dates, identities and duplicates.
- Direct components remain stable by due date and persisted tie order; direct
  installment blocks count once in the total but expose their internal
  installments for settlement queries.
- Status is derived with cancellation, full settlement, overdue and partial
  settlement precedence; settlement date is the latest actual component date.
- Controlled add, grouped add, remove, replace, deadline extension and
  cancellation preserve settled history and aggregate identity.
- Persistence allocates the header identity before assigning PLAN membership,
  writes aggregate members in one transaction and rejects stale versions.
- Complete aggregate loading uses one header select, one direct-transaction
  select and one direct-block-with-installments select; mutation lookup can lock
  the header pessimistically.
- Deletion is permitted only for eligible never-settled aggregates and removes
  owned direct components so no participant-owned artifact survives.

## Technical And MVP Decisions

- FTP lifecycle remains derived domain state; persisted status is verified and
  refreshed rather than accepted as an independent authority.
- Header-first ID allocation and PLAN membership assignment occur inside the
  same database transaction, so no externally observable half-plan exists.
- Optimistic versioning protects ordinary saves; a separate pessimistic lookup
  supports later multi-step mutations that require serialization.
- The component loader is deliberately split by concrete JOINED subtype to
  avoid an unbounded select-per-component pattern while retaining full block
  contents.
- Stable ordering is explicit because due dates are not unique; Java's stable
  sort preserves caller order before persistence assigns the tie index.
- New installment plans obey the current two-to-twelve rule. Historical
  positive-count plans remain readable without data conversion, but are not
  silently treated as valid new FTP composition outside the current boundary.
- No endpoint, application command, reservation workflow, payment replacement
  or new audit event was introduced; those remain in tasks 034b through 036b.

## Difficulties, Problems And Resolutions

- A new direct component cannot reference a generated plan ID before the header
  exists. The adapter flushes the header, assigns membership, then persists all
  members within the same transaction.
- The first persistence round-trip test exposed that an installment block's
  outer due date was not being restored. The mapper now restores due date for
  every transaction kind, preserving aggregate ordering.
- Tightening the installment minimum would have made a legacy one-installment
  row unreadable. A persistence-only restoration factory retains historical
  positive-count records while public constructors enforce the current rule.

## Tests And Verification

- Focused FTP domain, persistence, architecture, schema, mapper and installment
  service execution — all selected tests passed.
- `./mvnw test` — 344 tests passed with no failures, errors or skips.
- `git diff --check` — passed before SDD closeout and again after it.
- Changed Java files were checked for tabs and overlong lines; no violations
  were found.

No required verification was omitted.

## Prerequisite And Acceptance Review

- Every planned invariant, query, lifecycle precedence and composition mutation
  has direct domain coverage.
- Direct installment blocks count once in aggregate totals and expand only for
  settlement evidence.
- Settled history is immutable and retained; only eligible never-settled plans
  can be canceled and deleted.
- PLAN ownership and equal-date ordering survive complete persistence round
  trips.
- Complete loading is bounded to three selects, stale saves fail and real
  pessimistic locking serializes competing mutation access.
- Historical fixed installment plans are not converted or rewritten.
- No public or administrative endpoint exists for FTP yet.
- Every acceptance criterion passed. No unresolved contradiction remains.
