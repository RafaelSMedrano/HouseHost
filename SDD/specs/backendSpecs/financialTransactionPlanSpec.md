# Financial Transaction Plan Spec

## Specification

Financial Transaction Plan, abbreviated as `FTP`, is a financial domain model
that organizes a customized financial flow. Each direct component of a plan is
represented by one `FinancialTransaction` (`FT`) or by an eligible specialized
domain model that inherits from `FinancialTransaction`.

`InstallmentPlanTransaction` is an eligible direct component and represents a
fixed installment block inside the customized flow. Its
`InstallmentTransaction` instances remain internal installments of that block;
they are not added independently as direct FTP components.

Unlike a fixed installment generator, an FTP permits its transactions to be
modeled individually. The transactions in the same plan may therefore use
different concrete `FinancialTransaction` types and may evolve according to
the rules of their respective domain models.

`FTP` and `FT` are documentation abbreviations. Product contracts and future
implementation types use their complete names.

## Scope

This spec governs the product meaning and initial invariants of
`FinancialTransactionPlan` within the financial transaction submodule.

It covers:

- representing a customized installment flow as a plan;
- composing the plan from `FinancialTransaction` instances or eligible child
  models, including `InstallmentPlanTransaction`;
- preventing an `InstallmentTransaction` from being added independently as a
  direct plan component while allowing it to belong to the broader FTP flow
  through its `InstallmentPlanTransaction`;
- identifying plan membership through the existing financial transaction
  source mechanism;
- preserving the normal behavior and specialized rules of every transaction
  included in the plan;
- replacing a scheduled ordinary check-in or checkout transaction with the
  definitive transaction structure selected when that payment is performed;
- keeping the new plan concept distinct from the current fixed installment
  model while defining how both participate in the same flow.

This spec does not define public guest self-service payment, payment-provider
receivables, complete card-processing credentials or migration of historical
fixed installment contracts into FTPs. Exact classes, DTO shapes, persistence
mapping and endpoint paths belong to the governing technical plans.

## Capabilities

### Represent A Customized Financial Flow

An FTP groups the financial transactions that together represent one
customized financial flow. A plan is not limited to generating equal values,
repeating one due-date rule or using a single concrete child type.

Each direct component is a complete `FinancialTransaction` or an instance of
an eligible specialized child model. It retains the financial attributes and
behavior applicable to its concrete type, including its own amount,
participants, dates, method, status and description when those fields apply.

The FTP does not flatten specialized transactions into a generic installment
record and does not bypass validation belonging to `FinancialTransaction` or
its child model.

### Configure Reservation Payments Administratively

The initial reservation-payment customization belongs to the authenticated
administrative reservation-creation experience. It does not add payment fields
or payment customization to the public booking API.

The administrative form displays installment quantity only for a payment being
configured with an installment payment option. It may also configure:

- an optional signal or down payment;
- an amount allocated for payment at check-in;
- an amount allocated for payment at checkout;
- both check-in and checkout amounts when the remaining value is split between
  those moments.

Entering a signal opens an FTP when one does not already exist. Configuring a
check-in or checkout allocation opens an FTP or adds the corresponding direct
transaction to the existing FTP. The interface continuously displays the
reservation total, every configured amount and the value that remains to be
allocated.

The frontend provides conditional fields and immediate calculation feedback,
but it is not the authority for financial values. The backend recalculates and
validates the complete composition, rejects negative or excessive amounts and
does not accept a reservation payment plan whose allocation differs from the
reservation total.

A signal is a payment configured at reservation creation. It may be represented
by an ordinary `FinancialTransaction` when paid without installments or by an
`InstallmentPlanTransaction` when that signal itself is paid in installments.
Its installment quantity is known at reservation creation when installment
payment is selected for the signal.

Check-in and checkout allocations represent future payment moments. Reservation
creation defines their amounts and expected dates, but does not decide whether
those future payments will be made in installments. The installment choice and
quantity are defined only when the corresponding payment is actually performed
at check-in or checkout.

Consequently, signal, check-in payment and checkout payment are independent
payment purposes. Each purpose may ultimately be represented by either an
ordinary `FinancialTransaction` or an `InstallmentPlanTransaction`.

### Separate Payment Purpose From Concrete Structure

