# Implementation Report — Task 036b

## Task And Authorization

- Task: `036b`.
- Executed task file:
  `SDD/tasks/backendSpecs/036b-DONE-integrate-ftp-checkin-checkout-and-security.md`.
- Execution date: 2026-08-19.
- Authorization: explicit user instruction to execute task `036b`.

## Specs, Prerequisites And Plans Read

- `SDD/specs/sddSpec.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/specs/frontendSpecs/financialTransactionPlanAdministrativeExperienceSpec.md`
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`
- `SDD/plans/backendSpecs/financialTransactionPlanBackendPlan.md`
- `SDD/tasks/backendSpecs/035b-DONE-implement-atomic-ftp-payment-replacement.md`

The prerequisite chain was reviewed before implementation. No contradiction or
missing prerequisite was found.

## Result

Completed check-in and checkout can now optionally materialize the payment
purpose owned by their booking. The operational request accepts only payment
structure, method, installment quantity and idempotency key; protected plan,
transaction, amount, purpose, source, participant and status values are derived
from the locked booking-owned FTP.

If no plan or scheduled purpose exists, an operation without a materialization
request remains valid and creates no zero-value transaction. If an eligible
provisional purpose exists, completed operation requires its materialization.
Requests for pending or canceled operations are rejected. Checkout extra
charges, pending amount and rating remain independent from payment choice.

Financial replacement runs first inside the existing operational transaction.
Failures prevent booking, room, guest, rating and audit effects; successful
same-key repetitions return the authoritative idempotent result. Responses
expose the optional definitive replacement outcome.

## Files Created

- `src/main/java/com/househost/finance/financialtransaction/application/dto/FinancialTransactionPlanMaterializationDTO.java`
  defines the constrained operational payment choice.
- `src/main/java/com/househost/finance/financialtransaction/application/records/FinancialTransactionPlanMaterializationCommandRecord.java`
  carries the trusted booking-owned materialization command.
- `src/main/java/com/househost/booking/checking/application/service/CheckInFinancialResolver.java`
  adapts completed check-in to the financial inbound use case.
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutFinancialResolver.java`
  adapts completed checkout to the financial inbound use case.
- `src/test/java/com/househost/booking/booking/application/service/StayOperationalFinancialMaterializationTest.java`
  verifies purpose selection, returned outcomes, checkout independence and
  rollback before audit.
- `src/test/java/com/househost/booking/booking/adapter/in/rest/StayOperationalFinancialAuthorizationTest.java`
  verifies reception, forbidden housekeeping and unauthenticated boundaries.
- `src/test/java/com/househost/booking/checking/application/service/CheckInValidationServiceTest.java`
  verifies check-in materialization status rules.
- `SDD/ImplementationReport/2026-08-19-036b-integrate-ftp-checkin-checkout-and-security.md`
  records implementation, decisions and verification evidence.

The task file was renamed from
`036b-integrate-ftp-checkin-checkout-and-security.md` to
`036b-DONE-integrate-ftp-checkin-checkout-and-security.md`; this is an SDD
completion rename rather than a new implementation artifact.

## Files Changed

- `src/main/java/com/househost/booking/checking/application/dto/CheckInRequestDTO.java`
  accepts optional constrained payment materialization.
- `src/main/java/com/househost/booking/checking/application/dto/CheckInResponseDTO.java`
  exposes the optional definitive financial outcome.
- `src/main/java/com/househost/booking/checking/application/service/CheckInParticipantNotifier.java`
  owns and orders the new financial resolver boundary.
- `src/main/java/com/househost/booking/checking/application/service/CheckInService.java`
  passes materialization input and returns its outcome.
- `src/main/java/com/househost/booking/checking/application/service/CheckInValidationService.java`
  rejects materialization outside completed check-in.
- `src/main/java/com/househost/booking/checkout/application/dto/CheckOutRequestDTO.java`
  accepts optional constrained payment materialization independently of extras
  and rating.
- `src/main/java/com/househost/booking/checkout/application/dto/CheckOutResponseDTO.java`
  exposes the optional definitive financial outcome.
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutParticipantNotifier.java`
  executes financial materialization before the remaining participants.
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutService.java`
  coordinates payment, rating and audit within the operation transaction.
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutValidationService.java`
  rejects materialization outside completed checkout.
- `src/main/java/com/househost/finance/financialtransaction/application/port/in/FinancialTransactionPlanReplacementUseCase.java`
  exposes booking-owned operational materialization.
- `src/main/java/com/househost/finance/financialtransaction/application/port/out/FinancialTransactionPlanPersistencePort.java`
  declares locked FTP lookup by source.
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/FinancialTransactionPlanJpaRepository.java`
  locks the plan selected by booking source.
- `src/main/java/com/househost/finance/financialtransaction/adapter/out/persistence/FinancialTransactionPlanPersistenceAdapter.java`
  implements the locked source lookup.
