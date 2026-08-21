# Operational Logging Spec

## Specification

Operational Logging is the cross-cutting HouseHost capability that records
technical events from the Spring Boot backend and browser frontend in a
consistent, searchable and privacy-conscious form.

The capability lets an operator follow one interaction across browser and
server, diagnose failures, measure request behavior and distinguish technical
evidence from durable business audit records. Logging supports operation and
incident investigation; it does not replace product audit trails, metrics,
monitoring or the organizational incident-response process.

## Scope

This spec governs operational logs produced by:

- the Spring Boot application;
- HTTP request and response processing;
- handled and unhandled backend exceptions;
- explicitly selected operational and security events;
- the administrative browser frontend;
- frontend errors safely submitted to the backend;
- the local files, standard output and future collectors that retain those
  records.

It does not require:

- copying all audit events into operational logs;
- storing operational logs in the main application database;
- recording complete HTTP request or response bodies;
- collecting every browser console message;
- selecting a hosted monitoring vendor;
- implementing business analytics from operational log files;
- treating an error or warning as a confirmed security or personal-data
  incident.

The first delivery covers the existing administrative frontend under
`frontend/admin` and the current Spring Boot backend. Other frontend surfaces
may adopt the same contract in later tasks.

## Capabilities

### Use A Common Event Contract

Every operational record has a timestamp, severity, source service, runtime
environment, stable event name and human-readable message. When applicable it
also has a correlation identifier, HTTP method, normalized path, response
status, duration and server-established authenticated actor identifier.

The supported severity contract is:

- `DEBUG` for detailed development diagnosis;
- `INFO` for expected operational milestones;
- `WARN` for unusual or rejected behavior from which the application recovers;
- `ERROR` for interrupted operations and unexpected failures.

Stable event names use a dotted form such as `request.completed`,
`booking.create.failed` and `client.unhandled_error`. Messages may evolve, but
automation and searches rely on the event name and structured fields.

The initial no-extra-dependency backend format is a consistently ordered
`key=value` line. Native JSON output may be introduced later with an explicit
encoder and compatibility review; it is not required for the first delivery.

### Correlate Browser And Backend Activity

Every relevant HTTP request carries an opaque `X-Correlation-ID`. The frontend
may generate it before calling the API. The backend validates a supplied value
and either accepts it or replaces it with a generated value.

The backend returns the effective identifier in `X-Correlation-ID`, includes it
in every log emitted during request processing and makes it available to safe
error responses. The frontend attaches the effective identifier to the
corresponding API result or error log.

Correlation identifiers are random opaque values. They do not encode a user,
email, IP address, booking identifier or other business data.

### Record HTTP Request Outcomes

The backend automatically records one completion event for each application
request with method, normalized path, status and elapsed milliseconds.

Ordinary successful outcomes use `INFO`. Expected client or authorization
rejections may use `WARN`. Server failures use `ERROR`. Health checks, static
assets and other high-volume low-value paths may be excluded or sampled by
explicit configuration.

Query strings and request or response bodies are not part of the default HTTP
log record.

### Record Exceptions Safely

Known business and validation exceptions produce concise `WARN` records without
unnecessary stack traces. Unexpected exceptions produce `ERROR` records with a
server-side stack trace and a generic client response.

Client responses never expose internal exception types, SQL details, secrets or
stack traces. A caller can report the correlation identifier to support an
operator investigation.

### Keep Operational Logs And Audit Distinct

Operational logs answer whether the software is healthy and why a technical
operation failed. Audit records answer who performed a relevant business or
personal-data action and preserve the evidence required by the governing audit
and privacy specs.

An operational event never becomes a substitute for a mandatory audit event.
Business services add explicit operational logs only when they materially help
diagnosis, using identifiers and minimum context rather than complete domain
objects.

### Capture Relevant Frontend Failures

The administrative frontend provides one logger interface for `DEBUG`, `INFO`,
`WARN` and `ERROR`. Development records remain visible in the browser console.
Production remote delivery is limited by default to `WARN` and `ERROR`.