`FinancialTransaction.type` must not represent whether an amount is an entry or
expense for a specific participant. The FTP model must classify the business
role of each transaction through `FinancialTransaction.type`. A separate
FTP-component role property is not required.

A payment purpose and its concrete transaction structure are different
dimensions. For example, a signal can be represented by either a simple
`FinancialTransaction` or an `InstallmentPlanTransaction`. The concrete Java
domain model already distinguishes a simple transaction from an installment
block. Therefore, an `InstallmentPlanTransaction` created for a specific
payment purpose uses that purpose type, while its concrete class identifies its
installment structure.

The official `FinancialTransaction.type` vocabulary for the FTP model is:

- `STANDARD`: a transaction outside an FTP;
- `PLAN_DOWN_PAYMENT`: a signal or down payment;
- `PLAN_CHECK_IN_PAYMENT`: a payment performed at check-in;
- `PLAN_CHECK_OUT_PAYMENT`: a payment performed at checkout;
- `PLAN_TRANSACTION`: another direct transaction of a customized plan;
- `INSTALLMENT_PLAN_BLOCK`: a direct fixed installment block without a more
  specific signal, check-in or checkout purpose;
- `INSTALLMENT_TRANSACTION`: an internal `InstallmentTransaction` owned by an
  `InstallmentPlanTransaction`.

An `InstallmentPlanTransaction` that represents a signal, check-in payment or
checkout payment uses `PLAN_DOWN_PAYMENT`, `PLAN_CHECK_IN_PAYMENT` or
`PLAN_CHECK_OUT_PAYMENT`, respectively. Its class remains the authority for
identifying it as an installment block. `INSTALLMENT_PLAN_BLOCK` is reserved
for a fixed installment block whose direct FTP role is not one of those three
specific payment purposes. Every internal installment uses
`INSTALLMENT_TRANSACTION` and does not inherit the direct component's purpose
type.

The use case or domain operation that creates the transaction assigns its
business-role classification according to that context. A caller must not
freely claim a plan role without the corresponding relationship, and the role
must not be changed independently after creation.

Entry and expense direction remains derived from the sender and receiver
positions and must not be duplicated as separate amount properties on
`FinancialTransaction`.

### Customize Each Direct Component

Every direct FTP component owns its individual financial definition. The FTP
may therefore combine components with different:

- amounts;
- `transactionDate` values;
- `dueDate` values;
- payment methods;
- descriptions;
- eligible business-role classifications;
- concrete `FinancialTransaction` child models.

`dueDate` represents the expected payment date. `settlementDate` represents
the date on which settlement actually occurs and must not be preconfigured as
a planned payment date.

The FTP keeps its direct components ordered by `dueDate` in ascending order.
When multiple components have the same `dueDate`, their insertion order is the
stable tie-breaker. Adding a component or changing an allowed component due
date must preserve this ordering invariant.

When every installment requires individual dates, values or roles, the FTP
uses individually configured direct transactions. When a group follows one
fixed installment rule, the FTP may use one `InstallmentPlanTransaction` as a
direct component instead of flattening its internal installments.

### Compose Fixed Installment Blocks Without Flattening Them

An `InstallmentPlanTransaction` may be added directly to an FTP as one
financial component. It preserves its role as the owner and coordinator of its
internal `InstallmentTransaction` instances.

Its `FinancialTransaction.type` follows the classification defined above: a
purpose-specific block uses the corresponding plan payment type, and a generic
fixed installment block uses `INSTALLMENT_PLAN_BLOCK`. Each internal
`InstallmentTransaction` uses `INSTALLMENT_TRANSACTION`.

An `InstallmentTransaction` must not be added independently to the FTP's direct
component collection. It participates in the broader FTP flow only through its
owning `InstallmentPlanTransaction`.

This distinction produces two forms of belonging:

```text
FinancialTransactionPlan
    <- direct component: InstallmentPlanTransaction
        <- internal installment: InstallmentTransaction
```

An internal installment belongs indirectly to the broader FTP flow when its
owning `InstallmentPlanTransaction` is a direct FTP component. It does not
duplicate a direct FTP association because the ownership chain already
provides an unambiguous path to the plan.

