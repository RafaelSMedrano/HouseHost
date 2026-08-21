# Data-Subject Request Workflow Spec

## Specification

The Data-Subject Request Workflow turns the rights channel described in the
public privacy policy into an effective, free, traceable and secure process.
WhatsApp remains an accessible first-contact channel, while the system issues
an official protocol and, when suitable, a secure link for formal submission,
identity verification, follow-up and response.

Publishing instructions alone does not satisfy this spec. The workflow is
complete only when the controller can receive, authenticate, analyze, execute,
answer and audit a request without exposing another person's data.

## Scope

This spec governs the public channel, official submission, request lifecycle,
identity assurance, accountable handling, execution evidence, response and
escalation language. It covers requests related to confirmation, access,
correction, processing information, restriction, blocking, anonymization,
deletion, consent revocation and the other rights applicable to the processing.

It does not certify legal compliance, replace case-specific legal analysis,
automatically decide every retention exception or create integration with the
ANPD or courts.

## Capabilities

### Publish An Accurate Rights Channel

The current backend-governed privacy policy identifies WhatsApp
`+55 12 99252-5319` as the initial public channel and explains, in clear
language, that:

- requesting a right is free;
- the controller may perform a proportional identity check;
- an official protocol or secure link continues the process;
- some data cannot be immediately deleted when lawful retention applies;
- a refusal or limitation is explained;
- an unanswered or unsatisfactory request may be taken to the ANPD and other
  competent administrative, consumer-protection or judicial channels.

The policy must not threaten litigation, promise a guaranteed result or imply
that judicial action is the only escalation route.

### Support Accessible Official Intake

An authorized operator can register a request received through WhatsApp and
send an official invitation to the data subject's verified or declared email.
The invitation contains an opaque, expiring, revocable and single-purpose link.

Email is not mandatory for a person who cannot use it. The operator can perform
an assisted intake through another safe channel and the system produces the
same protocol, lifecycle and evidence. Neither email nor WhatsApp carries the
requested personal-data package or identity documents when a safer authenticated
delivery mechanism is available.

The public form presents the right types, required description, privacy notice
and only the information necessary to locate the processing. Successful intake
returns a non-sequential public protocol without confirming unrelated records.

### Verify Identity Proportionally

Identity assurance is proportional to the requested right, data sensitivity,
disclosure risk and information already held by the controller. The workflow
can request an additional verification step before access, portability or any
disclosure of personal data.

Verification evidence is minimized, access-controlled and retained only for a
defined period. Failure to verify does not silently close the request: the
subject receives safe instructions, and the decision is recorded without
revealing whether another person's data exists.

### Govern A Durable Lifecycle

Each request has a stable internal identifier, public protocol, right type,
received time, channel, identity-verification status, accountable assignee,
applicable due date, current status and status history.

The minimum lifecycle is:

```text
RECEIVED
IDENTITY_PENDING
UNDER_REVIEW
IN_EXECUTION
COMPLETED
PARTIALLY_COMPLETED
DENIED
CANCELLED
```

Every transition records actor, time and a minimized reason. Terminal decisions
cannot be rewritten; a correction is represented by a new history event. Access
is restricted to authorized administrative roles, and overdue requests are
visible and escalated internally.

### Analyze And Execute The Applicable Right

The accountable operator locates data across booking, guest, stay, finance,
security, audit, supplier, integration, export and backup contexts as
applicable. The analysis records which contexts were checked, the action
selected and any lawful retention or technical limitation.

Execution can record correction, access, blocking, restriction, anonymization,
deletion, consent revocation, communication to an operator or a justified
partial/refused result. A destructive action requires explicit administrative
confirmation and evidence from the owning module; a workflow status alone is
not proof that data was changed.

Deletion does not alter immutable audit evidence or records lawfully retained.
Restricted retained data is not reused for an incompatible purpose and is
protected against accidental restoration from backups after its applicable
lifecycle.

### Respond Safely And On Time

Confirmation and simplified access are answered immediately when safely
possible. A complete access statement is supplied within the applicable legal
period, currently planned as no more than 15 days after a valid request. Other
rights are handled without undue delay, with the applicable deadline and basis
recorded rather than invented by the frontend.

The final response uses clear language and states the actions performed,
limitations, retained categories and reason for a partial or denied result.
Sensitive results are delivered through an authenticated, expiring mechanism,
not as unrestricted email or WhatsApp attachments.

### Preserve Minimal Audit Evidence

Audit evidence covers intake, invitation issuance, identity state, assignment,
status transitions, searches, approved execution, operator communication and
final response. Audit metadata excludes passwords, link tokens, identity
documents, complete request narratives and exported personal-data packages.

The system retains enough evidence to demonstrate the protocol, chronology,
decision and accountable actors. Retention of the request file and verification
evidence is governed by an explicit retention rule and is not indefinite by
default.

## Prerequisite Specs

- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/privacyPolicySubmoduleSpec.md`

## Spec Degree

4.
