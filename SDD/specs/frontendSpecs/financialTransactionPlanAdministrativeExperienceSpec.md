# Financial Transaction Plan Administrative Experience Spec

## Specification

Financial Transaction Plan Administrative Experience is the authenticated
administrative interface capability used to configure, review and perform the
reservation payments represented by a `FinancialTransactionPlan` (`FTP`).

The experience separates three independent payment purposes: signal, check-in
payment and checkout payment. Each purpose has its own allocated amount and may
ultimately be represented by an ordinary `FinancialTransaction` or an
`InstallmentPlanTransaction`, according to the financial structure selected
when that payment is performed.

The frontend provides conditional controls, immediate calculation feedback and
a comprehensible preview. It is not the authority for financial totals,
transaction identity, participants, source relationships, eligibility for
replacement or installment generation. The authenticated backend recalculates
and validates every submitted financial definition.

## Scope

This spec governs the initial authenticated administrative experience for:

- configuring a reservation FTP while creating a reservation;
- configuring an optional signal and its payment structure;
- allocating reservation value for future payment at check-in, checkout or
  both moments;
- continuously presenting the reservation total, allocated amount and
  remaining amount;
- loading the payment scheduled for the current check-in or checkout;
- selecting the definitive simple or installment structure only when that
  future payment is performed;
- requesting atomic replacement of the scheduled transaction and presenting
  the definitive transaction returned by the backend;
- preserving the unsaved reservation form when the active administrative
  navigation flow temporarily opens a related record or form;
- presenting loading, validation, success and failure outcomes accessibly.

The experience belongs only to the authenticated administrative application.
It does not add payment customization to the public booking API or public
website and does not authorize public guest self-service payment management.

This initial spec does not define public payment, payment-provider settlement
or unrestricted historical-plan editing. Exact component structure, API helper
names and request classes belong to the frontend technical plan. This spec
depends on the governing financial spec for domain and lifecycle behavior and
must not silently redefine those rules.

## Capabilities

### Configure Payment Allocation During Reservation Creation

The reservation-creation form presents one financial section that keeps the
payment allocation understandable as a complete agreement rather than as one
ambiguous payment field.

The section continuously displays:

- the authoritative reservation-total preview available to the frontend;
- the amount allocated to a signal;
- the amount allocated to check-in;
- the amount allocated to checkout;
- the total amount currently allocated;
- the amount that remains to be allocated.

The interface updates this preview whenever the reservation price, discount or
one of the payment allocations changes. A frontend preview never replaces the
backend calculation. The reservation is confirmed only after the backend
accepts that the complete allocation equals the reservation total.

The experience must not represent signal, check-in and checkout through one
combined “paid amount” field or one global installment selection. Payment
purpose, concrete payment structure and settlement state are separate user
decisions.

### Configure An Optional Signal Independently

The operator may enable an optional signal and define its amount. The signal is
the only one of the three initial payment purposes whose concrete structure may
be selected during reservation creation, because it may be performed at that
moment.

The signal controls allow the operator to indicate:

- signal amount;
- payment method when the payment is being performed;
- simple or installment payment structure;
- installment quantity when installment payment is selected;
- payment date or other date required by the governing financial contract;
- whether the signal has actually been received or remains scheduled.

Installment controls remain hidden or unavailable while simple payment is
selected. Selecting an installment structure for the signal does not set the
structure or installment quantity of check-in or checkout payments.

Disabling a configured signal removes its allocation from the preview only
after the interface makes that consequence clear. It must not leave hidden
signal values contributing to the submitted total.

### Program Check-In And Checkout Payments Without Premature Structure

The operator may allocate the remaining reservation value to check-in,
checkout or both. Each enabled purpose has an independent amount.

Reservation creation defines the amount and expected payment moment for these
future purposes. It does not ask whether a future check-in or checkout payment
will be simple or installment-based and does not ask for an installment
quantity at that time.

The check-in allocation uses the reservation check-in date as its expected
payment context. The checkout allocation uses the reservation checkout date.
Changing reservation dates refreshes the visible payment context and causes the
backend to revalidate the submitted plan.

When both purposes are enabled, their amounts are independently editable and
their sum participates in the same remaining-amount calculation. Disabling one
purpose removes its allocation and exposes the resulting amount as remaining
unless another enabled purpose receives it.

### Keep The Financial Preview Exact And Understandable

The frontend performs monetary preview calculations in decimal cents and does
not rely on imprecise floating-point equality to decide whether allocation is
complete.

