# Task 007f — Implement Public Data-Subject Request Experience

## Status

Proposed. Not started and not approved for implementation by creation of this
document alone.

## Implementation Area

Frontend (`f`).

## Objective

Publish the reviewed rights-channel language and implement safe public
invitation, formal submission, protocol, status and response retrieval.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/dataSubjectRequestWorkflowSpec.md`
- `SDD/specs/backendSpecs/privacyPolicySubmoduleSpec.md`
- `SDD/specs/backendSpecs/dataSubjectRequestSubmoduleSpec.md`
- `SDD/specs/frontendSpecs/publicPrivacyPolicyExperienceSpec.md`
- `SDD/specs/frontendSpecs/dataSubjectRequestExperienceSpec.md`

## Required Plans

- `SDD/plans/frontendSpecs/dataSubjectRequestFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- implementation reports for tasks `006f` and `011b`.

## Dependencies

- tasks `006f` and `011b` completed and public contracts verified;
- reviewed policy wording approved for a new backend-governed version.

## Scope

- Publish the reviewed rights workflow through the governed policy lifecycle.
- Add public invitation exchange and formal request views/routes.
- Collect only necessary right, description and locating information.
- Show official protocol after idempotent submission.
- Add minimized safe status and authenticated response retrieval.
- Remove raw invitation tokens from browser history after exchange.
- Keep tokens and sensitive request content out of storage, analytics and logs.
- Add loading, invalid/expired, validation, retry and submitted states.
- Prevent duplicate submissions and preserve accessibility.
- Update cache-busting imports and add available frontend checks.

## Out Of Scope

- Administrative queue.
- Policy editor.
- Browser-side identity or deadline decisions.
- WhatsApp automation or direct delivery of sensitive files by email.

## Acceptance Criteria

- The current policy clearly explains WhatsApp intake, official continuation,
  free requests, proportional identity checks, possible lawful retention and
  administrative/judicial escalation channels.
- A valid invitation can be exchanged and submitted once without duplicate
  requests.
- Invalid, expired and unknown invitations use safe equivalent feedback.
- The successful flow displays the official protocol and next steps.
- Status exposes only the backend-approved minimized information.
- Sensitive response retrieval requires the backend-defined identity/token gate.
- Raw tokens are removed from visible history and never enter browser storage,
  analytics or console logs.
- Loading, failures and retries are explicit and accessible.
- Existing public navigation and privacy-policy behavior continue working.
- Available frontend checks and `git diff --check` pass.

## Verification Matrix

| Scenario | Expected result |
|---|---|
| Valid invitation | Safe exchange opens the official form. |
| Expired or unknown link | Equivalent explanation without existence leak. |
| Double submit | One request and one protocol are shown. |
| Reload after exchange | Raw link token is absent from visible history/storage. |
| Status access | Only minimized backend status appears. |
| Sensitive response | Additional backend gate is required. |
| API unavailable | Clear retry state; no false submission success. |

## Required Report

Create after implementation:

```text
SDD/ImplementationReport/YYYY-MM-DD-007f-implement-public-data-subject-request-experience.md
```
