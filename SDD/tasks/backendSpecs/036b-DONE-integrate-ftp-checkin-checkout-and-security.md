# Task 036b DONE — Integrate FTP Check-In, Checkout And Security

## Status

Completed on 2026-08-19 after explicit execution approval and successful full
verification.

## Implementation Area

Backend (`b`).

## Objective

Integrate scheduled FTP materialization with check-in and checkout, enforce the
complete authorization boundary and verify the backend sequence end to end.

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
- `SDD/tasks/backendSpecs/035b-DONE-implement-atomic-ftp-payment-replacement.md`

## Scope

- Extend check-in and checkout DTOs with optional payment materialization and
  idempotence input.
- Query and replace only the scheduled purpose owned by the operation's booking.
- Keep flows without scheduled payment valid and do not create zero-value FTs.
- Keep checkout extra charges, pending amount and rating behavior independent.
- Enforce operational creation/materialization and management-only complete
  profile, deadline, cancellation and deletion permissions.
- Add architecture, legacy-removal, privacy, audit and complete integration tests.
- Verify schema compatibility from the supported pre-FTP state.

## Acceptance Criteria

- Completed check-in/checkout materializes an eligible scheduled purpose once.
- Simple and installment choices return the definitive updated FTP state.
- Missing scheduled purposes do not block otherwise valid operations.
- Extra charges and ratings retain their existing independent behavior.
- Backend authorization returns the expected forbidden outcomes for every role.
- Architecture tests preserve Notifier/Resolver and module boundaries.
- Migration, focused integration, full Maven suite and `git diff --check` pass.
- The backend FTP implementation sequence is fully verified and reportable.
