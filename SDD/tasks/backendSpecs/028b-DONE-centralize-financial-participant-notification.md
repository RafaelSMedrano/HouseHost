# Task 028b DONE — Centralize Finance Participant Notification

## Status

Completed and verified on 2026-08-13 after correction of the implementation
scope to include the complete Finance module.

## Implementation Area

Backend (`b`).

## Objective

Align Finance module communication with the module architecture by using one
`FinancialParticipantNotifier` to coordinate the specialized party and source
Resolvers and by isolating the Cashier participant behind an integration
adapter and a Cashier-owned inbound use case.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/financialParticipantNotificationBackendPlan.md`

## Dependencies

- Existing financial participant and source ports, adapters and Resolvers.
- Existing Cashier movement application service and persistence ports.

## Scope

- Make `FinancialParticipantNotifier` centralize `FinancialPartyResolver` and
  `FinancialTransactionSourceResolver`.
- Remove `FinancialSourceNotifier`.
- Make the principal financial transaction services inject only the central
  participant notifier for external effects.
- Preserve the distinction between partial installment settlement and complete
  transaction or plan settlement.
- Remove the direct implementation of the transaction submodule's
  `FinancialParty` port from the Cashier application layer.
- Add a transaction-owned Cashier integration adapter and a Cashier-owned
  inbound use case for received financial effects.
- Update focused service, adapter and architecture tests.

## Acceptance Criteria

- `FinancialParticipantNotifier` is the only notifier used by the principal
  financial transaction services.
- Both financial Resolvers are owned by the central notifier and are not
  injected into principal services.
- Ordinary transaction settlement notifies parties before its optional source.
- Individual installment settlement does not notify the source prematurely.
- Completing an installment plan notifies its parties and source exactly once.
- Creation and deletion participant behavior remains unchanged.
- The Cashier application layer does not implement or import `FinancialParty`.
- `CashierFinancialPartyAdapter` implements `FinancialParty` and delegates
  received effects through `CashierFinancialTransactionUseCase`.
- Cashier continues to schedule, settle and reverse the same movements.
- Focused tests, the full Maven suite and `git diff --check` pass.

## Verification Commands

```text
./mvnw test
git diff --check
```

## Required Report

```text
SDD/ImplementationReport/2026-08-13-028b-centralize-financial-participant-notification.md
```

