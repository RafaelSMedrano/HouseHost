# Supplier Management Backend Plan

## Governing Specs

- `SDD/specs/backendSpecs/supplierManagementSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- prerequisite: `SDD/specs/cantinhoDasLavandasMainSpec.md`

## Objective

Implement a hexagonal `supplier` backend module that persists the internal
supplier inventory, validates LGPD relationship evidence, protects administrative
access, audits material actions and exposes stable REST contracts to the
administrative frontend.

This plan does not authorize implementation. Its backend task must be approved
and added to `SDD/implementation/implementation-order.md` first.

## Module Boundary

Create an independent top-level module:

```text
com.househost.supplier
├── domain
│   ├── model
│   └── exception
├── application
│   ├── dto
│   ├── port
│   │   ├── in
│   │   └── out
│   └── service
└── adapter
    ├── in/rest
    └── out
        ├── integration
        └── persistence/entity
```

The supplier domain does not depend on Spring, HTTP or JPA. DTO classes remain
in `application/dto` and end with `DTO`. Internal Java records, if introduced,
remain in `application/records` and end with `Record`. Identifiers follow the
Clean Code conventions in `AGENTS.md`.

## Independent Module Boundary

Neither `supplier/domain` nor `supplier/application` imports or injects an
application service, repository, persistence port, JPA entity or adapter owned
by another module. `SupplierService` depends only on supplier-owned contracts,
including:

```text
SupplierPersistencePort
SupplierAuditPort
SupplierReviewerPort
```

Create `SupplierReviewerPort` and `SupplierReviewerAdapter`. The port requests
only the minimal reviewer identity and authorization evidence required by the
supplier use case. The adapter translates that request to the public contract
exposed by the authentication module. `SupplierService` does not inject
`UserService`, `UserPersistencePort`, `AuditEventService`, privacy services or
financial services.

If another module later consumes supplier information, that consuming module
must declare its own purpose-specific output port. Its integration adapter may
call `SupplierUseCase`; its application service must not call `SupplierService`
or supplier persistence directly.

## Domain Design

### Supplier

Create:

```text
Supplier (domain/model; aggregate root)
SupplierStatus (domain/model; enum)
SupplierRiskLevel (domain/model; enum)
SupplierGovernanceStatus (domain/model; enum)
```

`Supplier` state includes:

```text
id
officialName
tradeName
registrationIdentifier
website
countryOfEstablishment
businessContact
privacyContact
incidentContact
internalOwnerUserId
status
createdAt
updatedAt
version
relationshipList
```

`SupplierStatus` values:

```text
ACTIVE
INACTIVE
```

`SupplierRiskLevel` values:

```text
LOW
MEDIUM
HIGH
```

`SupplierGovernanceStatus` values:

```text
DRAFT
PENDING
APPROVED
BLOCKED
INACTIVE
```

The aggregate enforces nonblank official name, at least one relationship,
consistent lifecycle transitions and preservation of relationship history.

### SupplierDataProcessingRelationship

Create:

```text
SupplierDataProcessingRelationship (domain/model; entity)
SupplierDataRole (domain/model; enum)
SupplierContractStatus (domain/model; enum)
SupplierDataDispositionStatus (domain/model; enum)
```

Relationship state includes the fields required by the governing spec:

```text
id
serviceName
description
purpose
personalDataCategories
dataSubjectCategories
processingActions
role
roleAssessment
storageLocations
internationalTransfer
transferMechanism
retentionCriteria
deletionOrReturnProcedure
securityMeasures
incidentNotificationChannel
incidentNotificationExpectation
subOperatorInformation
contractStatus
contractReference
contractStartDate
contractEndDate
responsibilitySummary
riskLevel
governanceStatus
assessmentNotes
reviewedAt
reviewedByUserId
nextReviewDate
endedAt
dataDispositionStatus
dataDispositionNotes
createdAt
updatedAt
version
```

`SupplierDataRole` values match the spec exactly:

```text
OPERATOR
SUB_OPERATOR
INDEPENDENT_CONTROLLER
JOINT_CONTROLLER
RECIPIENT
NO_PERSONAL_DATA
```

Contract status values may be `NOT_REVIEWED`, `ABSENT`, `UNDER_REVIEW`,
`ACTIVE`, `EXPIRED` and `NOT_APPLICABLE`. Data disposition values may be
`NOT_APPLICABLE`, `PENDING`, `RETURNED`, `DELETED` and
`RETAINED_WITH_JUSTIFICATION`.

The domain rejects personal-data fields for `NO_PERSONAL_DATA` and rejects
approval of a personal-data relationship without purpose, role assessment,
locations, retention, deletion, security, responsibility, contract assessment,
reviewer and review date.

For approval, contract assessment accepts only `ACTIVE` or `NOT_APPLICABLE`.
The latter requires a nonblank assessment justification. `NOT_REVIEWED`,
`ABSENT`, `UNDER_REVIEW` and `EXPIRED` block approval.

## Application Contracts

Create:

```text
SupplierUseCase (application/port/in; interface)
SupplierService (application/service; class)
SupplierValidationService (application/service; class)
```

Primary operations:

```text
create (application/service; SupplierService)
findAll (application/service; SupplierService)
findById (application/service; SupplierService)
update (application/service; SupplierService)
changeStatus (application/service; SupplierService)
reviewRelationship (application/service; SupplierService)
```

Use DTOs with explicit names:

```text
SupplierRequestDTO
SupplierRelationshipRequestDTO
SupplierListResponseDTO
SupplierDetailResponseDTO
SupplierRelationshipResponseDTO
SupplierStatusRequestDTO
SupplierReviewRequestDTO
```

List and detail DTOs remain separate. `SupplierListResponseDTO` contains concise
summary fields, while detail returns every relationship field allowed to the
authorized administrative user.

Input enums are domain enums rather than strings. Long free-text fields receive
explicit server-side length limits. Blank optional values normalize to `null`.

## Persistence Ports And Adapters

Create:

```text
SupplierPersistencePort (application/port/out; interface)
SupplierPersistenceAdapter (adapter/out/persistence; class)
SupplierJpaRepository (adapter/out/persistence; interface)
SupplierJpaEntity (adapter/out/persistence/entity; class)
SupplierRelationshipJpaEntity (adapter/out/persistence/entity; class)
SupplierPersistenceMapper (adapter/out/persistence/entity; utility)
```

The persistence port works only with domain models and supports:

- save aggregate;
- find by ID with relationships;
- list with optional name, role, risk, governance-status and supplier-status
  filters;
- detect normalized official-name duplicates;
- detect registration-identifier duplicates when present.

Use two tables:

```text
suppliers
supplier_data_processing_relationships
```

The relationship table references the supplier and is not independently
deleted. Use optimistic versions and indexes for normalized name, supplier
status, relationship role, risk, governance status and next review date.

Extend:

```text
DatabaseSchemaCompatibilityRunner.java
  (config/startup compatibility; DatabaseSchemaCompatibilityRunner)
