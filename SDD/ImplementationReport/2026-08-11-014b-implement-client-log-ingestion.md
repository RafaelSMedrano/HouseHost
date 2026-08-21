# Implementation Report — Task 014b

## Task

- Task id: `014b`.
- Executed file:
  `SDD/tasks/backendSpecs/014b-DONE-implement-client-log-ingestion.md`.
- Execution date: 11 August 2026.
- Authorization: the user explicitly requested implementation of `014b`,
  selecting it independently after completion of dependency `013b`.

## Documents Read

### Specs

- `SDD/specs/sddSpec.md`.
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`.
- `SDD/specs/lgpdGovernanceSpec.md`.
- `SDD/specs/moduleArchitectureSpec.md`.
- `SDD/specs/operationalLoggingSpec.md`.

### Plan

- `SDD/plans/operationalLoggingTechnicalPlan.md`.

### Implementation Files

- `SDD/implementation/task-bootstrap.md`.
- `SDD/implementation/implementation-order.md`.

## Files Created

- `src/main/java/com/househost/observability/domain/model/ClientLogLevel.java`.
- `src/main/java/com/househost/observability/domain/exception/ClientLogRejectedException.java`.
- `src/main/java/com/househost/observability/domain/exception/ClientLogRateLimitExceededException.java`.
- `src/main/java/com/househost/observability/domain/exception/ClientLogUnavailableException.java`.
- `src/main/java/com/househost/observability/application/dto/ClientLogRequestDTO.java`.
- `src/main/java/com/househost/observability/application/records/ClientLogRequestContextRecord.java`.
- `src/main/java/com/househost/observability/application/records/SanitizedClientLogRecord.java`.
- `src/main/java/com/househost/observability/application/port/in/ClientLogUseCase.java`.
- `src/main/java/com/househost/observability/application/port/out/ClientLogSinkPort.java`.
- `src/main/java/com/househost/observability/application/service/CorrelationIdService.java`.
- `src/main/java/com/househost/observability/application/service/ClientLogContextService.java`.
- `src/main/java/com/househost/observability/application/service/ClientLogValidationService.java`.
- `src/main/java/com/househost/observability/application/service/ClientLogRateLimiter.java`.
- `src/main/java/com/househost/observability/application/service/ClientLogService.java`.
- `src/main/java/com/househost/observability/adapter/in/rest/ClientLogController.java`.
- `src/main/java/com/househost/observability/adapter/in/web/ClientLogRequestSizeFilter.java`.
- `src/main/java/com/househost/observability/adapter/out/integration/Slf4jClientLogAdapter.java`.
- `src/test/java/com/househost/observability/application/service/ClientLogContextServiceTest.java`.
- `src/test/java/com/househost/observability/application/service/ClientLogValidationServiceTest.java`.
- `src/test/java/com/househost/observability/application/service/ClientLogRateLimiterTest.java`.
- `src/test/java/com/househost/observability/application/service/ClientLogServiceTest.java`.
- `src/test/java/com/househost/observability/adapter/in/rest/ClientLogAuthorizationTest.java`.
- `src/test/java/com/househost/observability/adapter/in/web/ClientLogRequestSizeFilterTest.java`.
- `src/test/java/com/househost/observability/adapter/out/integration/Slf4jClientLogAdapterTest.java`.
- `SDD/ImplementationReport/2026-08-11-014b-implement-client-log-ingestion.md`.

## Files Changed

- `pom.xml`.
- `src/main/resources/application.properties`.
- `src/main/java/com/househost/observability/adapter/in/web/CorrelationLoggingFilter.java`.
- `src/main/java/com/househost/security/adapter/in/config/SecurityConfig.java`.
- `src/main/java/com/househost/shared/exception/GlobalExceptionHandler.java`.
- `src/test/java/com/househost/observability/adapter/in/web/CorrelationLoggingFilterTest.java`.
- `SDD/plans/operationalLoggingTechnicalPlan.md`.
- `SDD/tasks/backendSpecs/014b-DONE-implement-client-log-ingestion.md`
  (renamed from the proposed filename and marked complete).
- `SDD/tasks/frontendSpecs/025f-DONE-integrate-api-log-transport.md`.
- `SDD/ImplementationReport/2026-08-11-013b-configure-operational-logging-foundation.md`.
- `SDD/implementation/task-bootstrap.md`.
- `SDD/implementation/implementation-order.md`.

## Flows Implemented

### Authenticated Ingestion

`POST /client-logs` accepts authenticated users in every current HouseHost role
and rejects anonymous callers. The HTTP contract contains only `WARN` or
`ERROR`, stable event, message, optional correlation, route, method, status,
duration, stack and diagnostic client timestamp. Jakarta Bean Validation
enforces type, syntax, size and numeric bounds, while unknown fields fail
deserialization.

A dedicated servlet filter limits the complete request to 16 KiB, including
requests without a declared content length, and safely returns `413` before
JSON parsing.

### Server-Owned Context And Sanitization

The controller derives the authenticated principal, direct network peer,
request correlation and receipt time on the server. Actor and origin are
pseudonymized with SHA-256 before leaving the context service. Browser fields
cannot supply or replace those values; `clientTimestamp` remains separately
named diagnostic input.

Messages and stacks have controls removed and known credentials, JWT shapes,
cookies, documents, emails and telephone numbers redacted. Routes lose query
strings. Client correlation uses the same policy as request correlation.

### Throttling And Output

The application applies fixed-window limits independently to actor and direct
origin. The in-memory map has a configurable hard capacity and rejects new keys
when full, preventing unbounded state. Expired entries are removed on access.

Accepted records cross `ClientLogSinkPort` as a sanitized immutable record.
`Slf4jClientLogAdapter` emits explicit ordered fields through logger
`HOUSEHOST_CLIENT_LOG`, mapping client severity without serializing the raw DTO.
Sink failures become a generic `503`; rate rejection returns `429` and a bounded
`Retry-After` value.

## Technical And MVP Decisions

- `spring-boot-starter-validation` was added because this endpoint requires
  declarative HTTP field validation and the project did not already include it.
- The in-memory limiter defaults to 60 events per actor and 120 per origin per
  minute, with at most 2000 stored keys. All values are externally configurable.
- The limiter is intentionally single-instance. A multi-instance deployment
  must replace it with a shared limiter or an equivalent infrastructure rule.
- Only the servlet container's direct remote address is used. Forwarding
  headers are ignored so an untrusted caller cannot choose the limiter key. A
  deployment behind a proxy therefore groups clients by proxy until a trusted
  proxy policy is explicitly configured.
- Actor and origin references are truncated SHA-256 pseudonyms. No raw email or
  network address is emitted to operational logs.
- Throttling occurs before sanitization so malformed traffic still consumes the
  bounded abuse budget. No application database or business service is called.

## Difficulties, Problems And Resolutions

- Spring MVC slice tests automatically include global servlet filters. The
  correlation policy was extracted into a shared service while the filter kept
  a no-argument construction path, preserving existing isolated and slice test
  behavior.
- Spring Boot's ObjectMapper ignores unknown properties by default. The DTO
  uses an explicit `JsonAnySetter` rejection hook so browser-supplied identity
  or arbitrary structured fields cannot silently enter the contract.
- The worktree contained extensive unrelated user changes. All implementation
  patches were scoped to observability, the necessary security/exception
  integration points and SDD completion documents; unrelated changes were
  preserved.

## Tests And Verification

- Focused observability suite:
  `HOUSEHOST_LOGIN_LIMIT_HMAC_SECRET=test-only-secret ./mvnw -q
  -Dtest='com.househost.observability.**' test` — passed.
- Full clean backend suite:
  `HOUSEHOST_ROOT_LOG_LEVEL=ERROR HOUSEHOST_LOG_LEVEL=ERROR
  HOUSEHOST_LOGIN_LIMIT_HMAC_SECRET=test-only-secret ./mvnw clean test` —
  passed with 168 tests, 0 failures, 0 errors and 0 skipped.
- `git diff --check` — passed before completion-document updates and repeated
  after them.
- Frontend tests were not run because task `014b` changes no frontend code.

## Acceptance Criteria Review

- Anonymous `401` and all current roles accepted: satisfied by MVC security
  tests.
- `WARN`/`ERROR` only and strict allowlist: satisfied by enum deserialization,
  validation and unknown-field tests.
- Field, numeric and 16-KiB total limits: satisfied by validation annotations
  and request-size filter tests.
- Server-owned actor, origin, correlation context and receipt time: satisfied;
  raw actor/origin values are not emitted.
- Log forging and sensitive marker prevention: satisfied by sanitizer and sink
  tests.
- Bounded actor/origin throttling: satisfied by rate-limiter tests and hard map
  capacity.
- Safe sink failure with no business mutation: satisfied by service tests and
  the absence of database/business dependencies.
- Existing backend behavior: satisfied by the complete clean suite.

## Prerequisite Review

### Product And Operational Logging

The endpoint is limited to authenticated administrative browser diagnostics,
keeps operational logging separate from durable audit and uses the existing
SLF4J/Logback outputs established by `013b`. It does not implement frontend
collection or anonymous ingestion reserved for later work.

### LGPD Governance

The implementation minimizes the contract, rejects arbitrary fields, strips
queries and known sensitive values, pseudonymizes server-derived identity and
origin, and creates no new database or indefinite retention path. Existing
Logback retention remains authoritative.

### Module Architecture

The implementation follows the observability hexagonal boundary: REST and web
filters are inbound adapters, the controller depends on a use-case port, the
application service depends on a sink port, and SLF4J is isolated in an
outbound integration adapter. Closed DTO values use a domain enum and internal
immutable carriers are application records with the required naming.

### Contradictions And Final Confirmation

No contradiction was found among the required specs, plan, acceptance criteria
or implementation rules. The user's explicit request changed the order only
for `014b`; no frontend logging task or unrelated proposed backend task was
implemented. The result is conformant and task `014b` is complete.
