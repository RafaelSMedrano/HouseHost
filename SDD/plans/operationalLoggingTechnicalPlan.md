# Operational Logging Technical Plan

## Governing Specs

- `SDD/specs/operationalLoggingSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- prerequisite: `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`

## Objective

Introduce correlated operational logging across the Spring Boot backend and the
administrative browser frontend while using Spring Boot's existing SLF4J and
Logback stack, preserving the audit module's separate responsibility and
preventing sensitive information from entering logs.

This plan does not authorize application-code implementation. Its six proposed
tasks must be approved and placed in
`SDD/implementation/implementation-order.md` before execution.

## Current Architecture

The backend uses Spring Boot 3.2.5. Its web and security starters transitively
provide `spring-boot-starter-logging`, SLF4J and Logback, so ordinary formatted
logging, rolling files and gzip archives require no new Maven dependency.

Relevant backend components are:

- `application.properties` (runtime configuration);
- `SecurityConfig` (security adapter/in/config; class);
- `JwtAuthenticationFilter` (security adapter/in/web; class);
- `GlobalExceptionHandler` (shared adapter/in/rest advice; class);
- `ResponseDTO` (shared DTO; class);
- `AuditEventService` (audit application/service; class);
- `OperationalLogLoginSecurityAlertAdapter` (auth adapter/out/integration;
  class).

The frontend is native browser JavaScript with ES modules and no bundler.
Relevant components are:

- `frontend/admin/js/api.js` (HTTP/session integration module);
- `frontend/admin/js/controllers/main.js` (administrative bootstrap module);
- `frontend/admin/tests/api.test.mjs` (Node test module).

The current frontend has direct `console` calls and no common logging contract.
The current API layer does not propagate a correlation header. The backend has
selected SLF4J calls but no project-wide request logging or rolling-file policy.

## Target Architecture

```text
Browser operation
    |
    +--> frontend logger --> development console
    |
    +--> api.js -- X-Correlation-ID --> CorrelationLoggingFilter
    |                                      |
    |                                      +--> MDC
    |                                      +--> request.completed
    |                                      +--> application SLF4J logs
    |
    +--> authenticated WARN/ERROR --> POST /client-logs
                                             |
                                             v
                                      ClientLogUseCase
                                             |
                                             v
                                      ClientLogSinkPort
                                             |
                                             v
                                       SLF4J / Logback
                                             |
                         +-------------------+-------------------+
                         |                   |                   |
                       stdout      logs/househost.log   error-only log
