# Task 033b DONE — Create FTP Domain And Persistence

## Status

Completed, verified and reported on 2026-08-18 after explicit execution
approval.

## Implementation Area

Backend (`b`).

## Objective

Create the `FinancialTransactionPlan` aggregate, derived lifecycle and complete
persistence foundation without exposing operational HTTP workflows yet.

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
- `SDD/tasks/backendSpecs/032b-DONE-correct-cashier-schedule-semantics.md`

## Scope

- Create FTP domain model and status enum with derived state.
- Implement defensive construction, ordering, totalization, settlement view,
  queries, addition, removal, replacement, deadline and cancellation rules.
- Enforce two-to-twelve installments and residual cents on the last installment.
- Create persistence entity, mapper, port, adapter, repository and schema.
- Persist stable equal-date component order and optimistic version.
- Load complete aggregates with bounded queries and locking support.
- Keep historical fixed installment plans unmodified.

## Acceptance Criteria

- Domain tests prove every invariant, query and status precedence.
- Direct installment blocks count once in total and expand only for settlement.
- Settled FTPs are immutable and retained; eligible never-settled FTPs follow
  cancellation and deletion rules.
- PLAN membership and stable ordering survive persistence round trips.
- Aggregate loading has bounded query behavior and lock-bearing mutation lookup.
- No public or administrative endpoint is introduced in this task.
- Focused domain/persistence tests, full Maven suite and `git diff --check` pass.
