# Data-Subject Request Backend Plan

## Governing Specs

- `SDD/specs/backendSpecs/dataSubjectRequestSubmoduleSpec.md`
- `SDD/specs/dataSubjectRequestWorkflowSpec.md`
- `SDD/specs/backendSpecs/privacyHexagonalSubmodulesSpec.md`
- `SDD/specs/backendSpecs/privacyPolicySubmoduleSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- prerequisite: `SDD/specs/cantinhoDasLavandasMainSpec.md`

## Objective

Create the `privacy.request` hexagon and its secure public and administrative
contracts so a request received through WhatsApp can become an official,
assigned, executed, answered and auditable case.

This plan does not authorize implementation. Task `011b` executes it only after
its dependencies are complete and the task is explicitly approved.

## Domain Design

Create under `privacy/request/domain/model`:

```text
DataSubjectRequest
DataSubjectRequestStatus
DataSubjectRightType
IdentityVerificationStatus
DataSubjectRequestChannel
DataSubjectRequestAction
DataSubjectRequestActionStatus
DataSubjectRequestHistoryEvent
```

The aggregate validates transitions, identity gates, due date, assignment and
terminal states. History and actions are append-only. Domain types contain no
Spring, MVC, security or persistence imports.

## Application Contracts

Create inbound ports for:

```text
DataSubjectRequestInvitationUseCase
PublicDataSubjectRequestUseCase
DataSubjectRequestQueryUseCase
DataSubjectRequestManagementUseCase
```

Create separate DTOs in `application/dto` for REST input/output and immutable
internal results in `application/records`. Every record name ends in `Record`,
every DTO name ends in `DTO`, and variables containing `Optional` end in
`Optional`, following the project Clean Code rules.

Create output ports for persistence, secure-token hashing/generation, clock,
notification, identity evidence, data-context discovery, action execution and
request audit. Use request-owned integration adapters when an external module
or provider is behind the boundary.

Services are separated into intake/invitation, query, management, deadline and
validation responsibilities. Controllers orchestrate HTTP only.

## Persistence Design

Add normalized tables for requests, invitation digests, append-only history,
analysis contexts, execution actions and communications. Use UUID internal IDs,
an independently generated non-sequential public protocol, optimistic locking,
unique idempotency keys and database constraints for active invitations and
protocols.

Store only a digest of invitation and response-access tokens. Encrypt or avoid
high-risk identity evidence as appropriate; define explicit retention metadata
instead of retaining evidence indefinitely. Add idempotent compatibility in
`DatabaseSchemaCompatibilityRunner`.

## REST API

Protected administrative routes:

```text
POST  /data-subject-requests/invitations
GET   /data-subject-requests
GET   /data-subject-requests/{id}
POST  /data-subject-requests/{id}/assign
POST  /data-subject-requests/{id}/identity-verification
POST  /data-subject-requests/{id}/analysis
POST  /data-subject-requests/{id}/actions
POST  /data-subject-requests/{id}/response
```

Public routes:

```text
GET   /public/data-subject-requests/invitations/{token}
POST  /public/data-subject-requests/invitations/{token}/submit
GET   /public/data-subject-requests/status/{accessToken}
GET   /public/data-subject-requests/response/{accessToken}
```

Prefer placing sensitive opaque tokens in an authorization header or one-time
exchange body where frontend navigation permits it. If the invitation arrives
in a URL, redact query/path values from request logging and exchange it
immediately for a scoped short-lived session. Public invalid, expired and
unknown cases receive indistinguishable safe responses.

Extend `SecurityConfig` so administrative routes require `CEO`, `CTO` or
`ADMIN`; public routes remain unauthenticated but rate-limited and token-gated.

## Workflow And Deadline Rules

Generate the protocol at idempotent submission. Calculate due dates on the
server from a versioned deadline policy. Support immediate simplified responses
and a complete-access target of at most 15 days while allowing the configured
legal rule to evolve without frontend changes.

Run an internal overdue query/notification without silently changing case
status. Assignment, verification, analysis, action approval, provider result
and response delivery each produce history and minimized audit evidence.

## Cross-Module Execution

Define a registry of supported processing contexts and action executors. Each
executor returns a typed result with affected context, action, time, evidence
reference and limitation. It may call an owning application service but never a
foreign repository or entity.

Begin with explicit supported contexts already present in the codebase. An
unsupported context remains an open or excepted action with explanation; it is
not reported as deleted. Operator/provider propagation is separately recorded.

## Notification And Safe Delivery

Notification uses a provider-neutral port. Email contains the protocol,
non-sensitive instructions and secure link, not exported personal data. Record
attempts and provider-neutral results. Failed delivery is retryable and does not
complete the case.

Final sensitive results use a short-lived token, identity gate and one-time or
limited retrieval policy. Downloads use safe content types, filenames and cache
headers.

## Verification Strategy

Test domain transitions, identity gates, token hashing/expiry/revocation,
idempotent intake, indistinguishable public errors, authorization, concurrency,
deadline calculation, append-only history, executor evidence, notification
failure, audit minimization and persistence mapping. Run focused tests, the full
Maven suite, architecture import checks and `git diff --check`.

## Out Of Scope

- automatic legal judgment or guaranteed compliance;
- direct WhatsApp provider automation;
- ANPD, court or consumer-body integration;
- unsupported-module deletion implemented inside this submodule;
- frontend pages;
- changing the public policy outside its governed publication lifecycle.
