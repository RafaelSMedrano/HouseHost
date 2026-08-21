# Task 029b DONE — Remove Financial Transaction Directional Amounts

## Status

Completed and verified on 17 August 2026 after the user's explicit
authorization.

## Objective

Remove `entryAmount` and `expenseAmount` from the complete backend financial
transaction flow while redefining `type` as a retained transaction
classification with the approved vocabulary.

## Required Implementation Files

- `SDD/specs/sddSpec.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/plans/backendSpecs/financialTransactionDirectionalAmountsBackendPlan.md`

## Scope

- Remove the obsolete directional amount properties from domain and
  persistence models.
- Retain `type` with `STANDARD`, `PLAN_SIGNAL_TRANSACTIONAL`,
  `PLAN_TRANSACTIONAL` and `INSTALLTMENT_PLAN_TRANSACTION`.
- Migrate old direction types to `STANDARD` and remove the two obsolete amount
  columns.
- Update DTOs, services, audit metadata, booking creation, mappers and tests.

## Acceptance Criteria

- Backend financial transactions retain one positive `amount`.
- No backend contract or persisted entity exposes `entryAmount` or
  `expenseAmount`.
- `type` uses only the approved four-value vocabulary.
- Existing transaction rows are retained, legacy types become `STANDARD` and
  the two obsolete amount columns are removed idempotently.
- Focused and full backend verification passes.
- The implementation report and prerequisite review confirm conformity.
