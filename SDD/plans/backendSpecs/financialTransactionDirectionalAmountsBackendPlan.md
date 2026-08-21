# Financial Transaction Directional Amounts Backend Plan

## Governing Specs

- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`

## Technical Direction

`FinancialTransaction` represents one positive `amount` transferred from its
sender to its receiver. Sender and receiver positions determine whether the
transaction produces an entry or expense for a specific Cashier participant.

The obsolete `entryAmount` and `expenseAmount` properties will be removed from
the domain, response DTO, JPA entity, mapper and administrative presentation.
`type` remains as a structural classification using only `STANDARD`,
`PLAN_SIGNAL_TRANSACTIONAL`, `PLAN_TRANSACTIONAL` and
`INSTALLTMENT_PLAN_TRANSACTION`.

Ordinary booking payments use `STANDARD`. `InstallmentPlanTransaction` and its
internal installments use `INSTALLTMENT_PLAN_TRANSACTION`.

## Database Migration

The MySQL schema compatibility runner normalizes every retained amount with
`abs(amount)`, removes `entry_amount` and `expense_amount`, converts the legacy
direction types to `STANDARD` and retains `type` as a required `varchar(50)`.
The migration is idempotent and preserves transaction identities and remaining
financial state.

## Verification

- Compile the backend after updating constructors and mappers.
- Verify transaction, installment, participant and Cashier tests.
- Verify the compatibility migration and approved type vocabulary.
- Verify that no backend contract contains either obsolete directional amount.
- Run the full Maven test suite and `git diff --check`.
