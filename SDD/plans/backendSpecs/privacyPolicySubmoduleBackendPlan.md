# Privacy Policy Submodule Backend Plan

## Governing Specs

- `SDD/specs/backendSpecs/privacyPolicySubmoduleSpec.md`
- `SDD/specs/backendSpecs/privacyHexagonalSubmodulesSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- prerequisite: `SDD/specs/cantinhoDasLavandasMainSpec.md`

## Objective

Create the `privacy.policy` hexagon, persist immutable policy versions, provide
protected draft/publication operations, expose the current policy publicly and
migrate the trustworthy current version without altering booking evidence.

This plan does not authorize implementation. Task `009b` executes it after the
privacy structural tasks when explicitly approved.

## Domain Design

Create under `privacy/policy/domain/model`:

```text
PrivacyPolicy
PrivacyPolicyStatus
PrivacyPolicyContentHash
```

`PrivacyPolicy` contains the fields defined by the spec and lifecycle methods
for draft update, publication and supersession. Published and superseded
instances reject content mutation.

`PrivacyPolicyContentHash` is an immutable domain value object that formats the
SHA-256 fingerprint as `sha256:<lowercase-hex>`. Hash calculation itself can be
provided by an application collaborator or output capability so the domain does
not depend on infrastructure-specific helpers.

Use a positive integer policy version with a unique constraint. Publication is
immediate in the MVP; scheduled future activation is out of scope.

## Canonical Content

Store content as UTF-8 `LONGTEXT` using a restricted documented format. The
recommended MVP is a structured JSON document with versioned schema containing
sections, headings, paragraphs, list items and approved link descriptors.

Canonical serialization must be deterministic before hashing. Unknown document
nodes, raw HTML, scripts, event attributes and non-HTTP(S) links are rejected.
The public response returns a typed content structure or the canonical document
needed by the safe frontend renderer.

## Application Contracts

Create:

```text
PrivacyPolicyUseCase                   application/port/in
PublicPrivacyPolicyUseCase             application/port/in
PrivacyPolicyPersistencePort           application/port/out
PrivacyPolicyPublisherPort             application/port/out
PrivacyPolicyAuditPort                 application/port/out
PrivacyPolicyService                   application/service
PrivacyPolicyValidationService         application/service
PrivacyPolicyHashService               application/service
```

Administrative operations:

```text
createDraft(request)
updateDraft(id, request)
findAll()
findById(id)
publish(id, authenticatedEmail)
```

Public/current operations:

```text
findCurrentPublished()
requireCurrentPublished(policyId)
```

The last operation is deliberately reusable by public booking through direct
service collaboration. It returns an immutable application record containing
ID, version, content hash and effective time, never a JPA entity.

## Persistence Design

Create the normal port, adapter, mapper, Spring Data repository and
`PrivacyPolicyJpaEntity` under `privacy/policy/adapter/out/persistence`.

Use table `privacy_policies` with the business fields from the spec and a
technical nullable unique current-publication slot where necessary to enforce
one `PUBLISHED` row in MySQL. Publication also locks the current row or uses an
equivalent transaction-safe strategy.

Add idempotent compatibility in `DatabaseSchemaCompatibilityRunner`. Do not
rename existing tables or ever insert policy foreign keys or policy IDs into
bookings. Task `010b` records an independent acceptance snapshot only.

## REST API

Protected administrative routes:

```text
POST /privacy-policies
PUT  /privacy-policies/{id}
POST /privacy-policies/{id}/publish
GET  /privacy-policies
GET  /privacy-policies/{id}
```

Public route:

```text
GET /public/privacy-policy
```

Administrative controllers depend on `PrivacyPolicyUseCase`. The public
controller depends on `PublicPrivacyPolicyUseCase`. Controllers contain no hash,
publication or persistence rule.

Extend `SecurityConfig` so administrative routes require `CEO`, `CTO` or
`ADMIN`; `/public/**` remains public.

## Migration Of Current Policy

Seed version 2 idempotently from the exact meaningful content currently shown
by `politicaPrivacidadeView.js`, effective `2026-07-26`. Convert it into the
canonical safe document without dropping or changing its statements.

Do not create version 1. Do not overwrite a pre-existing version 2. If a version
collision has different content or hash, fail safely and require human
resolution rather than silently replacing evidence.

The static frontend remains in place until task `006f`; task `009b` only creates
the authoritative server document and endpoint.

## Audit Integration

Implement `PrivacyPolicyAuditPort` through a policy-owned integration adapter
calling the audit module. Record draft creation, material update, publication
and supersession with policy ID, version, hash and status. Exclude complete
content and publisher contact data from metadata.

## Verification Strategy

Test domain transitions, immutability, canonical validation, deterministic
hashing, concurrent single-current publication, administrative authorization,
public response minimization, migration idempotency, version collision safety,
audit metadata and full persistence mapping.

Run focused tests, the complete Maven suite and `git diff --check`.

## Out Of Scope

- booking schema and request changes;
- frontend policy loading;
- administrative policy editor UI;
- scheduled publication;
- digital signatures or external timestamp authority;
- inventing historical version 1;
- changing the reviewed policy language.
