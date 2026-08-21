# Cantinho das Lavandas Task Bootstrap

## Purpose

Read this file before executing any enumerated SDD task.

Task suffixes identify the implementation area:

- `b`: backend;
- `f`: frontend;
- `a`: design and assets.

Number each suffix independently, starting at `001`: backend, frontend and
design/asset task sequences do not affect one another.

## Execution Rule

Before and during a task:

1. Read `SDD/specs/sddSpec.md` and this bootstrap.
2. Read the current task and every file in `Required Implementation Files`.
3. Follow prerequisite specs until reaching `cantinhoDasLavandasMainSpec`.
4. Implement only the task scope and strictly necessary dependencies.
5. Preserve the existing hexagonal/module architecture and naming patterns.
6. Keep backend, frontend and asset work in separate tasks.
7. Document small, non-conflicting MVP decisions in the report.
8. Run tests and checks proportional to the change.
9. Perform the prerequisite review defined by the SDD process.
10. Create the technical report in `SDD/ImplementationReport/`.
11. After every acceptance criterion and prerequisite review passes, add
    `DONE` to the task title and filename and update all references to that task
    file, following `SDD/specs/sddSpec.md`.

## Technical Report Rule

Name reports as:

```text
YYYY-MM-DD-task-id-short-title.md
```

Include every item required by `SDD/specs/sddSpec.md`. If a proposed solution
conflicts with an authoritative document, do not apply it: document the conflict
and keep the task blocked until the documents are corrected in SDD order.

Every report must contain an explicit list of files created. When a task creates
no files, that list must state `None`.

## Current Task Set

