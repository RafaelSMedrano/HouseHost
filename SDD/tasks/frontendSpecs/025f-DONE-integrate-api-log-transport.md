# Task 025f — DONE — Integrate API Correlation And Log Transport

## Status

Completed on 11 August 2026 after explicit user authorization.

## Implementation Area

Frontend (`f`).

## Objective

Correlate administrative API calls with backend records and deliver sanitized
browser warnings and errors through the authenticated client-log endpoint.

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

- `SDD/tasks/backendSpecs/013b-DONE-configure-operational-logging-foundation.md`
- `SDD/tasks/backendSpecs/014b-DONE-implement-client-log-ingestion.md`
- `SDD/tasks/frontendSpecs/022f-DONE-create-frontend-logger-core.md`
- `SDD/tasks/frontendSpecs/023f-DONE-capture-global-frontend-errors.md`
- `SDD/tasks/frontendSpecs/024f-DONE-integrate-logger-administrative-startup.md`

## Scope

- Generate one opaque correlation identifier for each `apiRequest` call.
- Send `X-Correlation-ID` and read the effective response header.
- Measure request duration with a monotonic browser clock when available.
- Extend `ApiError` with correlation and safe request diagnostics while
  preserving existing status and `Retry-After` behavior.
- Log API network failures and non-success outcomes without bodies, tokens,
  response payloads or query strings.
- Implement a dedicated authenticated transport for `WARN` and `ERROR` to
  `POST /client-logs`.
- Keep the transport independent from instrumented `apiRequest` to prevent
  recursive failure reporting.
- Add bounded queueing, short-window deduplication, retry ceiling and explicit
  drop behavior.
- Use page-lifecycle flushing only when authentication can remain protected;
  never place a token in a URL or log payload.
- Add API and transport tests and run the complete frontend suite.

## Out Of Scope

- Anonymous delivery of pre-login browser errors.
- Durable browser storage of queued logs.
- Capturing HTTP request or response bodies.
- Backend implementation.
- Selecting or configuring Loki, Grafana, Elastic or OpenSearch.
- Refactoring unrelated controllers and views.

## Expected Files

Expected additions or changes include:

```text
frontend/admin/js/api.js
frontend/admin/js/logger.js
frontend/admin/js/clientLogTransport.js
frontend/admin/tests/api.test.mjs
frontend/admin/tests/logger.test.mjs
frontend/admin/tests/clientLogTransport.test.mjs
```

## Acceptance Criteria

- Every API request sends one valid `X-Correlation-ID`.
- The server-returned identifier becomes authoritative for the response or
  `ApiError`.
- API failure records contain method, normalized path, status, duration and
  correlation identifier.
- Existing `401`, session-expiration and `Retry-After` semantics remain valid.
- No authorization token, request body, response body, email, password,
  document or telephone marker appears in logger or transport output.
- Only sanitized `WARN` and `ERROR` records are submitted remotely.
- A `/client-logs` failure cannot call itself, trigger an unhandled rejection or
  interrupt the user flow.
- Queue, deduplication and retry state remain explicitly bounded.
- No authentication token is placed in a query string or client-log payload.
- Existing and new frontend tests pass.

## Verification Commands

At minimum:

```text
node --test frontend/admin/tests/api.test.mjs
node --test frontend/admin/tests/logger.test.mjs
node --test frontend/admin/tests/clientLogTransport.test.mjs
node --test frontend/admin/tests/*.test.mjs
git diff --check
```

## Required Report

Create:

```text
SDD/ImplementationReport/YYYY-MM-DD-025f-integrate-api-log-transport.md
```