```

Add idempotent MySQL creation and index compatibility for both tables, matching
the existing project strategy. Do not introduce a new migration framework in
this task.

## REST Adapter

Create:

```text
SupplierController.java (adapter/in/rest; SupplierController)
```

Endpoints:

| Method | Path | Behavior |
|---|---|---|
| `POST` | `/suppliers` | Create supplier aggregate. |
| `GET` | `/suppliers` | Return filtered concise list. |
| `GET` | `/suppliers/{id}` | Return protected full detail. |
| `PUT` | `/suppliers/{id}` | Update supplier and relationship data. |
| `PATCH` | `/suppliers/{id}/status` | Activate or deactivate supplier. |
| `POST` | `/suppliers/{id}/relationships/{relationshipId}/review` | Record review and governance decision. |

The controller depends only on `SupplierUseCase`. Query filters use domain
enums. All responses use the existing `ResponseDTO` envelope.

## Access Control

Extend:

```text
SecurityConfig.java (security adapter/in/config; SecurityConfig)
```

Use the existing administrator roles `CEO`, `CTO` and `ADMIN` for all supplier
inventory endpoints in the MVP, consistent with the current processing-operation
and audit endpoints. Frontend permissions mirror this decision but never replace
backend authorization.

## Audit Integration

Create:

```text
SupplierAuditPort (application/port/out; interface)
SupplierAuditAdapter (adapter/out/integration; class)
SupplierReviewerPort (application/port/out; interface)
SupplierReviewerAdapter (adapter/out/integration; class)
```

Add processing-operation code and catalog entry:

```text
SUPPLIER_GOVERNANCE
```

Record at minimum:

```text
SUPPLIER_CREATED
SUPPLIER_VIEWED
SUPPLIER_UPDATED
SUPPLIER_STATUS_CHANGED
SUPPLIER_RELATIONSHIP_REVIEWED
```

Metadata contains supplier/relationship IDs, status, role, risk and changed
field names. It excludes full contracts, complete narratives, request payloads,
credentials and security secrets.

## Error Handling

Create a supplier domain exception and map expected validation/not-found/conflict
outcomes through `GlobalExceptionHandler` using the project's existing error
envelope. Duplicate identity and invalid governance transitions return generic
business validation errors without database diagnostics.

## Verification Strategy

### Domain And Validation Tests

- reject blank official name and empty relationship list;
- reject inconsistent `NO_PERSONAL_DATA` relationship;
- reject personal-data relationship missing mandatory governance evidence;
- reject approval without reviewer and review date;
- require disposition outcome when deactivating a relationship;
- preserve different roles for different services of one supplier.

### Persistence Tests

- save and reconstruct aggregate with every relationship field;
- detect normalized-name and registration-identifier duplicates;
- apply role, risk, governance and lifecycle filters;
- optimistic version prevents silent lost updates;
- inactive relationships are retained;
- startup compatibility is idempotent for MySQL.

### Service And HTTP Tests

- create, list, detail, update, status and review flows;
- list response remains concise while detail contains full permitted inventory;
- unauthorized and non-administrator access is rejected;
- enums and malformed inputs are rejected;
- audit events occur after successful persistence;
- failures do not produce false success audit events;
- no endpoint hard-deletes a supplier or relationship.

### Architecture Boundary Tests

- reject dependencies from `supplier/domain` or `supplier/application` to
  services, repositories, JPA entities or adapters owned by another module;
- verify that `SupplierService` depends only on supplier-owned ports;
- verify that audit and reviewer access use their respective outbound adapters;
- verify that no external module injects `SupplierService` or accesses supplier
  persistence directly.

## Configuration And Rollout

No external secret is introduced. Deploy the schema and backend before the
frontend. Seed data is not created automatically because supplier classification
requires factual and contractual review by the controller.

The first operational entries should cover hosting/infrastructure, database,
backup, WhatsApp/Meta, domain/DNS, accounting, banking/payment and any support
provider with system access.

## Out Of Scope

- contract or evidence file upload;
- automatic legal-role classification;
- automatic provider discovery from AWS or invoices;
- public display of nominal suppliers;
- electronic contract signature;
- supplier portal or supplier-authenticated access;
- automatic contract renewal or incident notification;
- hard deletion of governance history.