The association from `InstallmentTransaction` to
`InstallmentPlanTransaction` is mandatory and structurally identified by the
installment-plan key. The inverse installment collection belongs to the
`InstallmentPlanTransaction`. Removing or changing this relationship must not
leave an internal installment with no owning installment plan.

### Identify The Plan Before Associating Its Transactions

Every FTP has a stable identifier. A transaction can be associated with a plan
only after that identifier is available, or through an equivalent atomic flow
that guarantees the same final invariant.

The mechanism used to obtain and persist the identifier is an implementation
decision for a future plan and task.

### Define The Plan State

Every FTP has these attributes:

- `id`: stable identifier of the plan;
- `senderType`: type of the participant responsible for sending or paying the
  plan values;
- `senderId`: identifier of that sender;
- `receiverType`: type of the participant that receives the plan values;
- `receiverId`: identifier of that receiver;
- `sourceType`: type of the external business or operational origin of the
  plan;
- `sourceId`: identifier of that external origin;
- `totalAmount`: total value derived from the direct components of the plan;
- `status`: global state of the plan;
- `financialTransactionList`: direct financial components of the plan, ordered
  by `dueDate`;
- `planDueDate`: contractual deadline for completing the plan's financial
  flow;
- `planSettlementDate`: date on which the complete plan is settled;
- `description`: description of the agreement or general purpose represented
  by the plan.

The domain exposes `financialTransactionList` through the `List` abstraction.
An `ArrayList` may be used as an internal implementation detail, but the plan's
contract does not depend on that concrete collection type.

The FTP participant pair represents the parties to the financial obligation
modeled by the plan. Every transaction created for the FTP must carry the same
`senderType`, `senderId`, `receiverType` and `receiverId` values as the plan.
The operation must reject or prevent a member whose participants differ from
the FTP participants.

The member transactions retain their participant fields as part of their own
complete financial state. Plan membership must not make a transaction
dependent on loading the FTP merely to identify its sender and receiver.

The FTP `sourceType` and `sourceId` preserve the plan's external origin, such
as a booking, stay or manual financial operation. They do not represent the
membership relation between the FTP and its transactions.

### Initialize The Plan From Its Transactions

The FTP constructor receives the complete initial
`financialTransactionList`. Construction must:

1. make a defensive copy of the received list;
2. validate every direct component and reject a duplicated transaction;
3. order the copied list by `dueDate`;
4. calculate `totalAmount` from the ordered direct components;
5. establish the initial plan state without exposing an inconsistent
   intermediate collection.

The caller does not provide `totalAmount` independently. The FTP derives it
from the transactions that actually compose the plan.

Reconstruction from persistence must restore the same component membership,
stable ordering and derived total without producing creation effects again.

### Derive The Plan Total

`totalAmount` is always the sum of the amounts of the current direct
components. It must be recalculated during construction and after every
authorized addition, removal or replacement.

An `InstallmentPlanTransaction` contributes its own total amount exactly once
to this calculation. Its internal `InstallmentTransaction` amounts must not be
added again because they decompose the value already represented by their
owning installment plan.

The stored or exposed `totalAmount` must never diverge from the current direct
component sum. Status follows the derived FTP lifecycle defined in this spec.

### Manage Direct Transactions Through The Plan

The FTP provides these domain operations for its direct component collection:

- `addFinancialTransaction` adds one validated direct transaction;
- `addFinancialTransactionList` adds a validated group of direct transactions;
- `removeFinancialTransaction` removes one eligible direct transaction;
- `replaceFinancialTransaction` atomically replaces one eligible direct
  transaction with another.

Each operation must validate the resulting collection, preserve participant,
source, type and due-date invariants, reject duplicates, reorder the list by
`dueDate`, recalculate `totalAmount` and refresh any derived plan state.

A fully settled FTP cannot receive, remove or replace components. A settled
transaction cannot be removed or replaced because doing so would rewrite
financial history. Active, partially settled, overdue and canceled states obey
the lifecycle and eligibility restrictions defined in this spec.

The collection is encapsulated by the FTP. Callers must not mutate the internal
list directly or bypass these operations.

### Replace A Scheduled Transaction Destructively

