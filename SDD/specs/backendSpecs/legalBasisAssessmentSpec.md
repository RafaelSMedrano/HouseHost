# Legal Basis Assessment Spec

## Specification

Legal Basis Assessment is the privacy-governance capability that connects each
specific purpose of a registered personal-data processing operation to one
structured, justified, reviewable and versioned lawful-basis decision.

The capability turns a general lawful-basis label into demonstrable governance
evidence. It records the accountable human decision; it does not automatically
decide the law, certify compliance or replace fact-specific legal review.

## Scope

This spec governs lawful-basis assessments belonging to processing operations
registered by the `privacy` module. One processing operation can have multiple
assessments because different purposes can rely on different lawful bases. One
assessment represents exactly one purpose and one ordinary-personal-data lawful
basis. Combined labels are not assessments.

An assessment can additionally identify a specific sensitive-data basis when
the same purpose necessarily processes data covered by the LGPD sensitive-data
regime. The ordinary-data basis never silently authorizes sensitive-data
processing.

The existing processing-operation status continues to describe whether an
activity exists operationally. Lawful-basis readiness is a separate governance
state. Migrating an existing activity to a pending assessment does not
automatically interrupt reservations, authentication, auditing or another
runtime flow, but the operation must not be represented as legally reviewed or
production-ready until its applicable assessments are approved.

The legacy `legalBasis` text on a processing operation may remain temporarily
for compatibility and display, but it is not approval evidence and does not
determine lawful-basis readiness after this capability is introduced.

### Current Operation Boundary

The initial assessment set covers the current operations for reservations,
guests, stays, finance, internal-user access, supplier governance and
security/audit accountability. Marketing remains inactive and receives no
current-purpose approval merely because a historical operation or legacy basis
exists.

The implementation can provide draft candidate assessments for existing
operations, but it must not approve them automatically. Exact legal references,
balancing conclusions and accountable approval are supplied or confirmed by an
authorized human acting for the controller.

## Capabilities

### Associate One Assessment With One Purpose And Basis

Every assessment identifies:

- its processing operation;
- one explicit and specific purpose;
- one controlled lawful-basis type;
- a factual justification explaining why the basis applies;
- the personal-data categories evaluated;
- a necessity assessment explaining why those data are adequate, relevant and
  limited to the purpose;
- whether sensitive data are involved and, when applicable, the separate
  sensitive-data basis and indispensability justification;
- lifecycle, version, authorship and approval evidence.

Two different purposes use two assessments even when their lawful-basis type is
the same. Two different bases for one operation are not concatenated into one
string.

### Use Controlled Lawful-Basis Types

The ordinary-data basis uses a domain enum. The current supported values are:

```text
CONSENT
LEGAL_OR_REGULATORY_OBLIGATION
CONTRACT_OR_PRE_CONTRACT
REGULAR_EXERCISE_OF_RIGHTS
PROTECTION_OF_LIFE
LEGITIMATE_INTEREST
CREDIT_PROTECTION
```

Sensitive-data bases use a separate domain enum aligned with the applicable
LGPD sensitive-data hypotheses supported by the product. A new basis cannot be
introduced as arbitrary text; product support for a new hypothesis requires an
SDD review.

Display labels and explanations may be translated, but stored and transmitted
enum values remain stable.

### Require Evidence According To The Selected Basis

Every assessment requires purpose, justification, evaluated data categories and
necessity analysis.

Additional evidence is conditional:

- `LEGAL_OR_REGULATORY_OBLIGATION` requires the concrete applicable legal or
  regulatory reference and an explanation of what it obliges the controller to
  do;
- `CONTRACT_OR_PRE_CONTRACT` requires the contractual relationship or
  data-subject-requested preliminary step and why the data are necessary for it;
- `LEGITIMATE_INTEREST` requires a structured assessment of the legitimate
  interest, the data subject's reasonable expectation, necessity, impacts on
  rights and freedoms, safeguards and the final balance conclusion;
- `CONSENT` requires the specific purpose, collection mechanism, evidence
  method and withdrawal mechanism; the current inactive marketing activity
  cannot use this capability to reactivate marketing;
- a purpose involving sensitive data requires the separate sensitive-data
  basis, indispensability analysis and additional safeguards.

The application validates evidence completeness and consistency. It does not
assert that a complete record is legally correct.

### Follow A Review And Approval Lifecycle

Assessment status values are:

```text
DRAFT
UNDER_REVIEW
APPROVED
REJECTED
SUPERSEDED
```

A draft can be edited and submitted only after required evidence is complete.
An assessment under review can be approved or rejected by an authenticated
authorized administrator. Rejection requires a reason and returns no legal
readiness.

Approval records the reviewer, approval time and immutable version. Approval
means that an accountable human recorded the decision represented by that
version; the UI and API must not describe it as automatic legal certification.

An approved assessment is immutable. A later change creates a new draft version
linked to the previous version. When the replacement is approved, the previous
version becomes `SUPERSEDED`. Historical versions remain readable and are not
hard-deleted.

### Calculate Lawful-Basis Readiness Separately

A processing operation exposes a derived readiness summary:

```text
NOT_ASSESSED
DRAFT
UNDER_REVIEW
APPROVED
REJECTED
```

`APPROVED` requires at least one current approved assessment and no current
purpose awaiting replacement or rejected without resolution. The legacy basis
string and the operation's generic reviewed timestamp do not satisfy this rule.

The existing operation review can remain as evidence that the broader inventory
was reviewed, but it does not approve a lawful-basis assessment.

### Preserve Accountability And Minimum Audit Data

Creation, material draft update, submission, approval, rejection and
supersession are auditable facts. Audit metadata contains assessment ID,
operation ID, basis type, status and version. It does not copy complete legal
narratives, sensitive-data descriptions or the entire request.

Only `CEO`, `CTO` and `ADMIN` can access assessment management. Backend
authorization remains authoritative independently of frontend visibility.

### Preserve Migration And Catalog Consistency

Existing processing operations are associated by stable operation code, not by
mutable display name. Migration and catalog initialization are idempotent.

Candidate assessments created during migration start as `DRAFT` or
`UNDER_REVIEW`; they are never marked `APPROVED` by startup code. Repeated
startup does not duplicate versions or overwrite human-reviewed content.

Marketing remains inactive throughout migration. A legacy marketing assessment
or historical consent field cannot produce current marketing readiness.

### Keep The Administrative Experience Safe And Understandable

The administrative experience shows processing operations, their lawful-basis
readiness, assessment versions and required evidence. Conditional forms explain
why a field is required for the chosen basis.

The interface distinguishes draft, review and approval, warns that approval
records a governance decision rather than legal certification and never stores
assessment narratives in browser persistence. Backend text is rendered safely,
and status is communicated by text in addition to color.

## Prerequisite Specs

- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`

## Spec Degree

2.
