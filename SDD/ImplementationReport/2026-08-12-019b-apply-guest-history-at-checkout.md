# Implementation Report — Task 019b

## Task And Implementation File

- Task: `019b-DONE-apply-guest-history-at-checkout.md`
- Implementation control: `SDD/implementation/implementation-order.md`
- Execution date: 2026-08-12

## Specs, Prerequisites And Plans Read

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/bookingStayHistoryRetentionSpec.md`
- `SDD/specs/guestRegistrationPolishSpec.md`
- `SDD/plans/backendSpecs/guestRegistrationPolishBackendPlan.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/tasks/backendSpecs/018b-DONE-synchronize-guest-lifecycle-status.md`
- `SDD/tasks/backendSpecs/021b-DONE-centralize-guest-status-participant-notifiers.md`

## Files Created

- `SDD/ImplementationReport/2026-08-12-019b-apply-guest-history-at-checkout.md`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutGuestResolver.java`
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutHistoryServiceTest.java`
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutValidationServiceTest.java`
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutTransactionIntegrationTest.java`
- `src/test/java/com/househost/config/DatabaseSchemaCompatibilityRunnerCheckOutHistoryTest.java`

## Files Changed

- `SDD/tasks/backendSpecs/019b-DONE-apply-guest-history-at-checkout.md`
- `SDD/tasks/frontendSpecs/028f-DONE-move-assessment-to-checkout.md`
- `SDD/ImplementationReport/2026-08-12-018b-synchronize-guest-lifecycle-status.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `src/main/java/com/househost/booking/checkout/application/dto/CheckOutRequestDTO.java`
- `src/main/java/com/househost/booking/checkout/application/dto/CheckOutResponseDTO.java`
- `src/main/java/com/househost/booking/checkout/domain/model/CheckOut.java`
- `src/main/java/com/househost/booking/checkout/application/port/out/CheckOutPersistencePort.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutService.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutValidationService.java`
- `src/main/java/com/househost/booking/checkout/application/service/CheckOutParticipantNotifier.java`
- `src/main/java/com/househost/booking/checkout/adapter/out/persistence/CheckOutJpaRepository.java`
- `src/main/java/com/househost/booking/checkout/adapter/out/persistence/CheckOutPersistenceAdapter.java`
- `src/main/java/com/househost/booking/checkout/adapter/out/persistence/entity/CheckOutJpaEntity.java`
- `src/main/java/com/househost/booking/checkout/adapter/out/persistence/entity/CheckOutPersistenceMapper.java`
- `src/main/java/com/househost/guest/domain/model/Guest.java`
- `src/main/java/com/househost/guest/application/service/GuestService.java`
- `src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java`
- `src/test/java/com/househost/booking/checkout/adapter/out/persistence/entity/CheckOutDetachedBookingPersistenceMapperTest.java`
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutGuestStatusTest.java`
- `src/test/java/com/househost/booking/checkout/application/service/CheckOutParticipantResolverTest.java`
- `src/test/java/com/househost/guest/domain/model/GuestProfileTest.java`

## Files Removed

- The pre-completion task filename was removed when the verified task was
  renamed to include `DONE`.

## Flows Implemented

- Checkout accepts optional `rating` from 1 through 5 and returns the stored
  checkout assessment.
- A completed checkout is saved, finalizes Booking and Room, applies Guest
  history through `CheckOutParticipantNotifier → CheckOutGuestResolver`, marks
  the persistent evidence and only then records success audit.
- Guest history increments the stay count, records the actual checkout date,
  adds the finalized booking amount and updates the latest rating only when one
  is supplied.
- Pending and cancelled checkout do not apply guest history.
- Completed retries reload checkout with a pessimistic write lock and skip the
  Guest effect when `guest_history_applied` is already true.
- MySQL compatibility adds nullable `rating` and non-null
  `guest_history_applied`. Existing completed checkouts are marked as already
  processed to prevent unsafe retroactive duplication of legacy history.

## Authoritative Amount Decision

`Booking.totalAmount` is the authoritative amount incorporated into Guest
`totalSpent`. It is calculated by the backend from the room daily rate, stay
length and booking discount and is already the reservation's finalized lodging
total. Checkout `pendingAmount` and `extraCharges` remain operational inputs and
are not a trustworthy replacement for the booking total; financial transaction
records can also contain multiple partial or status-dependent movements.

## Technical And MVP Decisions

- The accepted assessment scale is the existing UI scale of 1 through 5.
- Absence of a checkout rating preserves the Guest's previous latest rating.
- The evidence flag is infrastructure state and is not exposed in the API.
- The checkout row is pessimistically locked during update so concurrent
  completed retries cannot both observe unapplied evidence.
- The main Checkout service still uses its single `ParticipantNotifier`; the
  new Guest mutation is isolated in a specialized Resolver.
- Audit metadata contains only checkout status and never rating or free text.

## Difficulties, Problems And Resolutions

- Legacy completed checkout rows have no reliable evidence of whether their
  Guest counters came from older data imports or manual history. The migration
  marks them processed instead of applying an unsafe retroactive increment.
- The contract initially used a more specific internal name for assessment.
  It was aligned to the spec and frontend plan's exact `rating` payload before
  completion.
- Database-backed rollback and restart behavior were verified with an H2
  transactional integration harness around the real `CheckOutService` flow.

## Tests And Verification

- Domain tests cover first and subsequent completed stays and preservation of
  an existing rating when no new assessment is supplied.
- Service tests cover completed, pending, retry and downstream failure paths.
- Resolver tests prove that only backend `Booking.totalAmount` reaches Guest.
- Persistence tests cover assessment/evidence mapping and pessimistic locking.
- Schema tests cover fresh, compatible, missing-table and repeated execution.
- Database-backed integration tests cover retry after reload and rollback of
  checkout, Guest history, evidence and audit after Guest failure.
- `./mvnw test`: passed, 242 tests with zero failures and zero errors.
- `git diff --check`: passed.

## Prerequisite Review

- Guest lifecycle status continues to be derived by Booking and is not
  overwritten by the history mutation.
- Booking, Room and Guest effects remain centralized in Checkout's one
  `ParticipantNotifier` and specialized Resolvers.
- Detached checkout history remains readable and retains its own Guest, Room,
  assessment and evidence fields.
- The client cannot submit stay count, last-stay date or total spent.
- Rating and history remain protected personal data and are absent from audit
  metadata.
- No financial calculation, frontend form or unrelated module behavior was
  redesigned.
- All acceptance criteria and prerequisite documents are conformant.
