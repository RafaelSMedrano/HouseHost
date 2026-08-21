# Data-Subject Request Submodule Spec

## Specification

The Data-Subject Request submodule is the hexagonal backend capability that
persists and enforces the workflow defined by
`dataSubjectRequestWorkflowSpec.md` under:

```text
com.househost.privacy.request
```

It owns official intake invitations, protocols, identity state, deadlines,
assignment, decisions, execution records, communication records and minimized
audit integration. It coordinates actions through owning application
capabilities and never edits another module's repositories or JPA entities.

## Capabilities

### Protect Invitations And Public Contracts

Invitation tokens are cryptographically random, opaque, short-lived, revocable
and stored only as a one-way digest. Public contracts are rate-limited and do
not disclose whether an email, booking, guest or protocol exists. Token values,
identity evidence and request narratives never enter logs or audit metadata.

An invitation can be exchanged only for its intended intake operation. A new
invitation revokes prior active invitations for that operation. Submission is
idempotent and cannot create duplicate requests through retries.

### Persist Workflow Invariants

The domain model owns valid lifecycle transitions, terminal-state protection,
deadline requirements and identity gates. Access or other sensitive disclosure
cannot enter execution or completion before the required identity state is
satisfied.

Every request has an unpredictable public protocol with a database uniqueness
constraint. Status history is append-only. Concurrent commands cannot execute
the same transition or destructive action twice.

### Separate Analysis From Execution Evidence

Analysis identifies relevant processing contexts and proposes actions.
Execution requires an authorized command, an owning-module result and an
append-only action record. The request submodule may directly call coherent
application services in other privacy submodules when dependency direction
remains acyclic; otherwise it uses a request-owned output port and integration
adapter.

The submodule does not claim deletion merely because an administrator selected
it. Completion is allowed only when every applicable action is executed,
explicitly excepted or communicated as a limitation.

### Expose Role-Protected Administration

Authorized `CEO`, `CTO` and `ADMIN` users can list queues, inspect a request,
assign responsibility, record verification, analyze contexts, approve and
record actions, register an operator communication and issue a final response.
Backend authorization is authoritative and sensitive response payloads receive
an additional safe-delivery control.

Administrative reads are paginated and filterable by status, right, assignee
and due state. Lists expose only the minimum personal identifiers needed to
operate the queue.

### Integrate With Policy, Notification And Audit

The public policy remains owned by `privacy.policy`. The request submodule uses
notification, secure-link, clock, identity, action-execution and audit ports.
Provider-specific email or WhatsApp code remains in output adapters.

Audit integration records identifiers, status and action classifications, not
the complete personal content. Notification failure is retryable and visible;
it never falsely records a successful response.

## Prerequisite Specs

- `SDD/specs/dataSubjectRequestWorkflowSpec.md`
- `SDD/specs/backendSpecs/privacyHexagonalSubmodulesSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`

## Spec Degree

5.
