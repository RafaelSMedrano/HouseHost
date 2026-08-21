# Privacy Hexagonal Submodules Spec

## Specification

Privacy Hexagonal Submodules is the structural capability that organizes the
backend `privacy` module as a parent boundary composed of cohesive
hexagonal submodules:

- `processing`, which owns the inventory and review of personal-data
  processing operations;
- `legalbasis`, which owns lawful-basis definitions, assessments, evidence,
  versioning, readiness and accountable approval;
- `policy`, which owns public privacy-policy content, immutable versions,
  publication, public delivery and publication evidence;
- `request`, when introduced by its dedicated descendant spec, which owns the
  official data-subject request workflow, evidence and coordination.

The split makes ownership explicit without turning the submodules into
separately deployed systems. They remain parts of the same Spring Boot
application and may collaborate directly at the application-service layer.

## Scope

This spec governs the package boundaries, dependency direction and migration of
the existing classes under `com.househost.privacy`. It is a structural
refactoring and must preserve current REST contracts, persisted data, approved
assessment history, catalog initialization, authorization, audit events and
frontend behavior.

The parent `privacy` boundary may retain thin composition entry points and
startup coordination that genuinely require multiple submodules. It must not become
a third location for unclassified domain models, persistence ports or business
rules. Every privacy business type belongs to `processing`, `legalbasis`,
`policy` or, after its descendant spec is implemented, `request`.

Data-subject request management, retention execution and incident management
are not introduced by this split. They require their own product specs if later
added as privacy capabilities. Public privacy-policy version governance is
defined by the dedicated policy-submodule descendant spec.

## Capabilities

### Establish The Target Structure

The target package structure is:

```text
privacy/
├── processing/
│   ├── domain/
│   ├── application/
│   └── adapter/
├── legalbasis/
│   ├── domain/
│   ├── application/
│   └── adapter/
├── policy/
│   ├── domain/
│   ├── application/
│   └── adapter/
├── request/                    added by its dedicated descendant spec
│   ├── domain/
│   ├── application/
│   └── adapter/
└── adapter/in/
    ├── config/       parent startup composition when required
    └── rest/         parent HTTP composition when one response needs both
```

The structural split tasks create `processing`, `legalbasis` and `policy`.
`request` is created only by its dedicated product task. Each present submodule
independently applies the domain, application and adapter rules
from `moduleArchitectureSpec`. The parent composition area contains no JPA
repository, domain entity or duplicated rule.

### Permit Direct Service Collaboration

Application services in `processing`, `legalbasis`, `policy` and `request` may
communicate directly when those submodules are present.
An internal port and integration adapter are not required merely because the
classes live in different privacy submodules.

Direct collaboration remains subject to these limits:

- the called service exposes a coherent application capability;
- neither service accesses the other submodule's repository, persistence port,
  JPA entity or persistence adapter;
- domain models are not modified outside their owning submodule;
- the service dependency graph contains no cycle;
- parent composition is used when an aggregate response needs both submodules
  and direct calls in both directions would create a cycle;
- audit and supplier boundaries continue following the explicit port and
  adapter exceptions from `moduleArchitectureSpec`.

The intended dependency direction for business validation is:

```text
legalbasis application service
  → processing application service
      → processing domain and persistence port
```

The processing core does not access legal-basis persistence. A parent inbound
composition can read both capabilities to preserve the current operation
profile response.

### Preserve Independent Ownership

`processing` is the only owner of a processing operation's identity, stable
code, descriptive inventory fields, operational status, generic inventory
review and processing catalog.

`legalbasis` is the only owner of lawful-basis types, statutory references,
sensitive-data basis types, assessment evidence, lifecycle, approval,
versioning and readiness calculation.

`policy` is the only owner of public privacy-policy versions, canonical content,
content hashes, publication lifecycle and current-policy lookup.

`request`, after its descendant spec is implemented, is the only owner of
official data-subject request protocols, lifecycle, identity state, request
history, execution coordination and response evidence.

An operation can refer to legal-basis readiness in a composed response, but
readiness is not state stored or decided by the processing-operation domain.
An assessment can refer to an operation identifier, but it does not own or
reconstruct the operation domain.

### Preserve Data And External Contracts

The refactoring does not rename database tables, columns, enum persistence
values, stable operation codes, endpoint paths or response JSON properties.
Approved assessments remain approved with the same reviewer, timestamps,
version links and narratives.

Spring component scanning, transaction behavior, security matchers, audit
classification and idempotent catalog initialization continue working after
package movement.

### Verify The Boundary

Architecture verification must prove:

- no type in a submodule imports the other submodule's persistence packages;
- no direct service dependency cycle exists;
- every business class previously under the undivided privacy package has an
  explicit owner;
- the parent package contains only justified composition adapters;
- current focused tests and the full backend suite pass after each migration
  task.

## Prerequisite Specs

- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`

## Spec Degree

2.
