# Implementation Report — Task 013b

## Task

- Task id: `013b`.
- Executed file:
  `SDD/tasks/backendSpecs/013b-DONE-configure-operational-logging-foundation.md`.
- Execution date: 11 August 2026.
- Authorization: the user explicitly requested execution of `013b`; this
  selected the cross-cutting task independently without authorizing other
  proposed tasks.

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

- `src/main/resources/logback-spring.xml`.
- `src/main/java/com/househost/observability/adapter/in/web/CorrelationLoggingFilter.java`.
- `src/test/java/com/househost/observability/LoggingConfigurationTest.java`.
- `src/test/java/com/househost/observability/LogbackRollingPolicyTest.java`.
- `src/test/java/com/househost/observability/adapter/in/web/CorrelationLoggingFilterTest.java`.
- `src/test/java/com/househost/security/adapter/in/config/SecurityConfigCorsTest.java`.
- `src/test/java/com/househost/shared/exception/GlobalExceptionHandlerLoggingTest.java`.
- `SDD/ImplementationReport/2026-08-11-013b-configure-operational-logging-foundation.md`.

## Files Changed

- `.gitignore`.
- `src/main/resources/application.properties`.
- `src/main/java/com/househost/publicapi/adapter/in/web/PublicRequestSizeFilter.java`.
- `src/main/java/com/househost/security/adapter/in/config/SecurityConfig.java`.
- `src/main/java/com/househost/shared/exception/GlobalExceptionHandler.java`.
- `SDD/plans/operationalLoggingTechnicalPlan.md`.
- `SDD/tasks/backendSpecs/013b-DONE-configure-operational-logging-foundation.md`
  (renamed from the proposed filename and marked complete).
- `SDD/tasks/backendSpecs/014b-DONE-implement-client-log-ingestion.md`.
- `SDD/tasks/frontendSpecs/022f-DONE-create-frontend-logger-core.md`.
- `SDD/tasks/frontendSpecs/023f-DONE-capture-global-frontend-errors.md`.
- `SDD/tasks/frontendSpecs/024f-DONE-integrate-logger-administrative-startup.md`.
- `SDD/tasks/frontendSpecs/025f-DONE-integrate-api-log-transport.md`.
- `SDD/implementation/task-bootstrap.md`.
- `SDD/implementation/implementation-order.md`.

## Flows Implemented

### Automatic Backend Output

Logback now writes the same ordered key/value record to standard output and to
`househost.log`. Exact `ERROR` records are duplicated into
`househost-error.log`.

Both file streams use daily and 20-MB size boundaries, gzip archives, 30-day
history and externally configurable caps. The default archive budget is split
between 1536 MB for the general stream and 512 MB for the duplicated error
stream. Logback owns directory/file creation, append, compression and cleanup.

### HTTP Correlation And Completion

`CorrelationLoggingFilter` runs at the highest servlet-filter precedence. It
accepts a conservative correlation identifier up to 64 characters or generates
a UUID, adds it to MDC, returns it through `X-Correlation-ID`, records method,
path, status and monotonic duration, and removes MDC in `finally`.

The public request-size filter now runs immediately after correlation, so early
payload rejection remains correlated and logged. CORS exposes the correlation
header to browser clients.

### Exception Logging

Known application and MVC exceptions produce concise `WARN` events without
their message or throwable. Framework exceptions retain their native HTTP
status through `ResponseEntityExceptionHandler`.

Unexpected exceptions produce an `ERROR`, the original controlled exception
type and stack frames under a generic throwable message. The original exception
message and cause chain are not copied into the operational record, and the
client receives a generic `500` response.

## Technical And MVP Decisions

- No Maven dependency was added. Spring Boot's existing SLF4J and Logback stack
  implements the delivery.
- The first format is one physical key/value line; CR, LF and tabs are removed
  from formatted messages and stack output.
- The current authenticated principal is an email address, so request logging
  deliberately does not emit it as an actor identifier.
- Query strings, bodies and headers are never read for completion logging.
- The Spring Boot category that prints an automatically generated security
  password was fixed at `ERROR`, preventing its `WARN` secret from entering the
  configured outputs. HouseHost uses its own security chain.