```

The existing audit path remains independent:

```text
business use case --> audit port --> audit persistence
```

Operational logging must not replace that path.

## Backend Logging Foundation

### Logback Configuration

Create `src/main/resources/logback-spring.xml` with:

- a console appender;
- a rolling general-file appender;
- a rolling error-only appender;
- a time-and-size rolling policy;
- gzip archive naming with date and index;
- 20 MB per archive part;
- 30-day history;
- a combined 2 GB archive budget, allocated as 1.5 GB for the general stream
  and 512 MB for the duplicated error-only stream;
- Spring profile sections for development and production levels;
- configurable `HOUSEHOST_LOG_PATH`, defaulting to `./logs`.

The general file receives the configured threshold and therefore also contains
errors. The error file uses an error-level filter. Logback creates parent
directories, active files, archives and retention cleanup when the process has
filesystem permission. Application code performs none of those file operations.

The initial pattern is single-line and consistently ordered, for example:

```text
timestamp=2026-08-11T15:10:30.123-03:00 level=INFO service=househost-backend environment=production correlationId=abc123 event=request.completed method=GET path=/rooms status=200 durationMs=42 logger=com.househost.observability.adapter.in.web.CorrelationLoggingFilter message="HTTP request completed"
```

Values controlled by callers must be normalized to prevent newline injection.
Full JSON is deferred because Spring Boot 3.2's existing logging stack does not
by itself provide the desired JSON encoder contract.

### Correlation Filter

Create:

```text
CorrelationLoggingFilter (observability adapter/in/web; class)
```

The filter extends `OncePerRequestFilter` and:

1. reads `X-Correlation-ID`;
2. accepts only a conservative ASCII/UUID-like shape and maximum length;
3. generates an opaque identifier when absent or invalid;
4. adds it to SLF4J MDC under `correlationId`;
5. sets the response header before continuing the chain;
6. measures elapsed monotonic time;
7. logs one completion outcome after the chain returns or throws;
8. derives the authenticated actor only from Spring Security when available;
9. records the path without query parameters;
10. clears MDC in `finally` to prevent cross-request leakage.

The filter must cover Spring Security rejection responses as well as controller
responses. `SecurityConfig.corsConfigurationSource` exposes
`X-Correlation-ID` to browser JavaScript.

### Exception Logging

Extend `GlobalExceptionHandler` to consistently log mapped failures and add a
final handler for unexpected exceptions. Known business outcomes log a concise
`WARN`; unexpected failures log `ERROR` with the throwable.

The response continues using the project's safe response envelope. Correlation
is authoritative in the response header; if a response-body field is added,
the compatibility impact on `ResponseDTO` and existing frontend tests must be
reviewed first.

Request-completion logging and exception logging have distinct events and may
both exist for a failure: one describes the HTTP outcome and the other describes
the cause.

## Backend Client-Log Ingestion

Implement an `observability` module with the following boundaries:

```text
ClientLogController (adapter/in/rest; class)
ClientLogUseCase (application/port/in; interface)
ClientLogService (application/service; class)
ClientLogSinkPort (application/port/out; interface)
Slf4jClientLogAdapter (adapter/out/integration; class)
ClientLogRequestDTO (application/dto; record or class)
```

`POST /client-logs` is authenticated. `SecurityConfig` permits it to all current
authenticated roles but not anonymous callers.

The accepted request contains an allowlisted subset of:

```text
level: WARN | ERROR
event
message
correlationId
route
method
status
durationMs
stack
clientTimestamp
```

Bean validation and explicit normalization apply per-field maximums and reject
unknown or malformed data. The controller enforces a small total request size.
The endpoint is throttled per trusted authenticated identity and network origin;
the implementation may use a bounded in-memory limiter for the current
single-instance deployment, but that limitation must be documented. A later
multi-instance deployment requires a shared limiter or infrastructure policy.

The server uses its current timestamp, authenticated actor and resolved request
context. The client timestamp is diagnostic only. Client `correlationId` is
accepted only through the same validation policy as the request filter.

`Slf4jClientLogAdapter` uses a dedicated logger name such as
`HOUSEHOST_CLIENT_LOG` and maps the accepted severity to SLF4J. It never logs the
raw DTO or arbitrary map.

## Frontend Logger

Deliver the frontend logger in three incremental tasks. Create
`frontend/admin/js/logger.js` with:

```text
logger.debug(event, message, context)
logger.info(event, message, context)
logger.warn(event, message, context)
logger.error(event, message, error, context)
installGlobalErrorLogging()
```

The exact ergonomic signature may be refined by tests, but event, message and
context must remain distinct. The module:

- validates levels and event names;
- copies only allowlisted scalar context;
- recursively rejects known sensitive key names when handling an `Error` or
  defensive context input;
- strips URL query strings;
- truncates message and stack values;
- uses the browser console in development;
- delegates remote delivery through an injected transport;
- catches its own formatting and transport failures;
- prevents recursive reporting.

The core logger and sanitization are delivered first without application
integration. `installGlobalErrorLogging` is delivered next and attaches `error`
and `unhandledrejection` listeners once. The final frontend-logger task updates
`main.js` to initialize the logger before starting the administrative UI and
replaces the current startup `console.log` with a stable logger event.

## API Correlation And Remote Transport

Extend `frontend/admin/js/api.js` so `apiRequest`:

- generates one correlation identifier per API call, preferring
  `crypto.randomUUID()` with a safe fallback;
- sends `X-Correlation-ID`;
- measures duration with a monotonic clock when available;
- reads the effective response correlation header;
- adds status, retry information and correlation to `ApiError`;
- emits an API failure event without body, token, full response or query string;
- avoids treating expected cancellation as an unexpected error when introduced.

Create a remote transport that posts only `WARN` and `ERROR` records to
`/client-logs`. It uses the normal authenticated token but must not call the
instrumented `apiRequest`, preventing a log-send failure from reporting itself.
It applies a small queue, deduplication window and retry ceiling. `sendBeacon`
may flush already-sanitized queued records on page lifecycle events only when
the browser can send the required authenticated request safely; otherwise the
queue is discarded rather than weakening endpoint authentication.

## Storage And Deployment

The supported initial modes are:

| Environment | Output | Retention owner |
|---|---|---|
| Development | Readable console and optional `./logs` files | Developer machine |
| Traditional production server | stdout plus rolling files under configured persistent path | Logback/local operations |
| Container or managed platform | key/value stdout; rolling files optional | Platform collector |

The `logs/` development directory must be ignored by Git. Production operators
must provision disk space, permissions, access controls and downstream
retention. Loki/Grafana, Elastic/OpenSearch or another collector is a deployment
extension, not a prerequisite for these six tasks.

## Privacy And Security Controls

Both sides share the denylist defined by the governing spec. Tests use marker
secrets and assert that output does not contain them. The implementation never
logs request bodies or blindly serializes JavaScript objects, Java exceptions,
DTOs, entities or HTTP headers.

Log forging is mitigated by removing CR/LF and other control characters from
client-controlled single-line values. The ingestion endpoint has authentication,
authorization, payload size, validation and throttling controls. Access to log
files is an operational permission, not an application-user capability.

## Test Strategy

Backend tests cover:

- supplied, missing and invalid correlation identifiers;
- response-header exposure and MDC cleanup;
- completion severity, status and duration;
- handled versus unexpected exception logging;
- rolling configuration startup;
- client-log authentication, validation, size and throttling;
- server-owned actor context;
- sensitive marker absence and control-character normalization.

Frontend Node tests cover:

- logger level and event contracts;
- redaction, allowlisting, URL normalization and truncation;
- idempotent global handlers;
- correlation request and response propagation;
- `ApiError` correlation and timing;
- remote level filtering, recursion protection, deduplication and bounded retry;
- continued application behavior when console or transport fails.

The complete Maven and frontend Node suites run after their respective tasks.

## Proposed Task Breakdown

1. `013b-DONE-configure-operational-logging-foundation.md` — Logback output,
   rotation, request correlation and exception logging.
2. `014b-DONE-implement-client-log-ingestion.md` — authenticated, validated and
   throttled browser-log endpoint.
3. `022f-DONE-create-frontend-logger-core.md` — browser logger core, sanitization,
   local console policy and safe transport boundary.
4. `023f-DONE-capture-global-frontend-errors.md` — idempotent uncaught-error and
   unhandled-rejection capture.
5. `024f-DONE-integrate-logger-administrative-startup.md` — administrative startup
   integration and complete local logger verification.
6. `025f-DONE-integrate-api-log-transport.md` — API correlation and safe remote
   transport.

Tasks 2 and 3 may be implemented after task 1 independently. Task 4 depends on
task 3. Task 5 depends on tasks 3 and 4. Task 6 depends on tasks 1 through 5.
