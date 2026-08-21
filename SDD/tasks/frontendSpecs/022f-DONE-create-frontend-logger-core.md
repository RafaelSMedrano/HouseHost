# Task 022f DONE — Create Frontend Logger Core

## Status

Completed on 11 August 2026 after explicit independent user approval.

## Implementation Area

Frontend (`f`).

## Objective

Create the administrative browser logging interface and privacy controls
without global error capture, application bootstrap integration or remote
delivery.

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

- `SDD/tasks/backendSpecs/013b-DONE-configure-operational-logging-foundation.md` for
  the shared event and correlation contract. The frontend task does not depend
  on the client-log endpoint.

## Scope

- Create `frontend/admin/js/logger.js` with `DEBUG`, `INFO`, `WARN` and `ERROR`
  methods and stable dotted event names.
- Provide environment-aware console behavior.
- Copy only approved scalar context; do not serialize arbitrary objects.
- Redact sensitive key names, strip URL queries, remove control characters and
  truncate messages and stack traces.
- Support an injected no-op/remote transport boundary without implementing the
  production transport in this task.
- Guarantee that formatting, console or transport failures do not throw into
  the application flow.
- Prevent recursive logging.
- Add isolated Node tests.

## Out Of Scope

- Editing every existing catch block or console call.
- Sending logs to the backend.
- Installing listeners for global JavaScript errors or unhandled rejections.
- Editing `main.js` or integrating the logger into application startup.
- Modifying `api.js` request behavior.
- Backend files.
- Persisting logs in localStorage or IndexedDB.

## Expected Files

```text
frontend/admin/js/logger.js
frontend/admin/tests/logger.test.mjs
```

## Acceptance Criteria

- Logger methods emit the expected normalized level, event, message and
  allowlisted context in development.
- Production filtering can suppress `DEBUG` and ordinary `INFO` console noise.
- Sensitive marker values do not appear in formatted output.
- URL query strings and control characters are absent from output.
- Messages and stacks respect explicit maximum lengths.
- Arbitrary nested objects are not blindly serialized.
- A formatter, console or injected transport failure never propagates to the
  caller or recursively logs itself.
- Relevant frontend tests pass.

## Verification Commands

At minimum:

```text
node --test frontend/admin/tests/logger.test.mjs
node --test frontend/admin/tests/*.test.mjs
git diff --check
```

## Required Report

Create:

```text
SDD/ImplementationReport/YYYY-MM-DD-022f-create-frontend-logger-core.md
```

Completed report:

```text
SDD/ImplementationReport/2026-08-11-022f-create-frontend-logger-core.md
```
