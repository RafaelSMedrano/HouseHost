# Public Booking Privacy Acceptance Backend Plan

## Governing Specs

- `SDD/specs/backendSpecs/privacyPolicySubmoduleSpec.md`
- `SDD/specs/frontendSpecs/publicPrivacyPolicyExperienceSpec.md`
- `SDD/specs/publicBookingDataMinimizationSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- prerequisite: `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`

## Objective

Make public booking resolve the current server-published privacy policy through
a transient ID and persist a self-contained acceptance snapshot without
rewriting legacy bookings or coupling Booking persistence to Privacy.

Task `010b` executes this plan after task `009b` when explicitly approved.

## Public Request Contract

Replace client-controlled `privacyPolicyVersion` in `PublicBookingRequestDTO`
with `Long privacyPolicyId`. Keep `privacyAccepted` mandatory and keep
`termsVersion` unchanged.

`privacyPolicyId` exists only in the inbound request and during current-policy
validation. It is never copied into the booking domain, booking response,
booking JPA entity, booking table or booking audit metadata.

`PublicBookingService` calls the policy application service directly to require
that the supplied ID is the current `PUBLISHED` policy. It copies version and
hash from the returned immutable policy record; it never accepts those values
from the client.

## Concurrency And Conflict

Policy validation and booking creation must establish a transactionally
consistent ordering with publication. Use a lock or equivalent concurrency
control so either:

- booking validates and persists against the still-current policy before a new
  publication completes; or
- publication completes first and booking receives `409 Conflict`.

An unknown policy ID is a controlled client error. A known but superseded ID or
a version changed since page load returns `409`. No current policy produces a
controlled unavailable response.

Create a policy-version conflict exception with an explicit `409` mapping in
`GlobalExceptionHandler`; do not map every privacy validation failure to
conflict.

## Booking Evidence

Extend the booking domain and persistence with nullable:

```text
privacyPolicyContentHash
```

Continue storing:

```text
privacyPolicyVersion
privacyAcceptedAt
```

For new public bookings, version, hash and acceptance time are populated from
the policy service and booking clock. The booking domain method receives an
immutable acceptance snapshot rather than a collection of unrelated client
strings when that improves clarity.

Do not add `privacyPolicyId`, `privacy_policy_id`, a foreign key, `@ManyToOne`,
`@OneToOne` or any other persistence association. The snapshot is deliberately
denormalized and remains meaningful without querying Privacy.

## Legacy Booking Migration

Add only the nullable content-hash column, without backfilling it. Preserve
every existing version string and acceptance timestamp exactly as stored.

A booking with version/time but null hash is reported as legacy evidence. Do
not infer that a historical string corresponds to version 2 and do not
fabricate a content hash.

## Audit Evidence

Extend `PRIVACY_ACCEPTED` metadata with server-resolved version and hash for new
records. Do not store policy ID in this booking-owned audit event. Legacy data
remains readable. Do not copy full policy content, guest contact or request body
into audit metadata.

## Verification Strategy

Test nonexistent, draft, superseded and current policies; missing
acknowledgement; no-current-policy behavior; concurrent publication conflict;
server-derived version/hash; booking persistence mapping; absence of policy ID,
foreign key and JPA relationship; legacy reconstruction; audit evidence;
request compatibility failure for the removed arbitrary version field; full
Maven suite and `git diff --check`.

## Out Of Scope

- rendering the public policy;
- changing policy content or publication lifecycle;
- terms-document governance;
- retroactive booking evidence;
- marketing consent.
