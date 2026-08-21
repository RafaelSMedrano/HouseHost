# Task 004b DONE — Add Security And Audit Processing Inventory

## Status

Completed on 26 July 2026. See
`SDD/ImplementationReport/2026-07-26-004b-security-audit-processing-inventory.md`.

## Implementation Area

Backend (`b`).

## Objective

Create the explicit security/audit processing operation and guarantee that the
legacy marketing operation remains inactive.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/securityAuditProcessingInventoryBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`

## Scope

- Add security/audit processing-operation code, name and catalog entry.
- Route security login outcomes to the new operation.
- Keep successful login and ordinary user events under user access management.
- Create marketing as inactive and deactivate an existing active catalog record.
- Preserve legacy marketing records and audit relationships.
- Add regression tests and implementation evidence.

## Acceptance Criteria

- A new catalog contains the active security/audit operation.
- A new catalog contains marketing with `INACTIVE` status.
- Startup changes an existing active marketing operation to `INACTIVE`.
- Security login outcomes use `SECURITY_AUDIT_MANAGEMENT`.
- Ordinary user-access audit events continue using `USER_ACCESS_MANAGEMENT`.
- Historical marketing records are not deleted.
- Relevant tests and the full Maven suite pass.

## Required Report

Create `SDD/ImplementationReport/2026-07-26-004b-security-audit-processing-inventory.md`.

