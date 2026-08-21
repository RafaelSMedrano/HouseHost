# Implementation Report — Task 029b

## Task And Implementation File

- Task: `029b-DONE-remove-financial-transaction-directional-amounts.md`
- Implementation control: `SDD/implementation/implementation-order.md`
- Execution date: 2026-08-17

## Specs, Prerequisites And Plans Read

- `SDD/specs/sddSpec.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/plans/backendSpecs/financialTransactionDirectionalAmountsBackendPlan.md`

## Files Created

- `SDD/plans/backendSpecs/financialTransactionDirectionalAmountsBackendPlan.md`
- `SDD/tasks/backendSpecs/029b-DONE-remove-financial-transaction-directional-amounts.md`
- `SDD/ImplementationReport/2026-08-17-029b-remove-financial-transaction-directional-amounts.md`
- `src/test/java/com/househost/config/DatabaseSchemaCompatibilityRunnerFinancialClassificationTest.java`
- `src/test/java/com/househost/finance/financialtransaction/architecture/FinancialTransactionLegacyClassificationRemovalTest.java`

## Files Changed

- `README1.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `src/main/java/com/househost/booking/booking/application/service/BookingService.java`
- `src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java`
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/FinancialTransactionPersistenceMapper.java`
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/entity/FinancialTransactionJpaEntity.java`
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/entity/InstallmentPlanTransactionJpaEntity.java`
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/entity/InstallmentTransactionJpaEntity.java`
- `src/main/java/com/househost/finance/financialtransaction/application/dto/FinancialTransactionResponseDTO.java`
- `src/main/java/com/househost/finance/financialtransaction/application/dto/InstallmentPlanTransactionRequestDTO.java`
- `src/main/java/com/househost/finance/financialtransaction/application/service/FinancialTransactionService.java`
- `src/main/java/com/househost/finance/financialtransaction/application/service/FinancialTransactionValidationService.java`
- `src/main/java/com/househost/finance/financialtransaction/application/service/InstallmentPlanTransactionService.java`
- `src/main/java/com/househost/finance/financialtransaction/application/service/InstallmentPlanValidationService.java`
- `src/main/java/com/househost/finance/financialtransaction/domain/model/FinancialTransaction.java`
- `src/main/java/com/househost/finance/financialtransaction/domain/model/FinancialTransactionType.java`
- `src/main/java/com/househost/finance/financialtransaction/domain/model/InstallmentPlanTransaction.java`
- `src/main/java/com/househost/finance/financialtransaction/domain/model/InstallmentTransaction.java`
- `src/test/java/com/househost/finance/cashier/application/service/CashierTransactionParticipantServiceTest.java`
- `src/test/java/com/househost/finance/financialtransaction/application/service/FinancialParticipantNotifierTest.java`
- `src/test/java/com/househost/finance/financialtransaction/application/service/FinancialTransactionServiceAuditTest.java`
- `src/test/java/com/househost/finance/financialtransaction/application/service/InstallmentPlanTransactionServiceTest.java`

## Flows Implemented

- Ordinary transactions receive and return one required structural type.
- Booking payments are created as `STANDARD`.
- Installment plans and their installments use
  `INSTALLTMENT_PLAN_TRANSACTION`.
- Persistence maps the type without reconstructing directional amount values.
- Compatibility migration normalizes retained amounts, removes
  `entry_amount` and `expense_amount`, and maps null, invalid and legacy types
  to `STANDARD`.

## Technical And MVP Decisions

- Sender and receiver positions remain the source of movement direction for a
  Cashier participant; the transaction no longer duplicates that direction in
  two amount fields.
- The exact user-approved type vocabulary is `STANDARD`,
  `PLAN_SIGNAL_TRANSACTIONAL`, `PLAN_TRANSACTIONAL` and
  `INSTALLTMENT_PLAN_TRANSACTION`.
- The intentionally supplied `INSTALLTMENT` spelling is preserved as part of
  the external and persisted contract.
- The persisted property is represented as a required `varchar(50)` so the
  compatibility runner can safely replace the former enum vocabulary.
- Existing transaction identities and remaining state are preserved.

## Difficulties, Problems And Resolutions

- The initial instruction removed `type`; the subsequent explicit instruction
  restored it with a new classification vocabulary before completion.
- The local MySQL server was stopped. Starting it with the bundled service
  script failed because the data directory requires administrator access, and
  passwordless `sudo` was unavailable. The idempotent migration is therefore
  ready to run automatically at the next authorized backend startup, but was
  not applied to that stopped local MySQL instance during this task.
- Migration behavior was verified with focused JDBC mocks and the generated H2
  test schema instead of claiming an unavailable local MySQL execution.
- The repository contained unrelated pre-existing changes; edits remained
  restricted to financial transaction classification, presentation, tests and
  their SDD records.

## Tests And Verification

- Backend compilation: passed.
- Focused migration, legacy-contract, financial service, installment,
  participant and Cashier tests: passed.
- `./mvnw -q test`: passed with zero failures and zero errors.
- Searches confirmed the core backend contracts no longer expose
  `entryAmount` or `expenseAmount` and retain the approved type vocabulary.
- `git diff --check`: passed after final documentation updates.

## Prerequisite Review

- The main financial-management capability remains intact.
- Domain, application, persistence and compatibility layers use one positive
  transaction amount and one structural classification.
- Cashier-owned direction remains derived from sender and receiver movements.
- The migration is idempotent and retains existing rows while deleting only
  the two obsolete columns and their legacy values.
- Audit metadata remains minimized and includes the retained classification.
- All backend acceptance criteria and prerequisite documents are conformant.
