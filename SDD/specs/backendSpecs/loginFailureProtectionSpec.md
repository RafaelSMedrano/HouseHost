# Login Failure Protection Spec

## Specification

Login Failure Protection is the administrative authentication capability that
limits repeated invalid login attempts, reduces password-guessing abuse and
produces evidence for security review without revealing whether an account
exists.

The capability treats a failed login as an observable security fact, not as a
confirmed incident. Repeated failures can cause temporary restrictions and
alerts. A human or a separate response process determines whether the behavior
represents user error, automation, credential stuffing or a confirmed security
incident.

## Scope

This spec governs password-based login to the authenticated administrative
experience through `POST /auth/login`.

It covers:

- counting well-formed login attempts rejected because the account does not
  exist or the password is invalid;
- temporary restrictions by account and client network origin;
- uniform responses that do not disclose account existence;
- recording success, failure, restriction and suspicious distributed behavior;
- communicating temporary restrictions to the administrative login interface;
- retaining only the operational state required to enforce the protection.

It does not govern:

- validation of JWTs already issued;
- authorization by role after authentication;
- public reservation rate limits;
- password reset or account recovery;
- multifactor authentication;
- permanent disciplinary account suspension;
- automatic declaration that an incident occurred;
- the full organizational incident-response procedure.

The capability protects system availability as well as credentials. An
attacker must not be able to permanently deny access merely by knowing an
administrator's email address.

## Capabilities

### Count Comparable Authentication Failures

The system counts a request as an authentication failure when the request has a
nonblank email and password and authentication is rejected because the email is
unknown or the password does not match.

Malformed requests without the minimum login fields are rejected but do not
participate in credential-failure counters.

Email comparison is case-insensitive after trimming and normalization. All
windows and restriction times use server time.

### Apply A Pair Restriction

Ten failed attempts for the same normalized email and client IP within a
rolling five-minute window restrict that email-and-IP pair for fifteen minutes.

The restriction prevents additional password comparison and token generation
for the pair while it is active.

### Apply A Network-Origin Restriction

Thirty failed attempts from the same client IP across one or more email values
within a rolling five-minute window restrict that IP for thirty minutes.

The network-origin restriction limits broad password spraying while avoiding a
permanent restriction. It does not replace infrastructure-level rate limiting.

### Detect Distributed Account Targeting

Twenty failed attempts for the same normalized account identity from multiple
client IPs within a rolling ten-minute window restrict the account identity for
fifteen minutes and create a priority security alert.

Unknown and known email values receive equivalent counting behavior so that
restriction behavior does not reveal account existence.

### Prevent Lockout From Becoming Permanent

Every restriction expires automatically. A new request after `blockedUntil`
may proceed without an administrator having to run a cleanup operation.

Additional attempts during a restriction do not create an unbounded extension.
Any escalation policy beyond the defined restriction requires a later spec
change.

### Reset The Appropriate State After Success

A successful login clears active failure state for the successful account and
email-IP pair after any existing restriction has expired.

A successful login does not clear the general failure window for the client IP,
because one successful credential must not erase evidence that the same origin
is spraying other accounts.

### Return Uniform Authentication Outcomes

An ordinary invalid login returns the existing generic invalid-credentials
outcome and does not state whether the email exists.

A request refused by an active restriction returns HTTP `429 Too Many
Requests`, a `Retry-After` header representing the remaining restriction and a
generic message that does not identify which scope caused the restriction.

Responses, timing behavior and restriction behavior must not intentionally
distinguish an unknown email from a known account with an invalid password.

### Preserve Comparable Password-Check Cost

When the email does not identify an account and the request is not already
restricted, the system performs a password-hash comparison against a fixed
dummy hash so that the missing-account path remains comparable to the
wrong-password path.

No token is generated for a failed or restricted request.

### Resolve Client Origin Safely

The capability uses the direct connection address unless forwarded client
headers were supplied by explicitly trusted proxy infrastructure.

Arbitrary `X-Forwarded-For` and `X-Real-IP` values sent directly by a client do
not define the rate-limit identity.

### Persist Enforcement State

Active windows and restrictions survive application restart and remain
consistent when requests are handled concurrently or by more than one
application instance sharing the database.

The state used for an unknown email, known account or IP does not store a raw
password, JWT or authorization header. Keys derived from email or IP are
protected against simple reversal.

Expired enforcement state is removed according to a short operational
retention rule. Audit-event retention remains governed separately.

### Record Security Events

The system records, at minimum:

- `USER_LOGIN_FAILED` for a comparable rejected credential attempt;
- `USER_LOGIN_BLOCKED` when a restriction becomes active;
- `USER_LOGIN_RATE_LIMITED` when an active restriction refuses a request;
- `USER_LOGIN_SUCCEEDED` after authentication succeeds.

Events include only the context required to understand the action, such as
restriction scope, current count, window and duration. Events never contain a
password, password hash, raw JWT, raw authorization header or full request
payload.

Security events concerning unknown emails do not persist the raw attempted
email. A known user may be referenced by its internal identity.

### Create Operational Alerts

Distributed account targeting, broad IP spraying and repeated protection-system
failures create alerts consumable by the configured operational monitoring
channel.

An alert is evidence of suspicious behavior, not an automatic declaration of a
personal-data incident. Alert recipients review the associated evidence and
apply the incident-response plan when warranted.

### Fail Without Silently Disabling Protection

If enforcement state cannot be read or updated consistently, the login flow
does not silently bypass the protection. It refuses authentication with a
temporary service-unavailable outcome and produces an operational error signal.

Audit unavailability remains governed by the audit module's availability
policy, but it does not authorize bypassing the login restriction decision.

### Support Configuration And Calibration

Thresholds, rolling-window durations, restriction durations and operational
retention are external configuration values with the defaults defined by this
spec.

Changing their defaults after observing real usage is a product-policy change
and must remain documented. Secrets used to derive storage keys are not stored
in source control.

## Prerequisite Specs

- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`

## Spec Degree

2.
