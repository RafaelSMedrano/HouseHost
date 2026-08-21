# Task 005b — DONE — Enforce Public Booking Data Minimization

## Status

Completed and verified.

## Implementation Area

Backend (`b`).

## Objective

Implement the reduced typed public contract, authoritative validation,
normalization and request-body size limit.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/publicBookingDataMinimizationSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/publicBookingDataMinimizationBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`

## Acceptance Criteria

- Public DTOs contain no document, email, payment or marketing fields.
- Guest and pet composition is numeric.
- Backend enforces every text and count limit in the governing spec.
- Valid phone is persisted as Brazilian E.164.
- Known CPF/card patterns remain rejected from remaining free text.
- Public mutation bodies above 16 KiB return HTTP 413.
- Focused and full automated tests pass.

## Required Report

- `SDD/ImplementationReport/2026-07-26-005b-public-booking-data-minimization.md`
