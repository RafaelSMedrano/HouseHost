# Implementation Report — Task 002b Enforce Login Failure Limits

## Task And Execution

- Task: `002b — Enforce Login Failure Limits`.
- Implementation file: `SDD/implementation/implementation-order.md`.
- Dependency: task `001b`, completed and reviewed before this task.
- Execution date: 24 July 2026.

## Documents Read

- `SDD/specs/sddSpec.md`;
- `SDD/specs/cantinhoDasLavandasMainSpec.md`;
- `SDD/specs/lgpdGovernanceSpec.md`;
- `SDD/specs/moduleArchitectureSpec.md`;
- `SDD/specs/backendSpecs/loginFailureProtectionSpec.md`;
- `SDD/plans/backendSpecs/loginFailureProtectionBackendPlan.md`;
- `SDD/implementation/task-bootstrap.md`;
- `SDD/implementation/implementation-order.md`;
- `SDD/tasks/backendSpecs/001b-DONE-login-failure-state.md`;
- `SDD/tasks/backendSpecs/002b-DONE-enforce-login-failure-limits.md`;
- `SDD/ImplementationReport/2026-07-24-001b-login-failure-state.md`.

## Files And Flows Implemented

The login use case now receives `LoginRequestContext` resolved by a trusted
proxy-aware inbound adapter. `AuthService` validates minimum fields, normalizes
email, checks all restrictions before password work, performs a single real or
dummy BCrypt comparison, records failures, activates restrictions, clears
pair/account state on success, generates the existing JWT response and records
the defined audit events.

Added generic `429` plus `Retry-After`, fail-closed `503`, structured security
alerts for broad IP spraying, distributed account targeting and protection
failure, and explicit-context audit support without raw unknown email or request
payload. Added unit and HTTP adapter tests for the behavior.

## Technical And MVP Decisions

- The dummy BCrypt hash is generated once as a singleton bean with the same
  configured encoder used for real passwords; it is never generated per login.
- Trusted proxies accept exact IP literals or CIDR ranges from external
  configuration. Forwarded values are used only for a trusted direct peer and
  only when the first value is a valid IP literal.
- The initial alert adapter emits a structured event to the dedicated
  `HOUSEHOST_SECURITY_ALERT` logger with an externally configured monitored
  destination. Provider selection remains outside the auth service.
- Alert delivery failure never permits authentication; protection-state failure
  remains fail-closed.

## Difficulties And Resolutions

The first full-suite run failed before test execution because Mockito inline
could not self-attach Byte Buddy on the environment's JDK 24. This also affected
pre-existing tests. Test scope now explicitly uses Mockito's subclass mock
maker, after which the full suite passed. Production behavior is unchanged by
that test resource.

## Tests And Verification

- Targeted domain, service, origin, HTTP and exception tests — passed.
- `HOUSEHOST_LOGIN_LIMIT_HMAC_SECRET=test-only-secret ./mvnw test` — passed:
  46 tests, 0 failures, 0 errors, 0 skipped.
- `git diff --check` — passed.

Coverage includes generic unknown/wrong-credential behavior, dummy BCrypt work,
active restriction short-circuiting, first block audit/alert, fail-closed
protection errors, successful response compatibility, untrusted and trusted
proxy behavior, `429`/`Retry-After`, `503`, policy defaults, rolling windows,
scope thresholds, cleanup behavior and concurrent service failures.

A live multi-process MySQL concurrency/restart test was not run. The persistence
path uses a unique constraint, pessimistic write lock, transactional mutation
and concurrent-create retry as required by the backend plan.

## Prerequisite And Acceptance Review

The result was compared with task `001b`, the mother spec, LGPD governance,
module architecture, login-failure spec, backend plan and all `002b` acceptance
criteria. Malformed requests are rejected before counting; restricted requests
do not compare passwords or generate tokens; unknown email follows the same
generic response and comparable hash path; success preserves IP state; the
first block and later rate limit are separate events; state failure returns
`503`; direct forwarded headers cannot rotate identity; and audit/alert data
exclude passwords, tokens, raw unknown emails and payloads.

No contradiction was found. Task `002b` is complete and conforms to its
prerequisites, with the live-MySQL verification limitation documented above.
