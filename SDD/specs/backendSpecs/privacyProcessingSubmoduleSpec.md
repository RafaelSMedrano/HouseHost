# Privacy Processing Submodule Spec

## Specification

The Privacy Processing submodule is the hexagonal capability that maintains the
controller's simplified inventory of personal-data processing operations. It
describes what processing exists, why it exists operationally, which data and
data subjects are involved, where data comes from, who can access it, who may
receive it, how long it is retained, how it is disposed of and which safeguards
apply.

## Scope

This spec owns `DataProcessingOperation`, its stable codes and names, status,
catalog, validation, CRUD, lookup and generic inventory-review behavior.

It does not own lawful-basis assessment evidence, statutory definitions,
lawful-basis approval, assessment versions or readiness calculation. The legacy
`legalBasis` text remains an inventory summary for compatibility until a
separate migration explicitly removes it; it is not approval evidence.

The structural migration preserves the current `data_processing_operations`
table and all external modules that use stable processing-operation codes for
audit classification.

## Capabilities

### Apply A Complete Hexagon

The submodule uses this package root:

```text
com.househost.privacy.processing
├── domain/model
├── application/dto
├── application/port/in
├── application/port/out
├── application/service
├── adapter/in/rest
├── adapter/out/persistence
└── adapter/out/persistence/entity
```

Domain models contain no JPA or web annotations. Services depend on inbound and
outbound contracts appropriate to their layer. Spring Data and JPA types remain
inside persistence adapters.

### Own Processing Operation Behavior

The submodule supports:

- idempotent initialization by stable operation code;
- creation and update with required inventory fields;
- active and inactive operational status;
- listing and detail queries;
- lookup by identifier and stable operation code;
- accountable generic inventory review;
- preservation of inactive marketing and the active security, supplier and
  privacy-governance operations already defined by the product.

### Offer A Service Capability To Legal Basis

The processing application exposes a cohesive lookup or query capability that
allows the legal-basis application service to confirm that an operation exists
and obtain the minimum operation facts needed for an assessment, including its
identifier, stable code and status.

This collaboration may be a direct service call. The legal-basis submodule does
not receive the processing persistence port or domain repository, and it does
not mutate the operation model.

### Keep Legal Readiness Outside The Processing Core

The processing service returns processing information without querying the
legal-basis persistence port. When the existing HTTP response requires both the
operation and its assessment readiness, the parent privacy inbound composition
combines the processing result with the legal-basis query result.

This rule prevents a processing-to-legalbasis service dependency that would
combine with legalbasis-to-processing validation and create a cycle.

### Preserve External Audit Classification

Stable operation codes remain usable by audit adapters in booking, guest,
stay, finance, authentication, public API, supplier and audit modules. Package
imports may change, but code values and audit-event associations do not.

## Prerequisite Specs

- `SDD/specs/backendSpecs/privacyHexagonalSubmodulesSpec.md`

## Spec Degree

3.