When a check-in or checkout payment was scheduled as an ordinary direct
transaction and its definitive payment structure is selected only when the
payment is performed, the FTP replaces the scheduled transaction rather than
changing its concrete domain type.

Replacement removes the old transaction physically and creates the definitive
transaction with a new identifier. The new transaction may be another ordinary
`FinancialTransaction` or an `InstallmentPlanTransaction`, according to the
payment structure actually selected. The old identifier is not preserved, and
the new transaction stores no replacement link or other domain relationship to
the removed transaction.

Before the old transaction is removed from financial persistence, its deletion
must pass through the financial participant flow. Every operational effect
previously created from that transaction in sender, receiver, source or any
other affected participant must be removed. No participant may retain a
movement, association, status effect or other active reference derived from the
removed transaction.

Creation of the definitive transaction then follows the normal financial
participant flow so that only effects belonging to the new transaction remain.
The FTP replaces the direct component, reorders `financialTransactionList` by
`dueDate`, recalculates `totalAmount` and refreshes its derived state.

The replacement is one application operation and must not expose a state in
which the old transaction has been removed but the definitive transaction and
its required participant effects were not created. Exact transaction-boundary,
rollback and failure-recovery mechanics remain technical decisions for the
future implementation plan.

A settled transaction remains ineligible for destructive replacement. The
only eligible source-transaction states are `WAITING` and `OVERDUE`, both with
no settlement date or partial realization. `SETTLED`, `CANCELED` or partially
realized transactions cannot be destructively replaced.

### Audit Only The Definitive Transaction Creation

The scheduled old transaction is a provisional representation of a future
check-in or checkout payment whose definitive structure is not yet known. Its
creation does not produce an audit event.

When that provisional transaction is replaced, its destructive removal, the
removal of its participant effects and the other internal steps of the
replacement cycle do not produce audit events. Only creation of the definitive
new `FinancialTransaction` or `InstallmentPlanTransaction` is audited through
the applicable normal creation event.

The definitive transaction's audit event identifies the new transaction and
its own necessary financial metadata. It does not store the removed
transaction's identifier, does not describe a replacement relationship and
does not preserve another audit link between the two transactions.

This rule does not delete, rewrite or suppress an audit record that already
exists. It governs generation of events by the future FTP flow. It is limited
to replacement of an unsettled provisional transaction under this spec and is
not a general exception for deletion of settled, historical or independently
material financial transactions.

### Keep Replacement Atomic, Idempotent And Cashier-Neutral

Destructive replacement is one atomic application transaction across the FTP,
the old and new financial transactions and every transactional participant
effect. Any failure restores the old transaction, its FTP membership and all
participant projections, while removing every incomplete effect of the new
transaction.

The operation is idempotent for the same authenticated replacement request.
Concurrent attempts cannot create two definitive transactions for the same
scheduled component. A stale request receives a conflict instead of acting on
the component that replaced the submitted identifier.

Because only an unsettled component is eligible, removing its cashier movement
changes only waiting and expected projections. Creation of the definitive
transaction schedules its own movement or the movements of its internal
installments. Neither step changes `cashOnHand`, `totalInflow` or
`totalOutflow`. Participant callbacks identify their effects by transaction
identity and remain safe under retry.

### Define The FTP Lifecycle

The FTP uses its own status vocabulary:

- `ACTIVE`: the plan has payable obligations, none is overdue and none has
  been settled;
- `PARTIALLY_SETTLED`: at least one payable obligation is settled, another
  remains payable and none is overdue;
- `OVERDUE`: at least one payable obligation is overdue, regardless of whether
  another obligation was already settled;
- `SETTLED`: every remaining payable obligation is settled;
- `CANCELED`: the plan was canceled before any obligation was settled.

Status is derived from the settlement view and cannot be freely assigned. The
precedence is `CANCELED`, `SETTLED`, `OVERDUE`, `PARTIALLY_SETTLED`, then
`ACTIVE`. The FTP does not need a persisted `DRAFT` state because construction
receives and validates its complete initial composition.

`planSettlementDate` is established from the latest settlement date only when
the FTP becomes `SETTLED`. A settled FTP and all its composition are immutable.
An active or partially settled FTP may replace or remove only components that
remain eligible and unsettled; settled component history cannot be rewritten.

