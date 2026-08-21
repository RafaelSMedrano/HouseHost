# Financial Transaction Plan Backend Plan

## Governing Specs

- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/specs/frontendSpecs/financialTransactionPlanAdministrativeExperienceSpec.md`

## Technical Direction

Implement `FinancialTransactionPlan` inside the financial transaction
submodule as an aggregate root that owns the validated direct composition and
coordinates its derived lifecycle. Preserve the existing
`FinancialTransaction`, `InstallmentPlanTransaction` and
`InstallmentTransaction` models as financial identities with their current JPA
inheritance strategy.

The implementation sequence first aligns transaction classification and source
relationships, then corrects cashier scheduling semantics, creates the FTP
domain and persistence, exposes its application contracts, integrates booking
creation and finally implements atomic materialization from check-in and
checkout.

No task may expose a partially implemented public payment contract. All new
HTTP endpoints remain authenticated administrative endpoints.

## Architecture And Ownership

`FinancialTransactionPlan` belongs to:

```text
src/main/java/com/househost/finance/financialtransaction/
```

Use these roles:

- `FinancialTransactionPlan` (domain/model; aggregate root);
- `FinancialTransactionPlanStatus` (domain/model; derived lifecycle enum);
- `FinancialTransactionPlanService` (application/service; principal FTP use
  case implementation);
- `FinancialTransactionPlanValidationService` (application/service; command,
  lifecycle and composition validation);
- `FinancialTransactionPlanReplacementService` (application/service; atomic
  destructive replacement responsibility);
- `FinancialTransactionPlanUseCase` (application/port/in; authenticated FTP
  commands and queries);
- `FinancialTransactionPlanParticipationUseCase` (application/port/in; narrow
  attach, detach and settlement callbacks used by the PLAN source adapter);
- `FinancialTransactionPlanPersistencePort` (application/port/out; aggregate
  persistence and locking contract);
- `FinancialTransactionPlanJpaEntity` (adapter/out/persistence/entity; JPA
  representation);
- `FinancialTransactionPlanPersistenceAdapter` (adapter/out/persistence;
  mapper and repository coordination);
- `FinancialTransactionPlanController` (adapter/in/rest; authenticated HTTP
  adapter);
- `FinancialTransactionPlanSourceAdapter` (adapter/out/integration; translates
  `FinancialTransactionSource` callbacks into the narrow participation use
  case).

The principal FTP service does not call Booking, CheckIn, CheckOut, Cashier or
Audit services directly. Financial transaction effects continue through the
single `FinancialParticipantNotifier`, specialized Resolvers and ports.

Booking, check-in and checkout initiate FTP behavior through financial inbound
use cases. They do not construct or persist FTP entities directly.

## Align Transaction Classification

Replace the transitional transaction type vocabulary with the authoritative
values:

```text
STANDARD
PLAN_DOWN_PAYMENT
PLAN_CHECK_IN_PAYMENT
PLAN_CHECK_OUT_PAYMENT
PLAN_TRANSACTION
INSTALLMENT_PLAN_BLOCK
INSTALLMENT_TRANSACTION
```

Migrate existing stored values without inventing a payment purpose:

- `STANDARD` remains `STANDARD`;
- `PLAN_SIGNAL_TRANSACTIONAL` becomes `PLAN_DOWN_PAYMENT`;
- `PLAN_TRANSACTIONAL` becomes `PLAN_TRANSACTION`;
- a row represented by `installment_plan_transactions` becomes
  `INSTALLMENT_PLAN_BLOCK` unless a later FTP command assigns a specific
  purpose;
- a row represented by `installment_transactions` becomes
  `INSTALLMENT_TRANSACTION`.

Add `PLAN` to `FinancialTransactionSourceType`. Preserve `INSTALLMENT` for the
internal installment-to-block source chain.

An `InstallmentPlanTransaction` receives its direct purpose type explicitly.
Its internal installments always use `INSTALLMENT_TRANSACTION`. Setting the
outer block source must not copy `PLAN` to internal installments. After the
block has an ID, every internal installment uses `INSTALLMENT` and the block ID
while preserving its mandatory structural association.

Update compatibility migration, DTO serialization, profile labels, tests and
legacy guards together. Do not temporarily accept arbitrary client-provided FTP
types.

## Correct Cashier Temporal Semantics

Cashier movements distinguish:

- record creation timestamp;
- expected `dueDate` copied from the financial transaction;
- actual `settlementDate` set when settlement changes money on hand.

Map or migrate existing `entryDate` and `expenseDate` into an explicit expected
date without claiming they are historical settlement dates. Add nullable
settlement dates and populate them only for future settlements unless reliable
legacy evidence exists.

Waiting scheduling updates only expected projections. Waiting reversal removes
only those projections. Settlement updates `cashOnHand`, inflow or outflow and
the actual settlement date. Replacement is allowed only for an unsettled
transaction, so its complete cashier cycle is neutral to realized balances.

Keep scheduling idempotent by transaction ID and cashier ID. Lock the affected
Cashier aggregate during projection mutation so concurrent schedules,
settlements or reversals cannot lose balance updates.

## Define The FTP Domain

The FTP constructor receives:

- stable plan identity when restoring or after identity allocation;
- sender and receiver pair;
- external source pair;
- complete initial direct transaction list;
- `planDueDate`;
- description.

It defensively copies, validates, orders and derives total plus status. Domain
operations include the additions, removals, replacement and queries named by
the governing spec. No generic setters expose status, total, settlement date or
the internal list.

Use exact decimal arithmetic. Count a direct `InstallmentPlanTransaction` once
for totalization and expand only its internal installments for settlement
queries. Derive status using the authoritative precedence. Derive
`planSettlementDate` from the latest individual settlement only when fully
settled.

Reject component participant mismatch, invalid source membership, duplicate
identity, due date after `planDueDate`, direct internal installment membership,
settled mutation and invalid cancellation or deletion.

## Persistence Model

Create `financial_transaction_plans` with at least:

- generated ID;
- sender type and ID;
- receiver type and ID;
- external source type and ID;
- derived total amount;
- derived status;
- plan due date;
- nullable plan settlement date;
- description;
- optimistic version;
- creation and update timestamps.

Direct membership remains identified by transaction `sourceType = PLAN` and
`sourceId = plan.id`. Add a nullable stable plan-component order field to the
financial transaction persistence representation so equal due dates preserve
insertion order. The field is valid only for direct PLAN members.

Provide bounded repository queries for direct components and complete aggregate
loading. Avoid one query per installment. Use a lock-bearing aggregate lookup
for replacement, deadline extension, cancellation and deletion.

Allocate the FTP identity inside the same database transaction before member
transactions require that identity. An uncommitted header may be inserted to
obtain the ID, but the operation commits only after the complete aggregate and
all invariants are persisted.

Do not migrate historical installment plans into FTPs automatically. Existing
fixed plans continue to work independently until explicitly used as a valid new
FTP component.

## Idempotence And Concurrency

Reservation-plan creation and scheduled-payment replacement accept an
idempotence key generated by the administrative client. Persist command outcome
under a unique key scoped to operation and authenticated actor or owning
aggregate.

For a repeated completed key, return the existing authoritative result. For an
in-progress or conflicting key, return a conflict without producing another
plan or definitive transaction.

Use optimistic versioning for ordinary FTP updates and a persistence lock for
destructive replacement. Confirm that the submitted old transaction is still
the direct member for the expected payment purpose before notifying any
participant.

## FTP Application Contracts

Place HTTP and use-case transfer objects in `application/dto` with `DTO`
suffixes. Use internal immutable carriers only in `application/records` with
`Record` suffixes and record-suffixed identifiers.

Expose authenticated contracts for:

- create a reservation FTP from one complete financial definition;
- find a minimized FTP by booking for operational work;
- find the scheduled component for check-in or checkout;
- retrieve a complete FTP profile for management;
- extend `planDueDate` for management;
- cancel or physically delete an eligible plan for management;
- atomically replace one scheduled component;
- reconcile a previous idempotent command outcome.

Commands contain operator choices but not freely supplied derived IDs,
participants, sources or status. Backend services derive those properties from
the owning booking and FTP.

Use one coherent reservation-form request with nested payment allocation. The
response returns the saved booking plus minimized FTP summary required by the
frontend.

## Participant And Source Flow

Extend `FinancialTransactionSource` with an optional creation callback and make
`FinancialParticipantNotifier.notifyCreation` invoke sender, receiver and then
source when present. Existing sources may keep the default no-op callback.

The PLAN source adapter attaches, detaches and refreshes direct members through
`FinancialTransactionPlanParticipationUseCase`. These callbacks are narrow and
idempotent; they never call the complete replacement command and therefore do
not recurse.

Replacement order is:

1. lock and validate FTP, old member and idempotence key;
2. notify deletion for old sender, receiver and PLAN source;
3. delete old transaction and its transactional participant effects;
4. create and persist the definitive transaction or installment block;
5. assign PLAN source to the direct transaction and INSTALLMENT source to
   internal installments;
6. notify creation for new sender, receiver and PLAN source;
7. validate and persist the refreshed FTP;
8. mark idempotence outcome;
9. commit;
10. produce only the definitive transaction creation audit event.

Any failure before commit rolls back every database-backed step. A stale or
repeated request does not create another transaction.

## Audit Behavior

Provisional FTP component creation uses an internal financial creation path
that performs persistence and participant notification without recording a
creation audit event. Destructive removal and participant cleanup during
replacement also produce no audit event.

Only the definitive transaction creation produces the applicable normal audit
event. Its entity ID and metadata describe only the new transaction and do not
store the old ID or a replacement link.

Publish that audit command only after the financial transaction commits so an
independent `REQUIRES_NEW` audit transaction cannot survive a rolled-back
replacement. Preserve the existing observable audit-failure behavior.

## Reservation Integration

`BookingFormService` (application/service; booking form orchestration) maps the
authenticated nested allocation into one financial inbound command after the
booking has an ID. Reservation and FTP creation share one transaction boundary.

No allocation purpose begins implicitly. Signal, check-in and checkout are
explicit. The backend recalculates reservation total, validates exact cent
allocation and derives participants, external BOOKING source and direct PLAN
sources.

A received signal creates its definitive simple or installment transaction and
settles it through the applicable financial flow. A scheduled signal remains
waiting. Check-in and checkout allocations create provisional waiting ordinary
transactions with their specific types and due dates.

## Check-In And Checkout Integration

Check-in and checkout request contracts accept an optional payment
materialization definition and idempotence key when the booking has a scheduled
component for that purpose.

The source operational service calls the financial replacement use case; it
does not delete and create transactions itself. A completed operation that
requires scheduled payment cannot report financial materialization success
until the atomic financial command succeeds.

When no component is scheduled, the existing operational flow continues
without fabricating a zero-value transaction. Extra checkout charges remain
outside the scheduled FTP component.

## Authorization

Reuse existing role groups:

- operational roles may create reservation FTPs, read minimized booking-owned
  summaries and materialize check-in or checkout payments;
- management roles may access complete FTP profiles, extend deadlines, cancel
  or physically delete eligible plans;
- no public endpoint or anonymous access is added.

Controller tests and security integration tests prove the backend restriction.
Frontend hiding is not considered authorization evidence.

## Verification

Backend verification covers:

- type and source migration;
- domain construction, ordering, totalization and immutable copies;
- settlement-view expansion and status precedence;
- deadline, cancellation, deletion and installment limits;
- persistence round trips and bounded aggregate queries;
- booking plus FTP atomic creation;
- received and scheduled signal behavior;
- check-in and checkout scheduled-component lookup;
- simple and installment replacement;
- rollback at every participant boundary;
- concurrency and idempotent replay;
- unchanged realized cashier balances;
- due and settlement dates;
- source callback ordering and absence of recursion;
- exactly one definitive creation audit and no provisional/deletion audit;
- operational and management authorization;
- module architecture and legacy classification removal;
- full Maven suite and `git diff --check`.

