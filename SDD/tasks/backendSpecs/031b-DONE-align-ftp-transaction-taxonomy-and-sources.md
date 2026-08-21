# Task 031b DONE — Align FTP Transaction Taxonomy And Sources

## Status

Completed and verified on 2026-08-18 after explicit execution approval.

## Implementation Area

Backend (`b`).

## Objective

Align financial transaction types and source relationships with the FTP model
before any FTP aggregate is persisted.

## Required Implementation Files

- `SDD/specs/sddSpec.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/plans/backendSpecs/financialTransactionPlanBackendPlan.md`
- `SDD/tasks/backendSpecs/029b-DONE-remove-financial-transaction-directional-amounts.md`
- `SDD/tasks/backendSpecs/030b-DONE-notify-financial-source-deletion.md`

## Scope

- Replace transitional transaction type values with the seven authoritative
  FTP classifications.
- Add the PLAN source type while preserving INSTALLMENT ownership.
- Migrate stored classification values without inventing specific purposes.
- Make installment blocks retain their direct purpose and internal installments
  use the internal classification.
- Stop propagating the block's PLAN source to its internal installments.
- Update DTOs, mappers, labels, compatibility migration and focused tests.

## Acceptance Criteria

- Every authoritative transaction type round-trips through domain, JPA and API.
- Existing rows migrate deterministically and legacy values are absent after
  compatibility processing.
- Direct blocks may use a purpose type; internal installments always use
  `INSTALLMENT_TRANSACTION`.
- PLAN direct source and INSTALLMENT internal source remain unambiguous.
- No FTP domain, API or frontend workflow is implemented prematurely.
- Focused migration, mapper and architecture tests plus the full Maven suite
  and `git diff --check` pass.
