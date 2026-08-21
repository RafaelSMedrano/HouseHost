para.# Login Failure Protection Backend Plan

## Governing Spec

- `SDD/specs/backendSpecs/loginFailureProtectionSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- prerequisite: `SDD/specs/cantinhoDasLavandasMainSpec.md`

## Objective

Implement durable, concurrent and privacy-conscious login-failure protection in
the `auth` module while preserving the existing separation between password
authentication, JWT validation, audit and HTTP adapters.

This plan does not authorize implementation. Its tasks must first be approved
and added to `SDD/implementation/implementation-order.md`.

## Current Architecture

The current credential flow is:

```text
AuthController.login
        ↓
AuthUseCase.login
        ↓
AuthService.login
        ├── UserPersistencePort.findByEmail
        ├── AuthValidationService.validatePassword
        ├── TokenUseCase.generateToken
        └── AuthAuditPort.recordForExplicitActor
```

Relevant current components:

- `AuthController.java` (adapter/in/rest; `AuthController`)
- `login` (adapter/in/rest; `AuthController`)
- `AuthUseCase` (application/port/in; interface)
- `AuthService` (application/service; class)
- `login` (application/service; `AuthService`)
- `AuthValidationService` (application/service; class)
- `UserPersistencePort` (application/port/out; interface)
- `PasswordPort` (application/port/out; interface)
- `AuthAuditPort` (application/port/out; interface)
- `AuthAuditAdapter` (adapter/out/integration; class)
- `GlobalExceptionHandler` (shared adapter/in/rest advice; class)
- `JwtAuthenticationFilter` (security adapter/in/web; class)

`JwtAuthenticationFilter` remains outside this feature. It authenticates issued
tokens and must not count password attempts.

## Architectural Direction

The `auth` application layer owns the policy. Persistence and HTTP context are
adapters. Audit observes decisions but does not enforce them.

```text
AuthController
    |
    | LoginRequestDTO + LoginRequestContext
    v
AuthService
    |
    +--> LoginSecurityService
    |       |
    |       +--> LoginSecurityPersistencePort
    |       +--> LoginSecurityKeyPort
    |       +--> LoginSecurityAlertPort
    |
    +--> PasswordPort
    +--> UserPersistencePort
    +--> TokenUseCase
    +--> AuthAuditPort