The interface distinguishes at least these outcomes:

- value still remains to be allocated;
- allocation exactly matches the reservation total;
- allocation exceeds the reservation total;
- reservation total is unavailable because required reservation data is
  incomplete or invalid;
- backend recalculation disagrees with the submitted preview.

Color is not the only indicator. Every outcome has visible text, and invalid
controls expose an accessible error relationship. The confirmation action must
not present an apparently successful state while the known frontend allocation
is incomplete or excessive.

Backend validation remains authoritative. A client-side accepted composition
may still be rejected because of concurrent changes, authorization, lifecycle,
date or domain invariants.

### Submit One Coherent Reservation Command

The frontend submits the reservation and its complete financial definition as
one coherent authenticated command. It does not create the reservation, FTP
and individual transactions through independent client-orchestrated requests.

The client submits administrative choices, not protected or derived financial
identity. It must not freely choose transaction IDs, participant IDs,
`sourceType`, `sourceId`, internal transaction type values or derived plan
status. Those values are established by the backend from the reservation, FTP
and payment purpose.

While submission is pending, the confirmation control is disabled and the
form exposes a busy state. A retry after an uncertain network outcome must use
the idempotence contract later defined by the backend plan rather than creating
a second reservation or duplicate financial plan.

### Materialize Check-In Payment In The Check-In Experience

When a reservation has an eligible payment scheduled for check-in, the
check-in form loads and presents that payment purpose before the operation is
completed.

The financial section presents at least:

- payment purpose;
- scheduled amount;
- expected date;
- current financial status;
- selected definitive payment method;
- choice between simple and installment structure;
- installment quantity and calculated installment preview when applicable.

The installment choice belongs to this check-in payment only. Selecting it
does not alter a signal or checkout payment.

The frontend requests one atomic backend replacement. It must never issue one
request to delete the scheduled transaction followed by another request to
create the definitive transaction. The interface shows success only after the
backend returns the definitive transaction and updated plan state.

### Materialize Checkout Payment In The Checkout Experience

When a reservation has an eligible payment scheduled for checkout, the
checkout form follows the same atomic materialization behavior as check-in.

The checkout experience keeps the scheduled FTP payment distinct from extra
charges or another operational pending amount. It must not silently merge
unrelated charges into the scheduled component or treat a generic “pending
amount paid” flag as sufficient evidence that the FTP transaction was
materialized.

Simple and installment controls apply only to the scheduled checkout purpose.
Extra charges follow their own governing financial behavior unless a later
product-spec change explicitly adds them to the FTP.

### Present The Installment Preview Without Becoming Its Authority

When installment payment is selected for a signal, check-in or checkout, the
interface presents the expected installment quantity, values and due dates
available from the current input or a backend preview contract.

The preview clearly identifies any residual-cent adjustment and confirms that
the installment sum equals the payment-purpose amount. The frontend does not
invent a persisted installment identity or assume that its preview is the
created result.

After confirmation, the interface replaces the preview with the definitive
installment data returned by the backend. A backend validation difference is
shown as a validation outcome and does not leave the old scheduled transaction
appearing deleted in the interface.

### Preserve Form State Through Administrative Navigation

Opening a related guest, reservation or financial record from the active
reservation flow preserves the applicable unsaved form state in memory so the
operator can return without re-entering the financial allocation.

The preserved state includes enabled payment purposes, monetary values,
selected signal structure, applicable method and installment inputs, along
with the reservation fields required to recalculate the preview.

Cancel, successful save and deliberate abandonment remain distinct actions.
Successful save must not create a back-history entry that reopens the obsolete
unsaved form. The experience follows the navigation rules defined by
`administrativeNavigationHistorySpec` and does not persist the financial form
in `localStorage`, `sessionStorage` or console logs.

### Preserve Authorization Boundaries

Frontend visibility follows the permissions eventually assigned to FTP
creation, payment materialization, deadline change, cancellation and financial
viewing. Hidden or disabled controls do not replace backend authorization.

When an operation is no longer authorized, the interface refuses it safely,
preserves no misleading optimistic result and presents an authorized fallback.
Previously loaded financial information does not grant permission to mutate a
plan after the user's role or the plan state changes.

### Provide Explicit Loading, Empty, Conflict And Failure States

The experience distinguishes:

- loading the scheduled payment;
- no payment scheduled for the current purpose;
- payment already materialized;
- payment no longer eligible for replacement;
- concurrent plan change;
- backend validation failure;
- authorization failure;
- network or server failure;
- successful materialization.

