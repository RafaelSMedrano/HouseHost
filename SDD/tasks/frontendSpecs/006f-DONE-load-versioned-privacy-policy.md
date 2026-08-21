# Task 006f DONE — Load And Acknowledge Versioned Privacy Policy

## Status

Completed on 28 July 2026 after implementation, verification and prerequisite
review. See
`SDD/ImplementationReport/2026-07-28-006f-load-versioned-privacy-policy.md`.

## Implementation Area

Frontend (`f`).

## Objective

Load and safely render the authoritative policy from the backend, submit its ID
as a transient validation token and require rereading when publication changes
during the reservation journey.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/publicBookingDataMinimizationSpec.md`
- `SDD/specs/backendSpecs/privacyPolicySubmoduleSpec.md`
- `SDD/specs/frontendSpecs/publicPrivacyPolicyExperienceSpec.md`

## Required Plans

- `SDD/plans/frontendSpecs/publicPrivacyPolicyFrontendPlan.md`
- `SDD/plans/backendSpecs/publicBookingPrivacyAcceptanceBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- implementation reports for tasks `009b` and `010b`.

## Dependencies

- tasks `009b` and `010b` completed and their contracts verified.

## Scope

- Add the current-policy request to the public API adapter.
- Replace static policy content with safe rendering of the controlled document.
- Display server-provided version and effective date.
- Keep policy state in memory only.
- Disable acknowledgement and submission until policy loading succeeds.
- Add explicit loading, unavailable and retry states.
- Replace hard-coded policy version with transient `privacyPolicyId` in booking
  payloads, without treating it as a persisted booking relationship.
- Handle `409` by clearing acknowledgement, loading the new policy, preserving
  other safe fields and requiring a new acknowledgement.
- Prevent duplicate submissions and false success.
- Preserve keyboard, focus and status accessibility.
- Update cache-busting import versions and add available frontend checks.

## Out Of Scope

- Administrative policy management UI.
- Editing policy content.
- Browser persistence.
- Terms versioning, marketing or a frontend framework migration.

## Acceptance Criteria

- The policy page contains no authoritative hard-coded policy content or
  version.
- The site loads title, content, version, hash and effective date from the
  public API.
- Server content is rendered without arbitrary `innerHTML` execution.
- A failed policy load is explicit, retryable and blocks acknowledgement and
  final submission.
- The booking request sends transient policy ID and acknowledgement, not
  version/hash; policy ID is not persisted in the booking.
- A `409` loads the new policy, clears acknowledgement and preserves other safe
  form fields.
- Resubmission requires a new acknowledgement and cannot duplicate a booking.
- No policy or reservation content enters browser persistence or console logs.
- Loading and conflict states are accessible through text, keyboard and focus
  feedback.
- Existing public navigation and reservation behavior continue working.
- Available frontend checks and `git diff --check` pass.

## Verification Matrix

| Scenario | Expected result |
|---|---|
| Open policy | Current server policy, version and effective date render. |
| API unavailable | Clear retry state; acknowledgement and submit disabled. |
| Unsafe document node | It is rejected or rendered as inert text. |
| Submit current policy | Request contains transient current policy ID and can succeed without persisting that ID. |
| Publish during form | First submit receives `409`; new policy opens. |
| Reread after conflict | Other safe fields remain; checkbox is cleared. |
| Retry submit | New acknowledgement is required; only one booking is created. |
| Reload page | Policy is fetched again; nothing is restored from storage. |

## Required Report

Create after implementation:

```text
SDD/ImplementationReport/YYYY-MM-DD-006f-load-versioned-privacy-policy.md
```