```

The enforcement state is not added to `User`. It has independent scopes,
windows, restrictions and retention.

## Application And Domain Components

### LoginRequestContext

Create:

```text
LoginRequestContext (application/dto; record)
```

Fields:

```text
ipAddress
userAgent
```

`AuthController.login` constructs this context. The application service does
not import `HttpServletRequest`.

### LoginSecurityScope

Create:

```text
LoginSecurityScope (domain/model; enum)
```

Values:

```text
EMAIL_IP
IP
ACCOUNT
```

### LoginSecurityControl

Create:

```text
LoginSecurityControl (domain/model; class)
```

State:

```text
id
scope
scopeKey
failureCount
windowStartedAt
lastFailedAt
blockedUntil
version
```

Behavior:

- determine whether the rolling window is still active;
- restart an expired window;
- increment a comparable failure;
- activate a restriction at the policy threshold;
- report remaining restriction time;
- clear the applicable state after success.

Do not place JPA annotations in this model.

### LoginSecurityPolicyProperties

Create configuration properties with these defaults:

```text
pair.max-failures=10
pair.window=5m
pair.block=15m
ip.max-failures=30
ip.window=5m
ip.block=30m
account.max-failures=20
account.window=10m
account.block=15m
retention=30d
```

Configuration is validated at startup. Zero or negative thresholds and
durations fail startup rather than disabling protection accidentally.

### LoginSecurityService

Create:

```text
LoginSecurityService (application/service; class)
```

Primary operations:

```text
ensureAllowed (application/service; LoginSecurityService)
registerFailure (application/service; LoginSecurityService)
registerSuccess (application/service; LoginSecurityService)
purgeExpiredState (application/service; LoginSecurityService)
```

`ensureAllowed` checks active restrictions before password comparison.

`registerFailure` updates `EMAIL_IP` and `IP` for every comparable attempt. It
also updates `ACCOUNT` using a protected key derived from the normalized email,
regardless of whether that email exists. This preserves equivalent behavior.

`registerSuccess` clears pair and account failure state after authentication.
It does not clear the IP scope.

Use an injected `Clock` so time-window tests do not sleep or depend on wall
clock time.

## Ports And Adapters

### LoginSecurityPersistencePort

Create:

```text
LoginSecurityPersistencePort (application/port/out; interface)
LoginSecurityPersistenceAdapter (adapter/out/persistence; class)
LoginSecurityJpaRepository (adapter/out/persistence; interface)
LoginSecurityJpaEntity (adapter/out/persistence/entity; class)
LoginSecurityPersistenceMapper (adapter/out/persistence/entity; utility)
```

Required persistence operations:

- acquire or create one scope row for atomic update;
- read active scope state;
- clear selected scope state;
- remove state older than retention;
- prevent more than one row for the same scope and key.

### LoginSecurityKeyPort

Create a port for deriving stable, non-reversible scope keys:

```text
LoginSecurityKeyPort (application/port/out; interface)
HmacLoginSecurityKeyAdapter (adapter/out/security; class)
```

Use HMAC-SHA-256 with a dedicated externally supplied secret. Do not reuse the
JWT signing secret. Derived keys may be encoded as lowercase hexadecimal or
Base64URL and stored at fixed maximum length.

The secret is configured by an environment-backed property such as:

```text
HOUSEHOST_LOGIN_LIMIT_HMAC_SECRET
```

The adapter derives keys for normalized email, IP and pair values. Key material
and raw passwords never appear in logs.

### LoginSecurityAlertPort

Create:

```text
LoginSecurityAlertPort (application/port/out; interface)
```

The port receives a structured alert without raw credentials. The first adapter
may emit a structured operational log only if the deployment routes that log to
a monitored recipient. Otherwise execution must supply a real monitoring
adapter before the alert acceptance criteria can pass.

External notification provider selection is not embedded in the auth service.

## Persistence Design

Create table `login_attempt_controls`:

| Column | Type intent | Rule |
|---|---|---|
| `id` | bigint | Primary key. |
| `scope_type` | varchar/enum | `EMAIL_IP`, `IP` or `ACCOUNT`. |
| `scope_key` | varchar(64-128) | HMAC-derived identifier. |
| `failure_count` | integer | Non-negative. |
| `window_started_at` | datetime(6) | Server-controlled. |
| `last_failed_at` | datetime(6) | Server-controlled. |
| `blocked_until` | datetime(6), nullable | Active restriction boundary. |
| `version` | bigint | Optimistic version if used. |
| `created_at` | datetime(6) | Operational trace. |
| `updated_at` | datetime(6) | Cleanup and diagnosis. |

Add a unique index on `(scope_type, scope_key)` and an index supporting cleanup
by `updated_at`.

The repository must serialize concurrent updates. Preferred implementation:

1. acquire an existing row with pessimistic write lock;
2. create the missing row under the unique constraint;
3. if concurrent creation wins elsewhere, retry by loading the unique row;
4. increment and decide restriction in one transaction.

The result must be tested with concurrent requests. Plain read-modify-save
without locking is not acceptable.

The existing project uses Hibernate schema update and
`DatabaseSchemaCompatibilityRunner` (config/startup compatibility; class).
Implementation must add an idempotent `ensureLoginSecurityControlsTable` path for
MySQL compatibility in addition to the JPA mapping. A later migration-tool
adoption can replace this mechanism through a separate architecture change.

## Client Origin Resolution

Extend `AuthController.login` to receive `HttpServletRequest` and create
`LoginRequestContext`.

Do not copy the current unconditional forwarded-header behavior used elsewhere.
Introduce an origin resolver that:

- uses `getRemoteAddr()` by default;
- reads forwarded headers only when the direct peer belongs to configured
  trusted proxies;
- selects and normalizes the first valid client address according to the proxy
  topology;
- rejects malformed values rather than using them as arbitrary keys.

Trusted proxies belong in external configuration.

## Authentication Flow Changes

Change:

```text
AuthUseCase.login (application/port/in; interface)
AuthService.login (application/service; AuthService)
AuthController.login (adapter/in/rest; AuthController)
```

Target sequence:

1. validate required request fields;
2. normalize email;
3. derive keys from email and trusted client IP;
4. call `ensureAllowed`;
5. find the user;
6. compare against the real hash or a fixed dummy BCrypt hash;
7. on failure, atomically register all scopes and audit the outcome;
8. if a threshold activates, create an alert and throw the restriction outcome;
9. on success, clear applicable failure state;
10. generate the JWT;
11. record `USER_LOGIN_SUCCEEDED`;
12. return the existing login response.

The dummy BCrypt hash is generated once for configuration, not once per
request. It must use the same encoder strength as normal passwords.

## Error Contract

Create:

```text
LoginTemporarilyBlockedException (auth domain/exception; class)
LoginProtectionUnavailableException (auth domain/exception; class)
```

`LoginTemporarilyBlockedException` exposes remaining seconds internally.
`GlobalExceptionHandler` maps it to:

```text
status: 429
Retry-After: remaining whole seconds, minimum 1
message: Muitas tentativas de acesso. Aguarde alguns minutos e tente novamente.
```

`LoginProtectionUnavailableException` maps to `503 Service Unavailable` with a
generic temporary-unavailability message.

Existing invalid credentials remain `401` with a generic message.

No response states whether the email exists, which scope was restricted or how
many attempts remain before restriction.

## Audit Integration

Extend:

```text
AuthAuditPort (application/port/out; interface)
AuthAuditAdapter (adapter/out/integration; class)
```

Add an operation for an unauthenticated login subject with explicit request
context. Known users may use internal ID and neutral label. Unknown email values
use only the protected scope key or no subject identifier.

Events:

| Event | When |
|---|---|
| `USER_LOGIN_FAILED` | Comparable credentials rejected. |
| `USER_LOGIN_BLOCKED` | A threshold first activates a restriction. |
| `USER_LOGIN_RATE_LIMITED` | Active restriction refuses a later request. |
| `USER_LOGIN_SUCCEEDED` | Existing successful authentication. |

Metadata is limited to scope, count, window and restriction duration. IP and
User-Agent remain in the audit context rather than duplicated in metadata.

Audit persistence failure follows the audit module's current availability
policy. Login-attempt persistence failure follows this feature's fail-closed
policy.

## Cleanup And Retention

Remove enforcement rows that are no longer blocked and whose `updated_at` is
older than the configured thirty-day retention.

Cleanup is idempotent and may run on a schedule or bounded startup maintenance.
It must not delete active restrictions.

Audit events are not deleted by this cleanup because their retention belongs to
the audit and privacy governance policy.

## Configuration

Add documented environment-backed configuration for:

- all thresholds and durations;
- HMAC secret;
- trusted proxy addresses or networks;
- cleanup retention and schedule;
- monitoring/alert adapter destination.

Add safe placeholders to `.env.example`; never add real secrets to `.env.example`
or source control.

## Verification Strategy

### Domain And Service Tests

- failures 1 through 9 do not restrict the pair;
- failure 10 within five minutes activates the pair restriction;
- an expired rolling window restarts at one;
- pair restriction expires after fifteen minutes;
- IP restriction activates at thirty failures across emails;
- account restriction activates at twenty distributed failures;
- attempts during restriction do not extend indefinitely;
- success clears pair/account but not IP state;
- unknown and known emails follow equivalent branches;
- dummy password comparison is invoked for unknown email;
- no secret or raw credential enters state, metadata or logs.

### Persistence And Concurrency Tests

- unique scope rows survive concurrent first creation;
- ten concurrent failures produce count ten and an active restriction;
- multiple application transactions cannot lose increments;
- cleanup preserves active restrictions and removes expired stale rows;
- application restart retains active restriction state.

### HTTP Integration Tests

- invalid credentials return generic `401`;
- an active restriction returns generic `429` and `Retry-After`;
- the blocked path does not generate JWT;
- malformed requests do not increment comparable-failure counters;
- untrusted forwarded headers cannot rotate the client key;
- trusted proxy configuration resolves the intended client;
- protection persistence failure returns `503` rather than authenticating.

### Audit And Alert Tests

- all four event types are emitted at their defined transitions;
- the first restriction emits one block event rather than one per request;
- rate-limited attempts emit bounded evidence without audit flooding;
- broad IP and distributed-account thresholds invoke the alert port;
- passwords, tokens and raw unknown emails are absent.

## Rollout

1. Deploy persistence and observation with enforcement disabled only in a
   time-bounded non-production calibration environment.
2. Exercise legitimate mistakes and automated test attacks.
3. Enable pair enforcement with the spec defaults.
4. Enable IP and account enforcement after confirming trusted proxy behavior.
5. Confirm monitoring recipients receive structured alerts.
6. Review false positives and change defaults through SDD if required.

Production must not remain indefinitely in observation-only mode because that
would fail the governing spec.

## Out Of Scope

- Redis introduction;
- MFA;
- password reset;
- JWT revocation and logout;
- a security-alert dashboard;
- permanent account suspension;
- public reservation rate limiting;
- full incident-response automation.
