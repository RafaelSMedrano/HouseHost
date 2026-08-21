# Task 013b DONE — Configure Operational Logging Foundation

## Status

Completed on 11 August 2026. See
`SDD/ImplementationReport/2026-08-11-013b-configure-operational-logging-foundation.md`.

## Implementation Area

Backend (`b`).

## Objective

Configure automatic rolling backend log files and standard output, then add
safe request correlation, HTTP completion records and exception records without
changing business behavior.

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

None beyond the required documents.

## Scope

- Add `logback-spring.xml` with console, rolling general-file and rolling
  error-file appenders.
- Configure daily-or-20-MB gzip rotation, 30-day history and a combined 2-GB
  archive budget split between the general and error-only streams.
- Make the base log path and relevant levels externally configurable.
- Keep the existing Spring Boot SLF4J/Logback dependencies; do not add a logging
  dependency for this text/key-value delivery.
- Add `CorrelationLoggingFilter` with strict incoming identifier validation,
  generated fallback, MDC lifecycle, response header, request timing and one
  completion event.
- Expose `X-Correlation-ID` through CORS.
- Add consistent known-exception and unexpected-exception logging to
  `GlobalExceptionHandler` without returning internal details.
- Exclude query strings, bodies, credentials and sensitive headers.
- Ignore the development `logs/` directory in Git.
- Add focused filter, exception and sanitization tests.

## Out Of Scope

- Browser logger code.
- `POST /client-logs`.
- A hosted log collector or dashboard.
- JSON encoder dependencies.
- Replacing the audit persistence module.
- Adding operational logs to every existing business service.

## Expected Files

Expected additions or changes include:

```text
src/main/resources/logback-spring.xml
src/main/resources/application.properties
src/main/java/com/househost/observability/adapter/in/web/CorrelationLoggingFilter.java
src/main/java/com/househost/shared/exception/GlobalExceptionHandler.java
src/main/java/com/househost/security/adapter/in/config/SecurityConfig.java
.gitignore
src/test/java/com/househost/observability/...
src/test/java/com/househost/shared/exception/...
```

Names may be adjusted only to match a stronger existing convention and must
remain traceable in the implementation report.

## Acceptance Criteria

- The application starts using only its existing logging dependencies.
- A writable configured path receives `househost.log` and
  `househost-error.log` automatically.
- Rotation configuration creates date/index gzip archive names at the defined
  day or size boundary and applies retention and total-size limits.
- Standard output remains available for container collection.
- Every tested API response exposes one valid `X-Correlation-ID`.
- A valid caller identifier is propagated; a missing, oversized or malformed
  identifier is replaced.
- Completion logs contain event, method, normalized path, status and duration.
- MDC is always cleared, including when the filter chain throws.
- Known exceptions log without unnecessary stack traces; unexpected exceptions
  log a server-side stack trace and return a safe response.
- Logs do not contain test marker passwords, tokens, authorization headers,
  query parameters or request bodies.
- Existing backend behavior and tests remain valid.

## Verification Commands

At minimum:

```text
./mvnw test
git diff --check
```

Add a controlled rolling-policy verification to the implementation report.

## Required Report

Create:

```text
SDD/ImplementationReport/2026-08-11-013b-configure-operational-logging-foundation.md
```
