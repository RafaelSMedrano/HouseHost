# Privacy Policy Submodule Spec

## Specification

The Privacy Policy submodule is the hexagonal capability that governs the
public privacy notice as a versioned, published and historically preserved
document whose authoritative content comes from the backend.

It allows the controller to prepare a draft, publish an immutable version,
retain previous published versions and demonstrate which exact policy was in
force. A content hash supplies a stable integrity fingerprint; the hash alone
is not a digital signature or absolute proof against a privileged attacker.
Evidence comes from the immutable content, hash, publication lifecycle,
authenticated publisher and audit trail together.

## Scope

This spec owns public privacy-policy identity, version, canonical content,
hashing, lifecycle, publication, current-policy lookup, administrative history
and public delivery.

It creates the third hexagonal submodule under:

```text
com.househost.privacy.policy
```

It does not own the booking entity or the public reservation form. Recording an
independent acceptance snapshot in a booking is a downstream integration
governed by this spec and the public-booking specs. Administrative rich-text editing, legal
advice, automatic policy generation and automatic legal approval are outside
scope.

The first governed document is the current public policy identified on the
site as version 2, effective 26 July 2026. No version-1 content is invented when
no trustworthy copy exists.

## Capabilities

### Represent A Privacy Policy Version

Each policy version records:

- stable identifier;
- positive version number;
- title;
- canonical content;
- SHA-256 content hash including an explicit algorithm prefix;
- lifecycle status;
- effective date and time;
- publication date and time;
- authenticated publisher identifier;
- creation and update timestamps.

Lifecycle values are:

```text
DRAFT
PUBLISHED
SUPERSEDED
```

A draft can be edited. A published or superseded version is immutable. A new
text is represented by a new draft with a new version number.

### Use Safe Canonical Content

Canonical content preserves all material information required by the public
notice, including controller, purposes, collected data, sharing, retention,
rights, contact, security language, version and effective date.

Content is not executable HTML. The product uses a documented restricted text
or structured-document format that can be rendered without executing markup,
scripts, event handlers or unsafe URLs. Hash calculation uses the exact
canonical UTF-8 representation after the defined normalization and before
publication.

Once published, title, content, version, hash and effective date do not change.

### Maintain One Current Published Version

At most one policy has `PUBLISHED` status. Publishing a draft occurs atomically:

1. validate complete content and a unique next version;
2. calculate and store its hash;
3. mark the former current policy `SUPERSEDED`, when one exists;
4. mark the selected draft `PUBLISHED`;
5. record publisher and publication time;
6. emit minimized audit evidence.

Concurrent publication cannot produce two current versions. Application
transactions and a database-supported uniqueness mechanism both protect this
invariant.

### Preserve Published History

Published and superseded documents are never edited or hard-deleted through
ordinary product operations. Historical versions remain readable to authorized
administrators. Bookings do not reference policy rows: they preserve their own
immutable version/hash/time acceptance snapshot.

Historical retention does not permit rewriting or inventing evidence. The
initial migration creates version 2 from the exact trustworthy public content.
It does not manufacture version 1 or associate version 2 with earlier bookings.

### Provide Administrative Publication

Only `CEO`, `CTO` and `ADMIN` can create and edit drafts, list all versions,
inspect a version and publish a draft. Backend authorization remains
authoritative.

Publication records the authenticated user and generates an audit event with
policy ID, version, hash and lifecycle status. Complete policy content is not
copied into audit metadata.

### Provide The Current Policy Publicly

An unauthenticated public contract returns the current published policy with:

```text
id
version
title
content
contentHash
effectiveAt
```

Drafts, publisher identity and internal timestamps are not exposed publicly.
If no policy is published, the API returns a controlled unavailable response
rather than an empty or invented policy.

The public endpoint is the source of truth. Static frontend strings, cache-bust
versions and client-provided version labels are not authoritative policy
evidence.

### Integrate Without Crossing Hexagonal Boundaries

The policy domain contains no Spring MVC, JPA, booking or audit imports. Policy
services use their own persistence, publisher-resolution and audit ports.

Public booking application services may call the policy application service
directly to resolve and validate the current published version. Neither module
accesses the other's repository or JPA entity. Audit remains accessed through
module-owned output ports as required by `moduleArchitectureSpec`.

This runtime validation does not create persistence coupling. Booking tables
must not contain a policy ID, foreign key or JPA association to
`privacy_policies`. The policy submodule must remain independently persistable,
migratable and operable.

## Prerequisite Specs

- `SDD/specs/backendSpecs/privacyHexagonalSubmodulesSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`

## Spec Degree

3.