The complete FTP may be canceled only before any component is settled. A plan
with settled history cannot be converted to `CANCELED`. An FTP with no settled
component may be physically deleted when no governing retention obligation
requires preservation. Once any component is settled, the FTP is retained and
closed through lifecycle state rather than physical deletion.

### Preserve The Contractual Plan Deadline

`planDueDate` is contractual and never extends automatically. Every direct
transaction and every internal installment must have a due date on or before
that deadline.

A proposed check-in or checkout installment structure whose final due date
exceeds `planDueDate` is rejected. An authorized management operation may
explicitly extend the deadline before replacement, subject to lifecycle,
reservation-date and retention rules. Deadline reduction cannot place an
existing payable component after the new deadline.

### Treat Initial Installments As Gradual Guest Obligations

The initial installment capability represents gradual settlement of the guest
obligation. Each internal installment remains payable until individually
settled and produces its corresponding cashier projection.

Card-provider acquisition, immediate terminal discharge of the guest debt and
future provider receivables are different financial facts and remain outside
this initial FTP scope. A later spec must model the provider as an appropriate
participant before those facts can replace gradual guest installments.

### Define Signal And Installment Boundaries

A signal actually received during reservation creation is created settled. A
signal only scheduled at that moment is created waiting. The administrative
command states which fact occurred; method selection alone does not imply
settlement.

An installment block contains from two through twelve installments. Values are
calculated in decimal cents. Equal base values use downward cent precision and
any residual cent amount is assigned to the final installment so the exact sum
matches the direct component amount.

### Preserve Administrative Authorization Boundaries

Existing operational roles may create a reservation FTP and materialize an
eligible check-in or checkout payment. Existing management roles may also
extend `planDueDate`, cancel or physically delete an eligible unsettled FTP and
access the complete financial profile.

Operational presentation may expose the minimized plan and payment-purpose
state required for reservation, check-in and checkout work. It does not grant
access to the unrestricted financial module. Backend authorization remains
authoritative for every command and query.

### Query Direct Plan Composition

The FTP provides read-only domain queries for its current direct composition:

- `getFinancialTransactionList` returns an immutable ordered copy;
- `findFinancialTransactionById` locates a transaction by identifier;
- `containsFinancialTransaction` checks membership by identifier;
- `getFinancialTransactionCount` returns the number of direct components;
- `findFinancialTransactionByOrder` locates a component by its current
  chronological order, using one as the first position.

A query that may have no matching transaction returns an optional outcome
instead of fabricating a component. These composition queries do not flatten
an `InstallmentPlanTransaction`; they expose that installment plan as the one
direct component stored by the FTP.

### Build The Settlement Transaction View

Payment and settlement queries use a derived chronological view of the
transactions that can be settled individually. The FTP builds this view
without changing its direct composition:

- an ordinary direct `FinancialTransaction` contributes itself;
- a direct `InstallmentPlanTransaction` contributes each of its internal
  `InstallmentTransaction` instances;
- the `InstallmentPlanTransaction` container does not also contribute itself,
  because doing so would duplicate the financial obligation represented by its
  installments.

For example:

```text
direct FTP composition: [transactionA, installmentPlanB, transactionC]
settlement view:        [transactionA, installmentB1, installmentB2,
                         installmentB3, transactionC]
```

The settlement view is ordered by `dueDate`. Stable direct-plan order and, for
internal installments, installment number resolve equal due dates. It is
read-only and derived from the current domain state.

`getSettlementFinancialTransactionList` returns an immutable copy of this
derived view. It does not alter `financialTransactionList` and does not make an
internal installment a direct FTP component.

### Query Settlement Transactions

The following payment-oriented queries operate on the expanded settlement
view, not only on the direct FTP composition:

- `getSettledFinancialTransactionList` returns individually settled
  transactions;
- `getUnsettledFinancialTransactionList` returns transactions that are not
  settled;
- `getOverdueFinancialTransactionList` returns transactions overdue on a
  supplied reference date;
- `getFinancialTransactionListDueOn` returns transactions due on one date;
- `getFinancialTransactionListDueBetween` returns transactions within an
  inclusive date interval;
- `findNextFinancialTransactionToSettle` returns the earliest payable unsettled
  transaction, including a specific internal `InstallmentTransaction` when it
  is next;
