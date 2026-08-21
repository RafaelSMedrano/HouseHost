# Task 024f DONE — Integrate Logger Into Administrative Startup

## Status

Completed on 11 August 2026 after explicit independent user approval.

## Implementation Area

Frontend (`f`).

## Objective

Initialize the sanitized frontend logger and global error capture during the
administrative application bootstrap without changing API transport behavior.

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
- `SDD/tasks/frontendSpecs/023f-DONE-capture-global-frontend-errors.md`

## Scope

- Initialize the frontend logger before starting the administrative UI.
- Install global error listeners during bootstrap.
- Replace the current startup `console.log` with a stable logger event.
- Select environment-aware local console behavior without embedding secrets or
  deployment credentials in frontend code.
- Guarantee that initialization, console or listener failures do not prevent
  the administrative application from starting.
- Add bootstrap integration tests and run the complete frontend suite.

## Out Of Scope

- Sending logs to the backend.
- Modifying `api.js` or adding request correlation.
- Implementing the authenticated client-log transport.
- Replacing unrelated console calls or catch blocks.
- Backend files.

## Expected Files

```text
frontend/admin/js/controllers/main.js
frontend/admin/js/logger.js
frontend/admin/tests/loggerBootstrap.test.mjs
```

## Acceptance Criteria

- Logger initialization occurs before the administrative UI bootstrap.
- Global listeners are installed once during startup.
- Startup uses a stable operational logger event instead of the direct
  `console.log` targeted by this task.
- Production configuration can suppress ordinary `DEBUG` and `INFO` console
  noise while development remains diagnosable.
- Logger or listener initialization failure does not interrupt application
  startup.
- Existing administrative startup behavior remains functional.
- Existing and new frontend tests pass.

## Verification Commands

At minimum:

```text
node --test frontend/admin/tests/logger.test.mjs
node --test frontend/admin/tests/globalErrorLogging.test.mjs
node --test frontend/admin/tests/loggerBootstrap.test.mjs
node --test frontend/admin/tests/*.test.mjs
git diff --check
```

## Required Report

Create:

```text
SDD/ImplementationReport/YYYY-MM-DD-024f-integrate-logger-administrative-startup.md
```

Completed report:

```text
SDD/ImplementationReport/2026-08-11-024f-integrate-logger-administrative-startup.md
```