- `src/main/java/com/househost/finance/financialtransaction/application/service/FinancialTransactionPlanValidationService.java`
  validates the operational command and shared replacement definition.
- `src/main/java/com/househost/finance/financialtransaction/application/service/FinancialTransactionPlanReplacementService.java`
  derives the owned purpose and performs optional, atomic, idempotent
  materialization.
- `src/test/java/com/househost/booking/checking/application/service/CheckInGuestStatusTest.java`
  supplies the new resolver collaborator.
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutGuestStatusTest.java`
  supplies the new resolver collaborator.
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutHistoryServiceTest.java`
  verifies the expanded participant notification boundary.
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutTransactionIntegrationTest.java`
  preserves rollback coverage through the expanded notifier result.
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutValidationServiceTest.java`
  verifies rejection of pending-operation materialization.
- `src/test/java/com/househost/booking/booking/architecture/CrossModuleServiceCommunicationTest.java`
  enforces Service-to-Notifier-to-Resolver-to-use-case communication.
- `src/test/java/com/househost/finance/financialtransaction/application/service/FinancialTransactionPlanReplacementServiceTest.java`
  covers booking-source purposes, absence and operational replay.
- `src/test/java/com/househost/finance/financialtransaction/adapter/out/persistence/FinancialTransactionPlanReplacementIntegrationTest.java`
  verifies real JPA booking-owned checkout replacement and replay.
- `src/test/java/com/househost/finance/financialtransaction/adapter/in/rest/FinancialTransactionPlanAuthorizationTest.java`
  completes management authorization coverage.
- `src/test/java/com/househost/finance/financialtransaction/architecture/FinancialTransactionPlanArchitectureTest.java`
  prevents protected identifiers and amounts in operational DTOs.
- `SDD/tasks/backendSpecs/036b-DONE-integrate-ftp-checkin-checkout-and-security.md`
  records verified completion in its title and status.
- `SDD/tasks/frontendSpecs/039f-DONE-materialize-ftp-payment-at-checkin.md`
  points to the completed backend prerequisite.
- `SDD/tasks/frontendSpecs/040f-DONE-materialize-ftp-payment-at-checkout-and-verify.md`
  points to the completed backend prerequisite.
- `SDD/implementation/implementation-order.md`
  records completion of the backend FTP sequence.
- `SDD/implementation/task-bootstrap.md`
  records the current completion state.
- `SDD/ImplementationReport/2026-08-19-034b-build-ftp-use-cases-and-reservation-creation.md`
  records subsequent 036b completion without changing 034b scope.
- `SDD/ImplementationReport/2026-08-19-035b-implement-atomic-ftp-payment-replacement.md`
  updates its forward reference and subsequent completion note.

## Design Decisions

- Operational clients cannot select a plan, provisional transaction, amount,
  purpose, source, participants or lifecycle status.
- The plan is locked by source type `BOOKING` and booking identifier, then the
  exact check-in or checkout purpose is derived internally.
- An omitted choice is valid only when no eligible provisional component needs
  materialization. Absence never synthesizes a zero-value FT.
- A requested choice without a matching scheduled purpose is rejected instead
  of creating an unrelated transaction.
- Financial effects precede other participant effects so any later failure
  rolls back the whole operation and prevents after-commit audit.
- Existing schema indexes and security routes already satisfied 036b; no
  production schema or security configuration change was necessary.
- No frontend code was changed by this backend task.

## Difficulties And Resolutions

- Existing tests mocked the former notifier overload. They were updated to
  return the optional outcome from the new overload while retaining compatible
  overloads for bookingless paths.
- Operational lookup could not trust an externally supplied plan identifier.
  A pessimistic source lookup derives and serializes access to the booking FTP.
- Checkout already coordinated extra charges, pending balance and rating. The
  materialization result was threaded separately so those behaviors remain
  independent.

## Verification Results

- `./mvnw -q -DskipTests compile`: passed.
- `./mvnw -q -DskipTests test-compile`: passed.
- Focused and expanded 036b test selections: passed.
- `./mvnw test`: 399 tests passed; 0 failures, 0 errors, 0 skipped.
- `git diff --check`: passed before and after SDD closeout.
- Java tab scan for the affected files: passed.
- No required verification was omitted.

## Prerequisite And Acceptance Review

- Task `035b` atomic replacement, replay, participant cleanup and after-commit
  audit semantics are reused without weakening their guarantees.
- Check-in and checkout select only their booking-owned scheduled purpose.
- Missing scheduled purposes remain non-blocking and no zero-value FT is made.
- Checkout extras, pending amount and rating remain independent.
- Operational and management role boundaries have positive and negative tests.
- Architecture, legacy-removal, privacy, audit, schema compatibility,
  persistence and integration suites passed in the expanded and full runs.

Every acceptance criterion is covered and passed. The backend FTP sequence is
complete; frontend tasks `037f` through `040f` remain proposed and require
separate explicit authorization.
