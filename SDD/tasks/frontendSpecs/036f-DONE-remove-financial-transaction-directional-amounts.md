# Task 036f DONE — Remove Financial Transaction Directional Amounts

## Status

Completed and verified on 17 August 2026 after backend task `029b`.

## Objective

Remove presentation of obsolete directional amounts while retaining the new
financial transaction type classification.

## Required Implementation Files

- `SDD/specs/sddSpec.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/plans/backendSpecs/financialTransactionDirectionalAmountsBackendPlan.md`
- `SDD/plans/frontendSpecs/financialTransactionDirectionalAmountsFrontendPlan.md`
- `SDD/tasks/backendSpecs/029b-DONE-remove-financial-transaction-directional-amounts.md`

## Scope

- Remove `entryAmount` and `expenseAmount` from the transaction profile.
- Present the retained amount, status and new transaction type.
- Preserve Cashier entry and expense presentation derived from movements.
- Add focused frontend contract verification.

## Acceptance Criteria

- The transaction profile does not access or display either removed property.
- The retained transaction amount and type remain visible.
- Cashier dashboard direction and totals remain unchanged.
- Focused and full frontend verification passes.
- The implementation report and prerequisite review confirm conformity.
