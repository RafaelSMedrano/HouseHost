# Public Privacy Policy Frontend Plan

## Governing Specs

- `SDD/specs/frontendSpecs/publicPrivacyPolicyExperienceSpec.md`
- `SDD/specs/backendSpecs/privacyPolicySubmoduleSpec.md`
- `SDD/specs/publicBookingDataMinimizationSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- prerequisite: `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`

## Objective

Replace static policy content and hard-coded version submission with the
authoritative public API, safe rendering and a recoverable reread flow when the
server reports a policy-version conflict.

Task `006f` executes this plan after tasks `009b` and `010b` when explicitly
approved.

## API Adapter

Extend `frontend/public/js/api.js` with `findCurrentPrivacyPolicy`. Preserve the
normal public error contract and make HTTP status available for controlled `409`
handling without exposing internal details.

Change public booking payload construction to send:

```text
privacyPolicyId
privacyAccepted
```

Remove the hard-coded `privacyPolicyVersion` value.

The ID is retained only in page memory and sent as a transient validation
token. The booking backend contract does not persist it or create a database
relationship with the policy.

## In-Memory Policy State

The public controller keeps only the current response needed for the active
page session:

```text
id
version
contentHash
effectiveAt
content
loadState
```

Do not use browser persistence. A reload fetches the authoritative policy
again.

## Safe Rendering

Refactor `politicaPrivacidadeView.js` into a renderer for the controlled policy
document. Construct semantic DOM nodes or escaped templates from supported node
types. Never assign server-provided raw HTML to `innerHTML`.

Keep the public controller identity, rights channel and every material statement
from the migrated policy visible. Display version and effective date supplied by
the API.

## Loading And Failure

Represent loading, ready, unavailable and retry states explicitly. Disable the
acknowledgement and final reservation submission until policy state is ready.
Retry does not erase safe reservation inputs.

## Conflict Recovery

When booking returns `409` for policy change:

- do not report reservation success;
- clear the acknowledgement checkbox;
- fetch the new current policy;
- show a clear update message and policy content;
- preserve other safe form fields;
- require the visitor to acknowledge again;
- prevent duplicate concurrent submission.

## Cache Busting And Verification

Update changed public import query versions. Verify safe rendering against
script-like text and unsafe links, initial loading, unavailable retry, ordinary
submission, `409` recovery, state preservation, keyboard access, focus/status
feedback and absence of policy/reservation data in browser storage.

Run available JavaScript checks, backend contract tests where applicable and
`git diff --check`.

## Out Of Scope

- administrative draft/publication UI;
- changing policy wording;
- terms version governance;
- frontend frameworks or rich-text editors.
