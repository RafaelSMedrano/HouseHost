# Task 010b DONE — Record Independent Privacy Acceptance Snapshot

## Status

Completed on 28 July 2026 after explicit user approval, prerequisite review,
implementation, automated verification and live MySQL migration validation.

## Implementation Area

Backend (`b`).

## Objective

Replace client-declared policy versions with transient current-policy ID
validation and persist only server-derived version, hash and acceptance time on
new public bookings while preserving legacy evidence and persistence isolation.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/publicBookingDataMinimizationSpec.md`
- `SDD/specs/backendSpecs/privacyPolicySubmoduleSpec.md`
- `SDD/specs/frontendSpecs/publicPrivacyPolicyExperienceSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/publicBookingPrivacyAcceptanceBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`

## Dependencies

- task `009b` completed;
- existing public booking, booking persistence and public audit flows.

## Scope

- Replace `privacyPolicyVersion` request input with transient
  `privacyPolicyId`.
- Validate current published policy through direct policy-service collaboration.
- Return controlled `409 Conflict` for a superseded/version-changed policy.
- Return controlled unavailable behavior when no policy is published.
- Make validation and publication transactionally consistent.
- Extend booking domain, JPA entity, mapper, response where applicable and
  schema with nullable content hash only.
- Populate server version, hash and acceptance time for new public bookings.
- Preserve existing version strings and timestamps and leave legacy hashes
  null.
- Explicitly prohibit a policy ID column, foreign key and JPA relationship.
- Extend minimized `PRIVACY_ACCEPTED` audit metadata.
- Add service, concurrency, persistence, migration, HTTP and audit tests.

## Out Of Scope

- Public frontend changes.
- Policy publication behavior already delivered by `009b`.
- Retroactive evidence or inferred hashes.
- Terms-document governance or marketing.

## Acceptance Criteria

- The public request cannot declare an authoritative policy version or hash.
- A new booking requires acknowledgement and a transient current published
  policy ID.
- The server copies version and hash from the policy submodule.
- Unknown, draft and superseded policies are rejected with the specified
  controlled behavior.
- A policy changed between page load and submission produces HTTP `409`.
- Concurrent publish/booking behavior cannot accept an invalid interleaving.
- New bookings persist version, hash and acceptance time, but never policy ID.
- Legacy bookings retain their original version/time and null hash.
- Migration does not associate old bookings with version 2.
- Booking persistence has no policy ID, foreign key, `@ManyToOne`, `@OneToOne`
  or dependency on a Privacy persistence type.
- Acceptance audit contains version/hash but not policy ID, content or guest
  contact.
- Existing non-public booking behavior remains compatible.
- Focused tests, full Maven suite and `git diff --check` pass.

## Verification Commands

At minimum:

```text
./mvnw test
git diff --check
```

The implementation report must include read-only before/after evidence for
legacy booking fields.

## Required Report

Create after implementation:

```text
SDD/ImplementationReport/YYYY-MM-DD-010b-record-privacy-acceptance-snapshot.md
```