- The local `logs/` directory is ignored by Git. Production disk permissions
  and persistent-path provisioning remain deployment responsibilities.
- The error file uses an exact-level filter, matching the spec's error-only
  intent while avoiding accidental duplication of non-error events.

## Difficulties, Problems And Resolutions

- The worktree already contained unrelated and user-owned modifications,
  including the error-dispatch permission in `SecurityConfig`. The logging
  change was applied as a minimal addition and preserved that existing diff.
- Logback 1.4 rate-limits size checks internally to once per minute. Waiting for
  that interval would make the test slow, so the isolated rolling-policy test
  uses the same policy with a test-only 10-ms check interval. It verifies the
  size trigger and a nonempty gzip archive without changing production config.
- Initial real-context output exposed Spring Boot's generated test password.
  The dedicated auto-configuration logger was suppressed before final
  verification, and an isolated log scan confirmed the secret no longer
  appears.
- A generic exception advice can accidentally convert MVC client errors into
  `500`. Extending `ResponseEntityExceptionHandler` retained framework status
  behavior, covered by a missing-parameter `400` test.

## Tests And Verification

- Focused logging suite:
  `HOUSEHOST_LOGIN_LIMIT_HMAC_SECRET=test-only-secret ./mvnw -q
  -Dtest=CorrelationLoggingFilterTest,GlobalExceptionHandlerLoggingTest,SecurityConfigCorsTest,LoggingConfigurationTest,LogbackRollingPolicyTest test`
  — passed.
- Controlled rolling policy:
  `HOUSEHOST_LOGIN_LIMIT_HMAC_SECRET=test-only-secret ./mvnw -q
  -Dtest=LogbackRollingPolicyTest test` — passed and created a nonempty gzip
  archive under a JUnit temporary directory.
- Full clean backend suite:
  `HOUSEHOST_ROOT_LOG_LEVEL=ERROR HOUSEHOST_LOG_LEVEL=ERROR
  HOUSEHOST_LOGIN_LIMIT_HMAC_SECRET=test-only-secret ./mvnw clean test` —
  passed with 152 tests, 0 failures, 0 errors and 0 skipped.
- Real Spring logging output was directed to an isolated temporary directory;
  `househost.log` and `househost-error.log` were created automatically.
- The isolated output was searched for `generated security password`, test
  password, token and document markers; none were found.
- `git diff --check` — passed before completion-document updates and repeated
  after them.
- Frontend tests were not run because task `013b` changes no frontend code.

## Acceptance Criteria Review

- Existing logging dependencies only: satisfied.
- Automatic active general and error files: satisfied in a writable isolated
  path.
- Daily/size gzip policy, history and caps: satisfied by configuration tests and
  the controlled rolling test.
- Standard output: satisfied by the console appender and real test execution.
- Response correlation and supplied/generated identifier rules: satisfied.
- Completion method, normalized path, status and duration: satisfied.
- MDC cleanup on success and exception: satisfied.
- Known versus unexpected exception logging: satisfied.
- Password, token, authorization, query and body marker exclusion: satisfied.
- Existing backend behavior: satisfied by the complete clean suite.

## Prerequisite Review

### Product And Operational Logging

The result matches the mother spec and operational logging spec: it adds
diagnostic traceability, keeps audit persistence separate, implements the
specified files and limits, and does not introduce frontend ingestion reserved
for later tasks.

### LGPD Governance

The filter records only method, path, status, duration and an opaque random
identifier. It does not copy contacts, credentials, tokens, payloads, queries
or authenticated email principals. Retention is finite and configurable. No
new personal-data purpose or persistence table was introduced.

### Module Architecture

The new servlet concern is isolated under the observability inbound web adapter.
No domain, application service, persistence or cross-module dependency was
introduced. Modified Java follows the required formatting and preserves
existing functional contracts.

### Contradictions And Final Confirmation

No contradiction was found among the required specs, plan, task scope,
acceptance criteria or implementation rules. The user explicitly changed the
execution order for `013b`; no other proposed task was skipped or implemented.
The implementation is conformant and task `013b` is complete.