- `findLastSettledFinancialTransaction` returns the most recently settled
  transaction, including a specific internal installment when applicable.

A query that may have no matching transaction returns an optional outcome
instead of fabricating a transaction. `findNextFinancialTransactionToSettle`
must exclude canceled or otherwise non-payable transactions even though they
are not settled. `findLastSettledFinancialTransaction` compares
`settlementDate`; settlement-view order resolves equal dates.

The FTP also derives financial and schedule summaries from the settlement view
without mutating its transactions:

- `calculateSettledAmount`;
- `calculateOutstandingAmount`;
- `calculateOverdueAmount` for a supplied reference date;
- `calculateSettlementPercentage`;
- `isFullySettled`;
- `hasOverdueFinancialTransaction` for a supplied reference date;
- `getFirstDueDate`;
- `getLastDueDate`.

Canceled or otherwise non-payable transactions do not contribute to
outstanding or overdue amounts. `isFullySettled` becomes true only when every
remaining payable transaction in the settlement view is settled. This result
governs when `planSettlementDate` may be established.

The expanded settlement view is never used to calculate `totalAmount`.
Totalization continues to use only direct components, counting an
`InstallmentPlanTransaction` once by its total amount.

### Integrate The Settlement View With Cashier

The cashier must not register the `FinancialTransactionPlan` aggregate or its
`totalAmount` as a financial movement. Only individually settleable
transactions from the FTP settlement view produce cashier movements.

The integration follows this structure:

```text
FinancialTransactionPlan
    <- ordinary direct FinancialTransaction -> one cashier movement
    <- InstallmentPlanTransaction
        <- InstallmentTransaction -> one cashier movement per installment
```

An `InstallmentPlanTransaction` container does not produce an additional
cashier movement when its internal installments already represent the payable
amounts. Plan or installment-plan aggregate settlement must not duplicate the
effects produced by settling their individual transactions.

Adding an ordinary transaction to the FTP schedules its corresponding cashier
movement when the cashier is one of its financial participants. Adding an
`InstallmentPlanTransaction` schedules the movements of its internal
installments instead of scheduling the container total.

Removing an eligible unsettled transaction reverses its scheduled cashier
movement. Removing an eligible `InstallmentPlanTransaction` reverses the
scheduled movements of its internal installments. A settled movement is not
silently removed or reversed through ordinary FTP composition editing.

Settlement affects `cashOnHand` only for the specific ordinary transaction or
internal installment being settled. Updating the aggregate status of the FTP
or of an `InstallmentPlanTransaction` does not independently change
`cashOnHand`.

### Preserve Cashier Movement Dates By Meaning

A cashier movement produced from an FTP settlement transaction preserves three
different temporal facts:

- `createdAt`: timestamp at which the cashier movement record was created;
- `dueDate`: business date on which the source transaction is expected to be
  settled;
- `settlementDate`: business date on which the source transaction is actually
  settled.

Scheduling a waiting cashier movement copies the source transaction's
`dueDate`, leaves `settlementDate` empty and records `createdAt` through the
normal persistence lifecycle. The scheduling date must not replace the
transaction due date merely because the movement record is created on that
day.

Settling the individual transaction records its actual settlement date on the
cashier movement without overwriting the original due date. The effective
settlement date may use the current business date when settlement occurs if the
operation does not provide another authorized effective date; it must not be
captured prematurely while the movement is only waiting.

Consequently, a movement can demonstrate independently:

```text
when the record was created
when the value was expected
when the value was realized
```

Legacy cashier date fields must not remain semantically ambiguous between
creation, due date and settlement date. A future implementation plan must map
or migrate them to the explicit meanings above without destroying historical
dates.

### Distinguish Plan And Transaction Dates

`planDueDate` is an explicit contractual deadline. It is not silently derived
or rewritten from the latest component due date.

Every direct component must have an effective `dueDate` on or before
`planDueDate`. A component with a later due date must be rejected unless a
management operation explicitly changes the plan deadline before the component
is accepted.

`planSettlementDate` remains empty until the complete FTP is settled. When all
required financial obligations of the plan become settled, the FTP records the
actual date of that complete settlement. Individual components retain their
own `settlementDate` values, which may differ from the plan settlement date.

