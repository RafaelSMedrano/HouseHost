# Task 032b DONE — Correct Cashier Schedule Semantics

## Status

Completed after explicit execution approval.

## Implementation Area

Backend (`b`).

## Objective

Make Cashier waiting projections, due dates, settlement dates and reversal
semantics safe for future FTP replacement.

## Required Implementation Files

- `SDD/specs/sddSpec.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/plans/backendSpecs/financialTransactionPlanBackendPlan.md`
- `SDD/tasks/backendSpecs/031b-DONE-align-ftp-transaction-taxonomy-and-sources.md`

## Scope

- Represent expected due date and actual settlement date separately in Cashier
  entry and expense models, persistence and contracts.
- Map legacy ambiguous movement dates without fabricating historical settlement
  evidence.
- Copy transaction due date during scheduling and actual settlement date during
  settlement.
- Keep scheduling and reversal idempotent by transaction and cashier.
- Lock Cashier balance mutation against concurrent lost updates.
- Prove waiting reversal never changes realized balances.

## Acceptance Criteria

- Waiting movement creation changes only waiting and expected projections.
- Waiting reversal restores projections and leaves `cashOnHand`, total inflow
  and total outflow unchanged.
- Settlement changes realized balances exactly once and records its actual
  date without overwriting due date.
- Repeated scheduling, settlement or reversal is safe and deterministic.
- Compatibility migration preserves legacy dates under an explicit meaning.
- Focused Cashier, migration and concurrency tests plus the full Maven suite
  and `git diff --check` pass.
