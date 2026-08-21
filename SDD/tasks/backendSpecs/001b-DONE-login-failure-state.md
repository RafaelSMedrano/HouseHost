# Task 001b DONE — Persist Login Failure State And Policy

## Status

Completed on 24 July 2026. See
`SDD/ImplementationReport/2026-07-24-001b-login-failure-state.md`.

## Implementation Area

Backend (`b`).

## Objective

Create the independent domain policy, protected scope-key derivation and durable
concurrent persistence required to count and restrict login failures, without
yet changing the production login flow.

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

## Dependencies

None.

## Scope

- Create `LoginSecurityScope` and `LoginSecurityControl` in the auth domain.
- Create validated `LoginSecurityPolicyProperties` with spec defaults.
- Create `LoginSecurityPersistencePort` and its JPA adapter, entity, repository
  and mapper.
- Create `LoginSecurityKeyPort` and HMAC-SHA-256 adapter using a dedicated
  external secret.
- Create `LoginSecurityService` with `ensureAllowed`, `registerFailure`,
  `registerSuccess` and cleanup behavior.
- Inject `Clock` for deterministic time.
- Add idempotent MySQL startup compatibility for
  `login_attempt_controls`.
- Add configuration placeholders without adding a real secret.
- Add domain, service, persistence and concurrency tests.

## Out Of Scope

- Calling the policy from `AuthService.login`.
- HTTP `429` and `Retry-After`.
- Login audit events.
- Operational alert delivery.
- Frontend changes.
- JWT validation or revocation.

## Expected Files

Expected additions or changes include:

```text
src/main/java/com/househost/auth/domain/model/LoginSecurityScope.java
src/main/java/com/househost/auth/domain/model/LoginSecurityControl.java
src/main/java/com/househost/auth/application/service/LoginSecurityService.java
src/main/java/com/househost/auth/application/port/out/LoginSecurityPersistencePort.java
src/main/java/com/househost/auth/application/port/out/LoginSecurityKeyPort.java
src/main/java/com/househost/auth/adapter/out/persistence/LoginSecurityPersistenceAdapter.java
src/main/java/com/househost/auth/adapter/out/persistence/LoginSecurityJpaRepository.java
src/main/java/com/househost/auth/adapter/out/persistence/entity/LoginSecurityJpaEntity.java
src/main/java/com/househost/auth/adapter/out/persistence/entity/LoginSecurityPersistenceMapper.java
src/main/java/com/househost/auth/adapter/out/security/HmacLoginSecurityKeyAdapter.java
src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java
src/main/resources/application.properties
.env.example
src/test/...
```

Names may be adjusted only when required by an existing local convention and
must remain traceable in the implementation report.

## Acceptance Criteria

- The model has no JPA annotations.
- Default thresholds and durations exactly match the governing spec.
- Invalid zero or negative configuration fails startup validation.
- Raw email, IP, password and token are absent from persisted scope keys.
- The HMAC secret is external and independent of the JWT signing secret.
- `(scope_type, scope_key)` is unique.
- Ten concurrent pair failures result in count ten and one active restriction.
- Thirty IP failures and twenty account failures produce their specified state.
- Success clears pair/account state and preserves IP state.
- Active restrictions survive application restart against the same database.
- Cleanup never removes active restrictions and removes stale expired state.
- No production login behavior has changed in this task.
- Relevant automated tests pass.

## Verification Commands

At minimum:

```text
./mvnw test
git diff --check
```

Add targeted test commands to the implementation report when available.

## Required Report

Create:

```text
SDD/ImplementationReport/YYYY-MM-DD-001b-login-failure-state.md
```

The report must include the prerequisite review required by
`SDD/specs/sddSpec.md`.
