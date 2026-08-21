# Implementation Report — Task 005b Public Booking Data Minimization

## Task And Execution

- Task: `005b — Enforce Public Booking Data Minimization`.
- Execution date: 26 July 2026.
- Implementation file: `SDD/implementation/implementation-order.md`.

## Documents Read

- `SDD/specs/sddSpec.md`;
- `SDD/specs/cantinhoDasLavandasMainSpec.md`;
- `SDD/specs/lgpdGovernanceSpec.md`;
- `SDD/specs/moduleArchitectureSpec.md`;
- `SDD/specs/publicBookingDataMinimizationSpec.md`;
- `SDD/plans/backendSpecs/publicBookingDataMinimizationBackendPlan.md`;
- task bootstrap and implementation order.

## Files Created

- `SDD/specs/publicBookingDataMinimizationSpec.md`;
- `SDD/plans/backendSpecs/publicBookingDataMinimizationBackendPlan.md`;
- `SDD/tasks/backendSpecs/005b-DONE-public-booking-data-minimization.md`;
- `src/main/java/com/househost/publicapi/adapter/in/web/PublicRequestSizeFilter.java`;
- `src/test/java/com/househost/publicapi/application/service/PublicBookingServiceTest.java`;
- `src/test/java/com/househost/publicapi/adapter/in/web/PublicRequestSizeFilterTest.java`;
- `SDD/ImplementationReport/2026-07-26-005b-public-booking-data-minimization.md`.

## Files Changed

- `src/main/java/com/househost/publicapi/application/dto/PublicBookingRequestDTO.java`;
- `src/main/java/com/househost/publicapi/application/dto/PublicQuoteRequestDTO.java`;
- `src/main/java/com/househost/publicapi/application/dto/PublicBookingResponseDTO.java`;
- `src/main/java/com/househost/publicapi/application/service/PublicBookingService.java`;
- `src/main/java/com/househost/publicapi/application/port/out/PublicBookingAuditPort.java`;
- `src/main/java/com/househost/publicapi/adapter/out/integration/PublicBookingAuditAdapter.java`;
- SDD implementation files and project documentation.

## Flows Implemented

The public booking contract no longer contains email, document, payment or
marketing fields. Adults, children and pets are numeric. The backend validates
all text and count limits, normalizes accepted Brazilian telephone numbers to
E.164 and continues rejecting known CPF and payment-card patterns from free
text.

Public POST, PUT and PATCH bodies above 16 KiB now receive HTTP 413 before
controller deserialization. Accepted bodies remain readable by the normal
Spring MVC pipeline.

## Technical And MVP Decisions

- A public request always creates a reduced guest without email or document;
  identity reconciliation is intentionally outside this task.
- Telephone support is restricted to Brazilian 10- or 11-digit national
  numbers, optionally prefixed by country code 55.
- The technical composition limit is 20 people and 5 pets; accommodation
  capacity remains authoritative when lower.
- Unknown removed JSON properties are not bound to the DTO and cannot create
  payment or marketing state.

## Tests And Verification

- focused public booking and request-size tests: passed;
- full Maven suite: 74 tests passed;
- removed-field and old-payload scans: passed;
- `git diff --check`: passed.

## Prerequisite And Acceptance Review

The implementation was compared with the mother spec, LGPD governance, module
architecture, task plan and every acceptance criterion. The DTO is reduced,
composition is typed, validation is authoritative in the backend, telephone
normalization and body limits are covered by tests, and audit metadata does not
receive the removed personal or financial fields. No contradiction remains.