The frontend does not optimistically remove the old transaction before backend
success. On failure, it keeps or reloads the authoritative plan state. Conflict
responses prompt a refresh rather than resubmitting stale identifiers.

Errors are associated with their relevant section and summarized accessibly.
A toast alone is not sufficient for a financial validation failure. Pending
controls prevent accidental duplicate submission without trapping keyboard
focus.

### Maintain Accessible And Responsive Financial Controls

Every field has a visible label and accessible name. Conditional sections
preserve logical focus order, and revealing installment fields moves no focus
unexpectedly. Error summaries can focus the first invalid control.

Payment-purpose groups use semantic headings or fieldsets. Status, allocation
completion and validation do not rely only on color. Monetary values use
Brazilian presentation while requests preserve the exact backend decimal
contract.

The complete allocation remains understandable on narrow screens. Summary
values do not disappear when fields stack vertically, and primary confirmation
remains distinguishable from cancellation or destructive replacement.

### Protect Financial And Personal Data In The Client

The interface displays only financial and participant information required for
the authorized operation. It does not write FTP composition, transaction IDs,
guest identity, payment method details or validation payloads to console logs
or persistent browser storage.

The frontend never collects card number, security code, bank credential or
other payment credential merely because an installment structure is selected.
Payment method classification is not direct card processing.

### Define Initial Labels, Defaults And Validation Feedback

Signal, check-in and checkout allocations begin disabled. The interface does
not assign an unallocated remainder automatically. The operator explicitly
enables each purpose and provides its value.

The initial visible labels are:

- `Sinal`;
- `Pagamento no check-in`;
- `Pagamento no checkout`;
- `Total da reserva`;
- `Total alocado`;
- `Valor restante`;
- `Pagamento já recebido`;
- `Forma de pagamento`;
- `Pagamento à vista`;
- `Pagamento parcelado`;
- `Quantidade de parcelas`.

Help text explains that check-in and checkout values are scheduled now and
their definitive structure is chosen only when the payment occurs. Validation
uses direct messages for missing allocation, excess allocation, invalid amount,
missing installment quantity, deadline conflict, stale plan and unauthorized
operation.

The interface exposes from two through twelve installments. Initial due dates
are calculated and read-only. Changing those dates requires a later product
capability. The frontend calculates an immediate cent-precise preview and
reconciles it with the backend response; the backend result is definitive.

### Define Initial Plan Presentation And Actions

An existing reservation presents a read-only FTP summary with total, allocated
purposes, current status, deadline, settled amount, outstanding amount and the
definitive transactions the current role may view. It does not provide a
generic editor for historical composition.

Operational roles may create the initial allocation and materialize eligible
check-in or checkout payments. Management-only controls cover deadline
extension, cancellation, physical deletion of an eligible unsettled plan and
full financial-profile navigation.

The status labels are `Ativo`, `Parcialmente pago`, `Em atraso`, `Liquidado`
and `Cancelado`. A settled FTP exposes no editing action. A partially settled
or overdue FTP exposes only actions allowed for its eligible unsettled
components.

When no payment is scheduled for the current check-in or checkout purpose, the
form states that no FTP payment is required and permits the operational flow to
continue without materialization. It must not fabricate a zero-value payment.

Before destructive replacement, the interface presents a confirmation that
identifies the purpose, amount and definitive structure to be created. It does
not expose the removed provisional transaction as retained history after
success.

Reservation-date changes refresh the preview and are submitted for backend
validation. A change that conflicts with an immutable, settled or overdue
financial fact is rejected rather than silently rewriting the plan.

### Recover From Uncertain Submission Outcomes

Reservation creation and payment replacement use backend-supported idempotence.
After a timeout or interrupted response, the frontend reloads the reservation,
FTP and relevant payment purpose before allowing another command.

If the backend already committed, the interface presents the authoritative
created or replaced state. If it did not commit, the preserved form remains
available for a deliberate retry. The client never assumes failure merely
because the response was lost and never retries a destructive replacement with
a stale transaction identifier.

### Preserve The Defined Initial Frontend Boundary

The decisions required for an initial frontend plan are resolved by this spec.
The technical plan defines component decomposition, API helper names, request
mapping, tests and delivery order without changing these product decisions.

Public payment, editable installment due dates, unrestricted historical-plan
editing and provider-receivable presentation remain outside this initial scope.

## Prerequisite Specs

- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`

## Spec Degree

3.
