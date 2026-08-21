# Implementation Report — Task 028b

## Task And Implementation File

- Task: `028b-DONE-centralize-financial-participant-notification.md`
- Implementation control: `SDD/implementation/implementation-order.md`
- Execution date: 2026-08-13

## Specs, Prerequisites And Plans Read

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/plans/backendSpecs/financialParticipantNotificationBackendPlan.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`

## Files Created

- `SDD/plans/backendSpecs/financialParticipantNotificationBackendPlan.md`
- `SDD/tasks/backendSpecs/028b-DONE-centralize-financial-participant-notification.md`
- `SDD/ImplementationReport/2026-08-13-028b-centralize-financial-participant-notification.md`
- `src/main/java/com/househost/finance/cashier/application/port/in/CashierFinancialTransactionUseCase.java`
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/integration/CashierFinancialPartyAdapter.java`
- `src/test/java/com/househost/finance/cashier/application/service/CashierTransactionParticipantServiceTest.java`
- `src/test/java/com/househost/finance/financialtransaction/adapter/out/integration/CashierFinancialPartyAdapterTest.java`
- `src/test/java/com/househost/finance/financialtransaction/application/service/FinancialParticipantNotifierTest.java`
- `src/test/java/com/househost/finance/financialtransaction/application/service/FinancialParticipantCommunicationArchitectureTest.java`

## Files Changed

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `src/main/java/com/househost/finance/cashier/application/service/CashierTransactionParticipantService.java`
- `src/main/java/com/househost/finance/financialtransaction/application/service/FinancialParticipantNotifier.java`
- `src/main/java/com/househost/finance/financialtransaction/application/service/FinancialTransactionService.java`
- `src/main/java/com/househost/finance/financialtransaction/application/service/InstallmentPlanTransactionService.java`
- `src/test/java/com/househost/finance/financialtransaction/application/service/FinancialTransactionServiceAuditTest.java`
- `src/test/java/com/househost/finance/financialtransaction/application/service/InstallmentPlanTransactionServiceTest.java`

## Files Removed

- `src/main/java/com/househost/finance/financialtransaction/application/service/FinancialSourceNotifier.java`

## Flows Implemented

- Financial transaction creation notifies sender and receiver through
  `FinancialParticipantNotifier` and `FinancialPartyResolver`.
- Ordinary settlement notifies both financial parties and then delegates the
  optional source effect through `FinancialTransactionSourceResolver`.
- Individual installment settlement notifies only the financial parties.
- The final installment settles the plan, then the central notifier propagates
  the plan settlement to its parties and source.
- Cashier participant callbacks are handled by
  `CashierFinancialPartyAdapter`, which invokes the Cashier-owned
  `CashierFinancialTransactionUseCase`.
- Cashier continues to schedule deposits or withdrawals, settle them and
  reverse individual or installment-plan movements.
- Deletion retains the existing participant cleanup behavior.

## Technical And MVP Decisions

- Source notification was consolidated into `FinancialParticipantNotifier`
  because the architecture permits only one module-local participant
  coordination point.
- `notifyInstallmentSettlement` explicitly represents a partial settlement so
  that the transaction source is not notified before the complete plan settles.
- Cashier did not receive a `ParticipantNotifier` without Resolvers because it
  is a destination in this flow and does not initiate an external mutation.
- The transaction submodule owns `CashierFinancialPartyAdapter`; Cashier owns
  the inbound use case and application behavior it exposes.
- Financial participant and source selection, movement persistence, schema and
  API contracts remain unchanged.

## Difficulties, Problems And Resolutions

- The initial implementation incorrectly interpreted Finance as only the
  financial transaction submodule and marked the task complete before
  assessing Cashier. The task was reopened, its scope corrected and all DONE
  references withheld until the complete Finance boundary was verified.
- Moving source behavior blindly into the existing settlement method would
  notify the booking source for every individual installment. A distinct
  partial-installment notification preserves the intended boundary.
- Cashier previously implemented another submodule's outbound port directly
  from its application service. A transaction-owned adapter and Cashier-owned
  inbound use case corrected the dependency direction without changing
  monetary behavior.
- The repository contained unrelated pre-existing changes. The implementation
  remained restricted to Finance coordination, focused tests and its SDD files.

## Tests And Verification

- Focused notifier, Cashier adapter, Cashier participant, architecture and
  financial service tests: passed, 20 tests with zero failures and zero errors.
- `./mvnw test`: passed, 303 tests with zero failures and zero errors.
- `git diff --check`: passed.
- Searches confirmed `FinancialSourceNotifier` has no remaining Java reference,
  principal financial services inject no Resolver or second Notifier, and the
  Cashier application layer no longer implements or imports `FinancialParty`.

## Prerequisite Review

- The project financial-management capability remains unchanged.
- The module-architecture requirement of one `ParticipantNotifier` centralizing
  specialized Resolvers is satisfied for financial transaction effects.
- The complete Finance module scope was reviewed, including Cashier.
- Financial boundaries use participant ports, an integration adapter and a
  Cashier-owned inbound use case; no concrete Cashier service is injected into
  the transaction application layer.
- Creation, settlement, partial installment settlement, complete plan
  settlement, Cashier movement and deletion preserve their prior functional
  effects and order.
- Domain behavior, persistence schema, HTTP contracts and frontend behavior
  were not changed.
- All acceptance criteria and prerequisite documents are conformant.

