# Financial Transaction Directional Amounts Frontend Plan

## Governing Specs

- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/plans/backendSpecs/financialTransactionDirectionalAmountsBackendPlan.md`

## Technical Direction

The financial transaction profile consumes the retained neutral `amount` and
new structural `type`, while no longer reading or presenting `entryAmount` and
`expenseAmount`.

Entry and expense presentation in the Cashier dashboard remains derived from
Cashier-owned movements, so its direction and totals remain unchanged.

## Verification

- Run a focused source-contract test for retained and removed response members.
- Run the full frontend Node test suite and JavaScript syntax checks.
- Run `git diff --check`.
