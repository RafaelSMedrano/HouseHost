# Financial Transaction Plan Administrative Experience Frontend Plan

## Governing Specs

- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`
- `SDD/specs/frontendSpecs/financialTransactionPlanAdministrativeExperienceSpec.md`
- `SDD/plans/backendSpecs/financialTransactionPlanBackendPlan.md`

## Technical Direction

Extend the existing administrative SPA without introducing a frontend
framework or a parallel navigation system. Reuse the reservation,
operations-controller, API-client, permission and form-state patterns already
present in the administrative frontend.

Implement the frontend after the corresponding backend contract is available.
Do not emulate atomic replacement with separate delete and create requests.

## Reservation Allocation View

Refactor the payment section of `newReservationView.js` (frontend view; new
reservation form) from the current global method/installment fields into
purpose-based controls:

- financial summary;
- optional signal group;
- optional check-in allocation group;
- optional checkout allocation group.

All groups begin disabled. No remaining amount is assigned automatically.

The signal group controls received state, amount, method, simple/installment
structure, quantity from two through twelve and applicable date. Check-in and
checkout groups control only enabled state and allocated amount during
reservation creation.

Create focused pure calculation and mapping helpers rather than embedding all
cent arithmetic in event listeners. Convert user-entered decimal strings to
integer cents, calculate allocation and installment preview in cents and format
only for presentation.

The local summary renders total, allocated and remaining values plus explicit
complete, incomplete, excessive or unavailable text. Backend response remains
authoritative.

## Reservation Request And State

Replace the legacy single payment payload with a nested FTP allocation object
matching the backend contract. Do not send derived participant, source, status
or transaction identity.

Generate and retain an idempotence key for one reservation submission attempt.
Preserve it across an uncertain retry of the same command and replace it only
after authoritative success or deliberate form reset.

Include all FTP form controls in the existing in-memory reservation-form state
used when navigating to guest registration, guest profiles or another related
administrative screen. Do not use persistent browser storage.

Add API helpers to `api.js` (frontend HTTP/session integration module) for FTP
queries, replacement, deadline extension, cancellation and idempotent outcome
reconciliation as backend tasks make them available.

## Check-In Materialization

Extend `checkOperationFormView.js` (frontend operational forms view; check-in
form) with a payment section that loads the minimized scheduled check-in
component by booking.

Represent loading, no scheduled payment, eligible payment, already
materialized, conflict and failure states. For an eligible component, display
purpose, amount, due date and status, then collect method and
simple/installment structure. Reveal quantity and read-only preview only for
installments.

Submit check-in and replacement as the coherent backend contract defines. The
frontend never removes the provisional transaction optimistically. Disable
submission while pending and use the command idempotence key for recovery.

Present a confirmation before replacement, naming purpose, amount and selected
structure. After success, render only the definitive returned transaction and
updated FTP state.

## Checkout Materialization

Apply the same materialization component to checkout without merging it with
extra charges, generic pending amount or rating state.

The scheduled checkout payment has its own status, method and structure. Extra
charges and `pendingAmount` preserve their existing contracts. Completed
checkout rating behavior remains unchanged.

Reuse shared cent, installment-preview, state and error helpers between
check-in and checkout. Avoid duplicating complete payment orchestration in both
view binders.

## Plan Summary And Permissions

Add a read-only minimized FTP summary to the reservation profile or applicable
reservation context. Present total, purposes, status, deadline, settled and
outstanding amounts and authorized links to definitive transactions.

Operational roles see only the minimized state needed for reservation,
check-in and checkout. Management roles may open the complete financial plan
profile and receive controls for deadline extension, cancellation or eligible
deletion when those backend contracts are available.

Use `permissions.js` (frontend authorization presentation helper) to expose
semantic FTP permissions derived from the existing operational and management
groups. Direct API authorization remains backend-owned.

## Validation And Errors

Use section-local errors plus one accessible summary. Validate known client
constraints before submission:

- positive cent amounts;
- exact allocation;
- signal method and structure;
- installment quantity from two through twelve;
- no installment controls for future check-in/checkout allocation;
- no client-known deadline conflict;
- required confirmation for replacement.

Map backend field and domain errors to the matching purpose section. A toast
may supplement but never replace inline financial feedback.

For conflict or uncertain network outcome, reload the authoritative booking,
FTP and purpose state. Do not blindly retry a stale transaction identifier.

## Accessibility And Responsive Behavior

Use semantic fieldsets or labelled groups for each purpose. Conditional
controls remain keyboard reachable only while relevant, preserve predictable
focus and announce allocation changes through a polite live region.

Status and allocation completeness use text and iconography in addition to
color. Confirmation, cancellation and destructive replacement remain visually
and semantically distinct.

Extend the existing responsive reservation and check-operation styles. On
narrow screens, financial groups stack while the total, allocated and remaining
summary remains visible and readable.

## Privacy And Logging

Do not log FTP payloads, transaction IDs, guest identity or payment selections
to console or persistent storage. Do not collect card number, security code or
bank credentials.

Preserve the existing API logging minimization and session-expiration behavior.

## Verification

Frontend verification covers:

- cent-precise allocation and residual installment calculation;
- conditional signal controls and disabled defaults;
- no automatic checkout allocation;
- payload minimization and exact contract mapping;
- form-state restoration and idempotence-key preservation;
- loading, empty, conflict, timeout and success states;
- no separate delete/create API sequence;
- check-in and checkout materialization isolation;
- extra-charge and rating regression coverage;
- permission visibility;
- accessible names, focus, live feedback and inline errors;
- responsive source-contract checks;
- absence of persistent storage and console logging;
- JavaScript syntax checks, complete Node test suite and `git diff --check`.

