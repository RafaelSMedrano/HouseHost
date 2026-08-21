# Task 001f DONE — Present Login Restriction Feedback

## Status

Completed on 26 July 2026. See
`SDD/ImplementationReport/2026-07-26-001f-login-restriction-feedback.md`.

## Implementation Area

Frontend (`f`).

## Objective

Present invalid credentials, temporary restrictions and temporary protection
unavailability correctly in the administrative login widget while preserving
the backend as the sole enforcement authority.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/loginFailureProtectionSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/loginFailureProtectionBackendPlan.md`
- `SDD/plans/frontendSpecs/loginFailureProtectionFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- implementation report produced by task `002b`.

## Dependencies

- Task `002b — Enforce Login Failure Limits` completed and reviewed.

## Scope

- Preserve response status and `Retry-After` in the administrative API adapter.
- Introduce a compatible typed API error or equivalent explicit result.
- Update the login widget for `401`, `429`, `503` and network failure.
- Clear password after invalid or restricted authentication.
- Display and announce a temporary countdown for `429`.
- Prevent repeated submit while the request or countdown is active.
- Re-enable submission when the local countdown ends without automatically
  retrying credentials.
- Update cache-busting query versions for changed modules.
- Add available automated tests and a documented manual verification matrix.

## Out Of Scope

- Any backend change.
- Local enforcement as a security control.
- Alert administration.
- Manual unlock.
- CAPTCHA, MFA or password reset.
- Saving login attempts in browser storage.

## Expected Files

Expected changes include:

```text
frontend/admin/js/api.js
frontend/admin/js/widgets/loginWidget.js
frontend/admin/js/controllers/main.js or the importing cache-bust owner
frontend/admin tests, if a test harness exists
```

Do not change unrelated API consumers without an explicit compatibility reason
documented in the report.

## Acceptance Criteria

- `401` displays the generic credentials message, not a connection error.
- `429` displays the generic temporary-restriction message.
- A valid `Retry-After` controls a visible whole-second countdown.
- Missing or malformed `Retry-After` uses a documented safe UI fallback.
- `503` displays temporary service unavailability without implying wrong
  credentials.
- Password is cleared after `401` and `429`.
- Email existence, restriction scope and attempts remaining are never shown.
- Password, attempted password and restriction state are not stored in browser
  persistence.
- The UI cannot create a session without the existing successful backend
  response.
- Reloading or editing the browser countdown does not bypass backend
  enforcement.
- Keyboard focus and live error announcement remain usable.
- Existing successful login behavior remains unchanged.
- Changed modules receive cache-busting versions.
- Available automated checks and the manual response matrix pass.

## Verification Matrix

Manually verify at minimum:

| Backend outcome | Expected UI |
|---|---|
| `200` | Save session and open the panel. |
| `401` | Generic invalid credentials; password cleared. |
| `429` + valid header | Restriction message and countdown. |
| `429` + invalid header | Restriction message and safe fallback countdown. |
| `503` | Temporary service-unavailable message. |
| Network failure | Connection error without credentials claim. |

## Required Report

Create:

```text
SDD/ImplementationReport/YYYY-MM-DD-001f-login-restriction-feedback.md
```

The report must include screenshots or equivalent UI evidence when practical,
all checks run and the prerequisite review required by the SDD process.