Plan status transitions and partially realized composition follow the derived
lifecycle rules defined in this spec.

### Use The Financial Source Contract For Membership

`FinancialTransactionSourceType` must support the value `PLAN`.

Every direct `FinancialTransaction` component created as part of an FTP,
including an `InstallmentPlanTransaction`, must have:

```text
sourceType = PLAN
sourceId = financialTransactionPlan.id
```

The `sourceId` stores the identifier of the FTP to which the direct component
belongs. A direct component created for a plan must not retain another source
type or another source identifier as its FTP-membership relation.

An internal `InstallmentTransaction` does not point directly to the FTP. Its
source relationship identifies its owning installment plan:

```text
sourceType = INSTALLMENT
sourceId = installmentPlanTransaction.id
```

This source pair reinforces, but does not replace, the mandatory structural
relationship between `InstallmentTransaction` and
`InstallmentPlanTransaction`.

Consequently, the complete source chain is:

```text
external business or operational source
    <- FinancialTransactionPlan.sourceType/sourceId
        <- direct FinancialTransaction sourceType=PLAN/sourceId=plan.id
            <- InstallmentTransaction
               sourceType=INSTALLMENT/sourceId=installmentPlanTransaction.id
```

The FTP preserves the external origin because each level uses its single source
pair for its immediate owner. A system locates the FTP of an internal
installment by traversing the installment's owning
`InstallmentPlanTransaction` and then that transaction's `PLAN` source.

A plan operation must not be considered successful if a direct component is
left without the FTP source pair, points to a different plan or contains an
internal installment whose owning-plan relationship is inconsistent.

### Preserve Transaction Autonomy Within The Plan

Membership in an FTP does not remove the identity of a transaction and does
not change it into a value-only installment. Each member remains a financial
transaction capable of following the behavior defined for its concrete model.

Rules that coordinate multiple transactions at plan level will be defined as
the FTP lifecycle is modeled. Until then, this spec does not imply automatic
status propagation, settlement propagation or value redistribution among plan
members.

### Integrate The Existing Installment Model

`FinancialTransactionPlan` is a new domain concept and is not another name for
the current `InstallmentPlanTransaction`.

`FinancialTransactionPlan` does not inherit from `FinancialTransaction`. A plan
coordinates financial transactions, whereas a financial transaction represents
an individual movement. Common participant, source or descriptive concepts may
be shared through composition or a suitable common abstraction without making
the two models substitutes for one another.

The two models coexist with different responsibilities:

- `FinancialTransactionPlan` composes the complete customized financial flow;
- `InstallmentPlanTransaction` represents one fixed installment block that may
  participate directly in that flow;
- `InstallmentTransaction` represents an internal installment of that fixed
  block, uses its structural key to reach that owner and does not participate
  as an independent direct FTP component.

This composition decision does not by itself define migration of existing
records or replacement of current contracts.

### Preserve The Defined Initial Boundary

The decisions required for initial backend planning are resolved by this spec.
Technical plans define exact DTOs, use cases, endpoints, persistence mapping,
locking and migration mechanics without changing the product behavior above.

Public guest self-service payment customization, provider receivables and
conversion of historical fixed installment records remain outside this initial
scope and require later product-spec review.

### Respect Financial Module Boundaries

Future FTP operations belong to the financial transaction submodule and must
follow the financial decoupling rules defined by `moduleArchitectureSpec`.
Other modules initiate financial behavior through financial use cases, and
financial effects directed to participants or origins cross the appropriate
ports, not concrete module internals.

The domain model remains independent from HTTP, JPA and framework concerns.
Future DTOs, services, ports, adapters, persistence entities and mappers must
follow their architectural roles when their contracts are specified.

### Protect Financial And Participant Data

An FTP and its transactions may reference participants and operational sources
that relate to identifiable people. Future contracts must expose, log and audit
only the information necessary for the authorized financial purpose.

Lifecycle and deletion rules must be modeled together with applicable
financial, accounting, tax, contractual, rights-protection and LGPD retention
requirements before implementation. This initial spec does not authorize
indefinite retention or deletion that destroys required financial history.

## Prerequisite Specs

- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`

## Spec Degree

2.
