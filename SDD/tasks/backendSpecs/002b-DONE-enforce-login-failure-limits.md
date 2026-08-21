# Task 002b DONE — Enforce Login Failure Limits

## Status

Completed on 24 July 2026. See
`SDD/ImplementationReport/2026-07-24-002b-enforce-login-failure-limits.md`.

## Implementation Area

Backend (`b`).

## Objective

Integrate the login-failure policy with the administrative credential flow,
produce uniform HTTP outcomes and security events, and invoke operational
alerts for the high-risk thresholds defined by the spec.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/loginFailureProtectionSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/loginFailureProtectionBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- implementation report produced by task `001b`.

## Dependencies

- Task `001b — Persist Login Failure State And Policy` completed and reviewed.

## Scope

- Add `LoginRequestContext` and trusted client-origin resolution.
- Change `AuthUseCase.login`, `AuthController.login` and `AuthService.login` to
  use the context and `LoginSecurityService`.
- Add comparable dummy BCrypt work for unknown emails.
- Add `LoginTemporarilyBlockedException` and
  `LoginProtectionUnavailableException`.
- Map restrictions to `429` plus `Retry-After` and protection failure to `503`.
- Extend `AuthAuditPort` and `AuthAuditAdapter` for unauthenticated outcomes.
- Record `USER_LOGIN_FAILED`, `USER_LOGIN_BLOCKED`,
  `USER_LOGIN_RATE_LIMITED` and existing success.
- Add `LoginSecurityAlertPort` and a configured operational adapter.
- Invoke alerts for broad IP spraying, distributed account targeting and
  protection-system failure.
- Add unit and HTTP integration tests, including concurrency at the login
  boundary.

## Out Of Scope

- Frontend countdown or error presentation.
- MFA and CAPTCHA.
- Password reset.
- JWT revocation.
- Permanent account suspension.
- Manual unlock endpoint.
- Public reservation rate limiting.
- Incident confirmation or ANPD communication.

## Expected Files

Expected additions or changes include:

```text
src/main/java/com/househost/auth/adapter/in/rest/AuthController.java
src/main/java/com/househost/auth/application/port/in/AuthUseCase.java
src/main/java/com/househost/auth/application/service/AuthService.java
src/main/java/com/househost/auth/application/dto/LoginRequestContext.java
src/main/java/com/househost/auth/application/port/out/AuthAuditPort.java
src/main/java/com/househost/auth/adapter/out/integration/AuthAuditAdapter.java
src/main/java/com/househost/auth/application/port/out/LoginSecurityAlertPort.java
src/main/java/com/househost/auth/domain/exception/LoginTemporarilyBlockedException.java
src/main/java/com/househost/auth/domain/exception/LoginProtectionUnavailableException.java
src/main/java/com/househost/shared/exception/GlobalExceptionHandler.java
src/test/...
```

The trusted-origin adapter and monitoring adapter paths must follow the existing
hexagonal ownership documented in the backend plan.

## Acceptance Criteria

- Nine comparable pair failures do not restrict login.
- The tenth comparable pair failure within five minutes activates a fifteen
  minute restriction.
- Thirty failures from one IP activate a thirty-minute IP restriction.
- Twenty failures for one email across multiple IPs activate a fifteen-minute
  account restriction and invoke the alert port.
- Unknown email and wrong password produce the same generic `401` outcome
  before restriction.
- Unknown email executes dummy BCrypt comparison.
- Active restriction returns generic `429` with valid `Retry-After`.
- Restricted requests do not compare passwords or generate JWTs.
- Protection-state failure returns `503` and never silently authenticates.
- Direct clients cannot rotate identity through forwarded headers.
- Trusted proxy resolution is covered by tests.
- Success clears pair/account state but not IP failure state.
- Required audit transitions are recorded without password, token, raw unknown
  email or request payload.
- The first block transition is distinguishable from later rate-limited
  requests.
- Existing successful login response remains compatible.
- All relevant backend tests pass.

## Verification Commands

At minimum:

```text
./mvnw test
git diff --check
```

The report must list any test not run and the reason.

## Required Report

Create:

```text
SDD/ImplementationReport/YYYY-MM-DD-002b-enforce-login-failure-limits.md
```

The report must compare the result with task `001b`, both governing specs and
the backend plan.