The frontend captures uncaught JavaScript errors and unhandled promise
rejections. API instrumentation records failure status and duration without
logging request bodies, authorization headers or response payloads.

Logging is best effort. A logger, serializer or remote-delivery failure never
breaks the user flow, causes an infinite reporting loop or masks the original
error.

### Receive Browser Logs Through A Controlled Contract

The backend exposes a dedicated authenticated client-log ingestion endpoint.
It accepts a small allowlisted contract, validates severity and event names,
limits field and payload sizes, applies request throttling and emits the
accepted event through the backend logging infrastructure.

The backend derives trusted actor and request context itself. It does not trust
a browser-supplied user identifier, IP address, authorization state or server
timestamp. Malformed or excessive submissions are rejected and do not create
arbitrary structured fields.

Errors that occur before authentication remain in the local browser console in
the initial delivery. Making anonymous client-log ingestion available requires
a later threat and abuse review.

### Minimize And Sanitize Data

Operational logs never intentionally contain:

- passwords or password hashes;
- JWTs, authorization headers, cookies or session secrets;
- database credentials or application secrets;
- complete request or response payloads;
- complete free-text observations;
- card or banking credentials;
- identity documents;
- guest email addresses, telephone numbers or postal addresses;
- browser storage contents;
- complete domain or user objects.

Logging helpers reject or redact sensitive keys such as `password`, `token`,
`authorization`, `cookie`, `documentNumber`, `email`, `phone`, `creditCard` and
equivalent case variations. Free text, URLs and browser stack traces are
length-limited and sanitized. Paths are recorded without query strings.

Internal identifiers may be recorded when necessary to diagnose an operation,
subject to access and retention controls. Personal data is not added merely
because it is available in memory.

### Store And Rotate Backend Logs Automatically

Spring Boot's existing SLF4J and Logback stack writes backend and accepted
frontend events. Application code does not manually open, append, compress or
delete log files.

The default local file layout is:

```text
logs/
├── househost.log
├── househost-error.log
└── archive/
    ├── househost-YYYY-MM-DD.i.log.gz
    └── househost-error-YYYY-MM-DD.i.log.gz
```

`househost.log` is the active general log. `househost-error.log` is the active
error-only log, so an error may intentionally exist in both files. Archived
`.gz` files appear only after rotation.

The default rotation boundary is daily or 20 MB, whichever occurs first. The
default retention is 30 days and the default combined archive cap is 2 GB.
These values and the base path are external configuration. A production server
may use a persistent path such as `/var/log/househost`; the application process
must have write permission.

The application also writes to standard output. Container or managed-platform
deployments may treat standard output as the authoritative source and delegate
retention to the platform. A later Loki/Grafana, Elastic/OpenSearch or equivalent
collector can ingest the same stable fields without changing business code.

### Apply Environment-Appropriate Defaults

Development favors readable console output and permits `DEBUG` for HouseHost
packages. Production defaults to `INFO`, preserves `WARN` and `ERROR`, and
reduces noisy framework categories such as Hibernate and Spring Security unless
temporary diagnosis explicitly enables them.

File location, log levels, retention, archive size and remote frontend delivery
are externally configurable. Secrets are never stored in logging configuration.

### Protect Log Availability And Access

Log directories, collectors and dashboards are restricted to authorized
operators. Retention and deletion apply to local files and any downstream copy.
Backup or export of logs does not silently create indefinite retention.

File logging and remote browser logging are best effort for ordinary operations.
A full disk, unavailable collector or malformed client event must not corrupt
business state. Security controls whose governing specs require fail-closed
behavior retain their existing policy and are not weakened by this rule.

Operators can search at minimum by time, severity, service, environment, event,
correlation identifier, path and status. Later monitoring may alert on elevated
HTTP `5xx`, recurring frontend failures, authentication abuse and latency, but
alert thresholds require operational calibration.

## Prerequisite Specs

- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`

## Spec Degree

2.
