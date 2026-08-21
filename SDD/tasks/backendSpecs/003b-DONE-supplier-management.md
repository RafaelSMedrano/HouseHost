# Task 003b DONE — Implement Supplier Management Backend

## Status

Completed on 26 July 2026. See
`SDD/ImplementationReport/2026-07-26-003b-supplier-management.md`.

## Implementation Area

Backend (`b`).

## Objective

Create the supplier inventory domain, persistence, application services,
protected REST API and audit integration required to register suppliers and
return their governance data to the administrative frontend.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/backendSpecs/supplierManagementSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/supplierManagementBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`

## Dependencies

None beyond the existing authentication, audit and privacy-processing-operation
capabilities described by the required specs and plan.

## Scope

- Create the hexagonal `supplier` backend module.
- Create supplier and processing-relationship domain models and enums.
- Create validation for identity, role, personal-data evidence, approval and
  deactivation consistency.
- Create request, list, detail, relationship, status and review DTOs.
- Create `SupplierUseCase`, `SupplierService` and specialized validation.
- Create persistence port, JPA entities, repository, mapper and adapter.
- Add idempotent MySQL compatibility for supplier tables and indexes.
- Create create/list/detail/update/status/review REST operations.
- Add administrator-only backend authorization.
- Add `SUPPLIER_GOVERNANCE` to the processing-operation catalog.
- Create supplier-owned audit and reviewer ports/adapters; do not introduce
  direct service-to-service communication between modules.
- Create the required audit events.
- Map expected supplier errors through the global error contract.
- Add domain, service, persistence, authorization, HTTP and audit tests.

## Out Of Scope

- Administrative frontend changes.
- Contract or evidence file storage.
- Automatic provider discovery or classification.
- Public policy changes or public supplier disclosure.
- Supplier portal.
- Hard deletion.
- Automatic alerts, renewal or incident workflows.

## Expected Files

Expected additions or changes include:

```text
src/main/java/com/househost/supplier/domain/model/Supplier.java
src/main/java/com/househost/supplier/domain/model/SupplierDataProcessingRelationship.java
src/main/java/com/househost/supplier/domain/model/SupplierDataRole.java
src/main/java/com/househost/supplier/domain/model/SupplierGovernanceStatus.java
src/main/java/com/househost/supplier/application/port/in/SupplierUseCase.java
src/main/java/com/househost/supplier/application/port/out/SupplierPersistencePort.java
src/main/java/com/househost/supplier/application/port/out/SupplierAuditPort.java
src/main/java/com/househost/supplier/application/port/out/SupplierReviewerPort.java
src/main/java/com/househost/supplier/application/service/SupplierService.java
src/main/java/com/househost/supplier/application/service/SupplierValidationService.java
src/main/java/com/househost/supplier/application/dto/...
src/main/java/com/househost/supplier/adapter/in/rest/SupplierController.java
src/main/java/com/househost/supplier/adapter/out/persistence/...
src/main/java/com/househost/supplier/adapter/out/integration/SupplierAuditAdapter.java
src/main/java/com/househost/supplier/adapter/out/integration/SupplierReviewerAdapter.java
src/main/java/com/househost/privacy/domain/model/DataProcessingOperationCodes.java
src/main/java/com/househost/privacy/domain/model/DataProcessingOperationNames.java
src/main/java/com/househost/privacy/application/service/DataProcessingOperationCatalogService.java
src/main/java/com/househost/security/adapter/in/config/SecurityConfig.java
src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java
src/main/java/com/househost/shared/exception/GlobalExceptionHandler.java
src/test/...
```

Names may change only to follow an existing convention or required architecture,
and every adjustment must be traceable in the report. Clean Code conventions in
`AGENTS.md` apply.

## Acceptance Criteria

- Domain models contain no JPA or web annotations.
- A supplier requires official name and at least one relationship.
- One supplier can hold multiple relationships with different LGPD roles.
- `NO_PERSONAL_DATA` rejects contradictory personal-data fields.
- A relationship processing personal data cannot be approved without purpose,
  role assessment, location, retention, deletion, security, responsibilities,
  contract assessment, reviewer and review date.
- Approval accepts only an active contract or a contract marked not applicable
  with a recorded justification; absent, unreviewed, under-review and expired
  contracts block approval.
- Duplicate normalized official names and registration identifiers are rejected
  without automatic merging.
- Create, update, list, detail, status and review operations work through the
  use case and persistence port.
- List responses are concise; detail responses contain all permitted inventory
  evidence.
- Filters support name, role, risk, governance and lifecycle status.
- Deactivation preserves records and requires end/disposition evidence.
- No API hard-deletes suppliers or relationships.
- Only `CEO`, `CTO` and `ADMIN` can access `/suppliers/**`.
- All material successful operations produce the specified audit event after
  persistence.
- `supplier/domain` and `supplier/application` do not import or inject services,
  repositories, persistence ports, JPA entities or adapters from other modules.
- `SupplierService` communicates with external capabilities only through
  supplier-owned ports, implemented by integration adapters.
- No external module calls `SupplierService` or supplier persistence directly;
  a future consumer must use its own port/adapter and the public
  `SupplierUseCase`.
- Audit metadata excludes full request payloads, complete narratives,
  credentials, secrets and contract files.
- MySQL startup compatibility is idempotent.
- Relevant automated backend tests pass.

## Verification Commands

At minimum:

```text
./mvnw test
git diff --check
```

The report must identify any database-specific check not executed and why.

## Required Report

Create:

```text
SDD/ImplementationReport/YYYY-MM-DD-003b-supplier-management.md
```

The report must include the prerequisite review required by
`SDD/specs/sddSpec.md`.