The implementation cycle contains completed tasks `001b`, `002b`, `001f`,
`003b`, `002f`, `004b`, `005b`, `003f`, `006b`, `004f`, `005f` and `007b`,
followed by completed tasks `008b`, `009b`, `010b`, `006f` and `012b`, followed by
proposed backend task `011b`, frontend
tasks `007f` and `008f`, historical
navigation-history tasks `009f` and `010f` based on the superseded
route-registry design, and completed replacement tasks `015f` through `021f`.
Cross-cutting backend tasks `013b` and `014b` are also complete after their
respective explicit independent approvals. Frontend logging tasks `022f`
through `025f` are complete after their respective explicit independent
approvals. Corrective backend task `015b` completed migration of the legacy
financial booking association after explicit approval.
Guest schema-compatibility task `016b` completed the guest status and obsolete
care-storage migration after explicit approval.
Guest domain and registration-contract task `017b` completed the server-side
inactive default, editable care-text contract and operational-state protection
after explicit approval.
Guest status-transition task `018b` completed direct reservation, check-in and
checkout status assignments without a lifecycle service after explicit
approval.
Corrective guest status task `020b` completed reservation-wide status
derivation and module-local Notifier/Resolver communication after explicit
approval.
Corrective guest status architecture task `021b` completed the single
`ParticipantNotifier` flow per source module and restored direct service calls
for simple queries after explicit approval.
Guest checkout-history task `019b` completed atomic, idempotent stay-history
and optional-assessment application after explicit approval.
Guest identification frontend task `026f` completed removal of manual status
input and aligned status presentation with the authoritative backend values
after explicit approval.
Guest care-fields frontend task `027f` completed the two-textarea registration
contract and removed obsolete structured care presentation after explicit
approval.
Guest checkout-assessment frontend task `028f` completed removal of editable
registration history and added the optional checkout rating with a read-only
history preview after explicit approval.
Guest internal-notes frontend task `029f` completed the symmetric accessible
notes design and integrated guest-flow verification after explicit approval.
Guest referrer-removal tasks `022b` and `030f` completed removal of the obsolete
guest property and database column, retained only the origin channel and moved
the origin section before guest care fields after explicit approval.
Completed booking-service ratings tasks `023b` and `024b` created the
independent ratings domain, persistence foundation, validated use case and
bounded list/guest-history API after their respective explicit approvals.
Completed ratings checkout task `025b` created one atomic six-criterion rating
for newly completed checkout and removed the obsolete generic Guest/CheckOut
rating after explicit approval.
Completed ratings query task `026b` replaced aggregate-loading summaries with
minimized booking-derived projections, proved bounded query behavior and
blocked deletion of rated bookings after explicit approval.
Completed ratings security task `027b` enforced operational-role access, added
minimized audit events and verified architecture plus legacy removal after
explicit approval. Completed frontend ratings foundation task `031f` added the
authorized sidebar root, bounded API helpers and history-preserving profile
callbacks after explicit approval. Completed frontend ratings checkout task
`032f` replaced the generic optional rating with six required accessible score
groups and optional observations after explicit approval. Completed frontend
ratings list task `033f` added the complete escaped, paginated and responsive
table after explicit approval. Completed participant-link task `034f`
independently verified semantic callbacks, exact retained list state and the
absence of rating-row actions after explicit approval. Frontend task `035f`
completed exact-selection rating history, stale-request protection, accessible
inline presentation and reservation-form restoration after explicit approval.
The booking-service ratings frontend sequence is complete.
Backend task `028b` completed central financial participant notification and
the Cashier integration boundary after its scope was corrected to cover the
complete Finance module.
Backend task `029b` and frontend task `036f` completed removal of the obsolete
financial transaction directional amounts while retaining the approved
structural type vocabulary and Cashier-owned movement direction.
Backend task `030b` completed optional source notification during financial
transaction deletion through the central participant notifier.
Backend task `031b` completed the authoritative transaction taxonomy, compatible
legacy migration and PLAN-to-INSTALLMENT source hierarchy. Backend task `032b`
completed Cashier schedule, settlement, reversal, temporal migration and
concurrency semantics. Backend task `033b` completed the FTP aggregate, derived
lifecycle, persistence, stable ordering, bounded loading and concurrency
foundation. Backend task `034b` completed authenticated FTP use cases,
idempotence and atomic reservation/initial-plan creation. Backend task `035b`
completed atomic and idempotent replacement of provisional FTP payments,
including rollback, concurrency and post-commit audit semantics. Backend task
`036b` completed check-in/checkout materialization, authorization and complete
backend FTP verification.
Frontend task `037f` completed the reservation allocation interface after
explicit execution approval. Frontend task `038f` completed the idempotent
reservation command, timeout reconciliation and read-only FTP summary after
explicit execution approval. Frontend task `039f` completed the check-in
scheduled-payment loading, confirmation, atomic materialization and recovery
flow after explicit execution approval. Frontend task `040f` completed
checkout materialization plus verification after explicit execution approval.
Backend task `037b` completed the reusable provider-neutral notifier contracts
and domain after explicit approval. Backend task `038b` completed notifier-
owned intent/provider-event persistence, idempotency, atomic claims, lease
recovery and retention anonymization after explicit approval. Backend task
`039b` completed automatic provider-neutral dispatch, durable retry, jitter,
restart recovery and exhausted-intent reprocessing after explicit approval.
Backend task `040b` completed trusted delivery profiles, AWS SDK v2 SES sending,
default credential-chain client creation and provider-neutral failure mapping
after explicit approval. Backend task `041b` completed authenticated SNS
feedback ingestion, normalized SES feedback and atomic idempotent notifier
state transitions after explicit approval. Backend task `042b` completed
required public transactional email, two atomic public-booking notification
intents and complete provider-isolation verification after explicit approval.
Production delivery and SNS ingress remain disabled. Frontend task `041f`
completed the public reservation form adaptation to the verified transactional
email contract after explicit execution approval. The form now validates and
normalizes the required email, sends only `guest.email` in the public contract
and preserves later WhatsApp confirmation and payment negotiation. This did
not authorize production email activation.
The execution order and completion state are defined by
`SDD/implementation/implementation-order.md`.
