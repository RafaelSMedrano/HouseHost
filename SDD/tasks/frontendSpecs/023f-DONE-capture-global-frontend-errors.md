# Task 023f DONE — Capture Global Frontend Errors

## Status

Completed on 11 August 2026 after explicit independent user approval.

## Implementation Area

Frontend (`f`).

## Objective

Capture uncaught JavaScript errors and unhandled promise rejections through the
sanitized frontend logger without integrating it into administrative startup
or enabling remote delivery.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/operationalLoggingSpec.md`

## Required Plans

- `SDD/plans/operationalLoggingTechnicalPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`

## Dependencies

- `SDD/tasks/frontendSpecs/022f-DONE-create-frontend-logger-core.md`

## Scope

- Add `installGlobalErrorLogging()` to the frontend logger contract.
- Attach listeners for browser `error` and `unhandledrejection` events.
- Normalize `Error`, string and unknown rejection reasons into the logger's
  allowlisted contract without blindly serializing arbitrary objects.
- Emit stable events for uncaught errors and unhandled rejections.
- Make listener installation idempotent.
- Prevent a listener or logger failure from producing recursive logging or an
  additional unhandled rejection.
- Add isolated Node tests for global capture behavior.

## Out Of Scope

- Editing `main.js` or installing the listeners during application startup.
- Sending logs to the backend.
- Modifying `api.js`.
- Migrating existing catch blocks or console calls.
- Backend files.

## Expected Files

```text
frontend/admin/js/logger.js
frontend/admin/tests/globalErrorLogging.test.mjs
```

## Acceptance Criteria

- Browser `error` events produce one stable sanitized logger event.
- Unhandled promise rejections produce one stable sanitized logger event.
- Strings, `Error` instances and unknown rejection values are handled safely.
- Sensitive markers, URL queries and control characters do not appear in
  captured output.
- Repeated installation does not register duplicate listeners.
- Listener and logger failures do not propagate or recursively report
  themselves.
- Relevant frontend tests pass.

## Verification Commands

At minimum:

```text
node --test frontend/admin/tests/logger.test.mjs
node --test frontend/admin/tests/globalErrorLogging.test.mjs
node --test frontend/admin/tests/*.test.mjs
git diff --check
```

## Required Report

Create:

```text
SDD/ImplementationReport/YYYY-MM-DD-023f-capture-global-frontend-errors.md
```

Completed report:

```text
SDD/ImplementationReport/2026-08-11-023f-capture-global-frontend-errors.md
```
