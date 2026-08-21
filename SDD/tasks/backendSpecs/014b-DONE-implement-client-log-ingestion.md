# Task 014b DONE — Implement Client Log Ingestion

## Status

Completed on 11 August 2026 after explicit independent user approval.

## Implementation Area

Backend (`b`).

## Objective

Provide a controlled authenticated endpoint that accepts sanitized browser
warnings and errors and emits them through the backend SLF4J/Logback pipeline.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/operationalLoggingSpec.md`

## Required Plans

- `SDD/plans/operationalLoggingTechnicalPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`

## Dependencies

- `SDD/tasks/backendSpecs/013b-DONE-configure-operational-logging-foundation.md`

## Scope

- Create `ClientLogUseCase`, `ClientLogService`, `ClientLogSinkPort`,
  `ClientLogController`, `ClientLogRequestDTO` and `Slf4jClientLogAdapter` in
  the `observability` module.
- Expose authenticated `POST /client-logs` to all current authenticated roles.
- Accept only `WARN` and `ERROR` and the allowlisted fields in the technical
  plan.
- Apply strict event syntax, per-field length, total payload size, numeric range
  and control-character validation.
- Sanitize paths, messages and stacks again on the server even if the frontend
  reports already-sanitized content.
- Derive actor, request origin and receipt time on the server rather than
  trusting browser values.
- Apply bounded throttling by authenticated identity and trusted client origin.
- Emit accepted events using dedicated logger `HOUSEHOST_CLIENT_LOG` without
  serializing the raw DTO.
- Return a minimal success response and safe validation/throttling errors.
- Add controller, service, security, throttling and sensitive-data tests.

## Out Of Scope

- Anonymous/pre-login log ingestion.
- Frontend implementation.
- Storing client logs in the application database.
- A distributed limiter for a future multi-instance deployment.
- External dashboards or alert delivery.

## Expected Files

Expected additions or changes include:

```text
src/main/java/com/househost/observability/application/dto/ClientLogRequestDTO.java
src/main/java/com/househost/observability/application/port/in/ClientLogUseCase.java
src/main/java/com/househost/observability/application/port/out/ClientLogSinkPort.java
src/main/java/com/househost/observability/application/service/ClientLogService.java
src/main/java/com/househost/observability/adapter/in/rest/ClientLogController.java
src/main/java/com/househost/observability/adapter/out/integration/Slf4jClientLogAdapter.java
src/main/java/com/househost/security/adapter/in/config/SecurityConfig.java
src/test/java/com/househost/observability/...
```

## Acceptance Criteria

- Anonymous requests receive `401` and produce no accepted client-log event.
- Every current authenticated role can submit a valid event.
- Only `WARN` and `ERROR` are accepted.
- Unknown fields, malformed events, oversized fields and excessive payloads are
  rejected safely.
- Accepted records contain server-established actor and request context.
- Browser-supplied identity or timestamp cannot replace server-owned values.
- Control characters cannot forge a second physical log line.
- Test marker passwords, JWTs, cookies, documents, emails and telephone numbers
  do not appear in emitted records.
- Excessive submissions are throttled without unbounded in-memory state.
- A sink failure does not change business data and returns a safe outcome.
- Relevant automated tests pass.

## Verification Commands

At minimum:

```text
./mvnw test
git diff --check
```

## Required Report

Create:

```text
SDD/ImplementationReport/YYYY-MM-DD-014b-implement-client-log-ingestion.md
```

Completed report:

```text
SDD/ImplementationReport/2026-08-11-014b-implement-client-log-ingestion.md
```
