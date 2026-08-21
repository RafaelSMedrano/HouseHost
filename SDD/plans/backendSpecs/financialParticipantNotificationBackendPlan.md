# Financial Participant Notification Backend Plan

## Governing Specs

- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`

## Technical Direction

The financial transaction submodule will expose one
`FinancialParticipantNotifier` as the coordination point for every external
effect produced by transaction creation, settlement and deletion.

`FinancialParticipantNotifier` will own both specialized resolution paths:

- `FinancialPartyResolver` selects the participant integration by
  `FinancialPartyType`;
- `FinancialTransactionSourceResolver` selects the source integration by
  `FinancialTransactionSourceType`.

The principal transaction services will inject only the central notifier. The
separate `FinancialSourceNotifier` will be removed.

Cashier is part of the Finance module but is a destination of participant
effects rather than an origin of external mutations in this flow. Its
application layer will expose `CashierFinancialTransactionUseCase`. A
`CashierFinancialPartyAdapter` owned by the transaction submodule will
implement `FinancialParty` and translate notifier callbacks into that Cashier
use case. Cashier therefore does not receive a wrapper `ParticipantNotifier`
without Resolvers or external effects of its own.

## Preserved Behavior

- Creation notifies sender and receiver participants.
- Ordinary settlement notifies sender and receiver participants, then the
  transaction source when present.
- Settling one installment notifies its participants without notifying the
  source before the whole plan is settled.
- Completing an installment plan notifies the plan participants and then its
  source.
- Deletion notifies sender and receiver participant integrations and then the
  transaction source when present, before persistence removes the transaction.
- `FinancialTransactionSource` exposes an optional deletion callback so each
  source integration can resolve the effects applicable to its owning module.
- Cashier scheduling, settlement and reversal behavior remains unchanged while
  the direction of its application boundary is corrected.

## Verification

- Focused notifier and financial transaction service tests.
- Focused deletion tests proving that an optional source is notified after the
  sender and receiver and that a transaction without source does not resolve
  one.
- Architecture verification proving that each principal financial transaction
  service injects one `FinancialParticipantNotifier` and no Resolver or second
  Notifier.
- Architecture verification proving that Cashier application does not
  implement the transaction submodule's outbound participant port.
- Full Maven test suite.
- `git diff --check`.
