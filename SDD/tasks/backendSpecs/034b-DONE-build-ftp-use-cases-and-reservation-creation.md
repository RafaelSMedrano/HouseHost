# Task 034b — DONE — Build FTP Use Cases And Reservation Creation

## Status

Completed after explicit user approval on 2026-08-19.

## Implementation Area

Backend (`b`).

## Objective

Expose validated authenticated FTP contracts and create a reservation plus its
complete initial payment allocation atomically.

## Required Implementation Files

- `SDD/specs/sddSpec.md`
- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/financialTransactionPlanSpec.md`
- `SDD/specs/frontendSpecs/financialTransactionPlanAdministrativeExperienceSpec.md`
- `SDD/plans/backendSpecs/financialTransactionPlanBackendPlan.md`
- `SDD/tasks/backendSpecs/033b-DONE-create-ftp-domain-and-persistence.md`

## Scope

- Create FTP DTOs, inbound use cases, validation service, principal service and
  authenticated controller contracts.
- Add minimized operational queries and management profile/deadline/cancel/
  eligible-delete commands.
- Add command idempotence persistence and outcome reconciliation.
- Extend financial source creation notification and implement the PLAN source
  adapter through a narrow participation use case.
- Replace the legacy reservation payment payload with explicit signal,
  check-in and checkout allocation.
- Create booking and FTP in one transaction with exact backend recalculation.
- Create received or scheduled signal correctly and provisional waiting
  check-in/checkout components without provisional audit events.

## Acceptance Criteria

- Reservation creation derives all participants, sources, types and status on
  the backend and rejects incomplete or excessive allocation.
- Received and scheduled signals follow distinct settlement behavior.
- Check-in and checkout components are provisional, waiting and correctly due.
- Replayed idempotence keys return one authoritative booking and FTP.
- PLAN source callbacks attach/detach without recursion.
- Operational queries are minimized; management commands are role restricted.
- Booking-only behavior remains valid when no FTP allocation is applicable.
- Focused contract, security, transaction and idempotence tests plus the full
  Maven suite and `git diff --check` pass.
