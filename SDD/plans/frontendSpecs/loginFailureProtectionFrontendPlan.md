# Login Failure Protection Frontend Plan

## Governing Spec

- `SDD/specs/backendSpecs/loginFailureProtectionSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- prerequisite: `SDD/specs/cantinhoDasLavandasMainSpec.md`

## Objective

Present invalid credentials and temporary login restrictions accurately in the
administrative login interface without treating the browser as the enforcement
authority or exposing account existence.

This plan depends on the backend HTTP contract defined by
`SDD/plans/backendSpecs/loginFailureProtectionBackendPlan.md`.

## Current Architecture

Relevant components:

- `api.js` (frontend/admin API adapter; module)
- `apiRequest` (frontend/admin API adapter; function)
- `parseJsonResponse` (frontend/admin API adapter; function)
- `login` (frontend/admin API adapter; function)
- `loginWidget.js` (frontend/admin widget; module)
- login submit handler (frontend/admin widget; login interaction)

`parseJsonResponse` currently throws a generic `Error` for non-successful HTTP
responses and does not preserve HTTP status or `Retry-After`. The login widget
therefore cannot distinguish invalid credentials, rate limiting and network
failure reliably.

## API Error Contract

Create a small frontend error representation such as:

```text
ApiError (frontend/admin API adapter; class)
```

Fields:

```text
status
message
retryAfterSeconds
```

`parseJsonResponse` reads:

- the JSON response message;
- HTTP status;
- `Retry-After`, accepting only a non-negative integer duration for this flow.

Do not expose backend diagnostics or restriction scope in UI text.

Existing API consumers must preserve their current behavior. If introducing a
typed error globally would affect unrelated screens, add a compatible path and
update consumers deliberately rather than silently changing all error handling.

## Login Widget Behavior

Update the login submit handler in `loginWidget.js`:

- `401`: clear the password field and show the generic credentials message;
- `429`: clear the password, show the generic temporary-restriction message and
  disable submit for the `Retry-After` duration;
- `503`: show temporary service unavailability without implying invalid
  credentials;
- network failure: retain the existing connection-error meaning;
- success: preserve the current session-save and navigation behavior.

The widget may show a countdown rounded to whole seconds. The countdown is a
usability aid only. Reloading, editing JavaScript or opening another browser
does not affect the backend restriction.

When the countdown reaches zero, the button is re-enabled. The frontend does
not automatically submit stored credentials.

## Privacy And Security Rules

- Do not store password, attempted password, restriction state or attempted
  email in `localStorage` or `sessionStorage`.
- Do not log credentials or full error payloads to the browser console.
- Do not display whether the email exists.
- Do not display remaining attempts before restriction.
- Do not claim that a suspicious attempt is a confirmed incident.
- Preserve the existing rule that only a successful response stores a JWT.

## Accessibility And Usability

- Announce error and countdown changes through the existing visible alert and
  an appropriate live region.
- Keep the message readable without relying only on color.
- Prevent repeated clicks while a request is in flight.
- Restore focus to the password field after an ordinary `401`.
- Keep focus on the restriction message after `429`.
- Do not reveal a specific account in the restriction wording.

## Cache Busting

Update the query versions for every changed public import in the administrative
frontend so deployed browsers do not combine the new backend response with an
old login widget or API parser.

## Verification Strategy

- `401` shows invalid credentials rather than a connection error;
- `429` reads `Retry-After` and disables submit for the displayed duration;
- malformed or absent `Retry-After` uses a safe short fallback without
  pretending that the browser controls the backend duration;
- `503` is distinguishable from credentials failure;
- password is cleared for `401` and `429`;
- no restriction or credential data is persisted locally;
- refresh does not create a JWT or bypass backend enforcement;
- successful login remains unchanged;
- keyboard and screen-reader behavior remains usable;
- changed module URLs receive new cache-busting versions.

## Out Of Scope

- choosing security-alert recipients;
- displaying audit history;
- administrative manual unlock;
- CAPTCHA;
- MFA;
- password recovery;
- frontend-only blocking.
