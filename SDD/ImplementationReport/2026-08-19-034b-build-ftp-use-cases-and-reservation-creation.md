# Implementation Report — Task 034b

## Task And Authorization

- Task: `034b`.
- Executed task file:
  `SDD/tasks/backendSpecs/034b-DONE-build-ftp-use-cases-and-reservation-creation.md`.
- Execution date: 2026-08-19.
- Authorization: explicit user instruction to execute task `034b`.

## Result

The backend now exposes authenticated FTP creation and query contracts, creates
a booking and its complete initial payment allocation in one transaction,
recalculates the authoritative booking total on the backend and reconciles
repeated reservation commands through actor-scoped idempotency.

Received signals are settled with their effective date. Scheduled signals and
the check-in/checkout allocations remain waiting; the latter are provisional
and emit no provisional creation audit. PLAN source callbacks use the narrow
participation port for attach, settlement refresh and detach without calling
the principal workflow recursively.

## Main Artifacts Created

- FTP HTTP and use-case boundary: `FinancialTransactionPlanController`,
  `FinancialTransactionPlanUseCase` and
  `FinancialTransactionPlanParticipationUseCase`.
- FTP application behavior: `FinancialTransactionPlanService` and
  `FinancialTransactionPlanValidationService`.
- Reservation and FTP contracts: purpose-specific allocation DTOs, minimized
  summary/profile DTOs, creation outcome DTO and trusted reservation command
  record.
- Idempotency: operation/status records, persistence port, JPA entity,
  repository and adapter with a unique actor/operation/key scope.
- Integrations: authenticated command actor adapter and PLAN source adapter.
- Booking form contract: explicit guest, reservation and response DTOs.
- Conflict handling: dedicated FTP conflict exception and explicit payment
  structure vocabulary.

## Existing Artifacts Changed

- Booking form orchestration now claims or replays idempotency before mutation,
  creates the booking and FTP under the same transaction and retains valid
  booking-only behavior when no allocation applies.
- `BookingService` no longer fabricates a legacy financial transaction for all
  bookings; the booking-form FTP workflow is the authoritative orchestration.
- FTP persistence can find an aggregate by source, and schema compatibility now
  creates the command-idempotency table and indexes repeatably.
- Financial transaction/domain settlement and cancellation now keep direct and
  installment components coherent with the derived FTP lifecycle.
- Participant notification expands installment blocks for participant effects,
  invokes the source creation callback once and offers source-only settlement
  refresh where the installment workflow already handled participants.
- Security distinguishes minimized operational reads from management-only
  profile and mutation routes; FTP command conflicts map to HTTP 409.
- Existing booking, notifier, installment, schema and architecture tests were
  aligned with the new orchestration boundary.

## Verification Coverage

- Validation tests cover exact allocation, incomplete/excess allocation,
  purpose requirements and installment constraints.
- Service tests cover received versus scheduled signals, provisional
  check-in/checkout components, idempotent replay and management operations.
- Booking-form tests cover booking-only behavior, trusted command mapping,
  replay without duplicate booking, the transactional boundary and propagation
  of FTP creation failures.
- Persistence tests cover command completion reload and uniqueness conflicts.
- PLAN source adapter tests cover direct attach, refresh and detach delegation.
- HTTP security tests cover authentication plus operational/management role
  separation.

## Verification Results

- Focused 034b suite: passed.
- `./mvnw test`: 369 tests passed; 0 failures, 0 errors, 0 skipped.
- `git diff --check`: passed before SDD closeout.

Every acceptance criterion is covered and passed. Task `035b` was subsequently
completed; task `036b` was subsequently completed and was not implemented by this
execution.
