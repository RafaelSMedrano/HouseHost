# Task 030b DONE — Notify Financial Source On Transaction Deletion

## Status

Completed and verified on 2026-08-18.

## Implementation Area

Backend (`b`).

## Objective

Make financial transaction deletion notify its optional source through the
central `FinancialParticipantNotifier` after notifying sender and receiver and
before removing the transaction from persistence.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/financialParticipantNotificationBackendPlan.md`

## Dependencies

- Existing `FinancialParticipantNotifier`, party Resolver, source Resolver and
  transaction deletion flow.

## Scope

- Add a deletion callback to the financial source port.
- Resolve and notify the optional transaction source during deletion.
- Preserve party cleanup before source notification.
- Preserve deletion behavior for transactions without a source.
- Add focused regression coverage for both paths.

## Acceptance Criteria

- Deletion notifies sender and receiver integrations before its optional
  source.
- Source notification receives the source identifier and deleted transaction.
- A transaction without source does not invoke the source Resolver.
- The principal service continues to depend only on
  `FinancialParticipantNotifier` for external deletion effects.
- Focused tests, the full Maven suite and `git diff --check` pass.

## Verification Commands

```text
./mvnw -q -Dtest=FinancialParticipantNotifierTest,FinancialTransactionServiceAuditTest test
./mvnw test
git diff --check
```

## Required Report

```text
SDD/ImplementationReport/2026-08-18-030b-notify-financial-source-deletion.md
```
