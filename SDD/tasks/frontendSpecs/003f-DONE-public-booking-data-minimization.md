# Task 003f — DONE — Send Minimized Public Booking Data

## Status

Completed and verified after task `005b`.

## Implementation Area

Frontend (`f`).

## Objective

Adapt the public booking experience to the reduced numeric backend contract and
mirror the authoritative text limits.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/publicBookingDataMinimizationSpec.md`

## Required Plans

- `SDD/plans/frontendSpecs/publicBookingDataMinimizationFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`

## Acceptance Criteria

- Quote and booking payloads send numeric adults, children and pets.
- Public form fields mirror backend maximum lengths.
- No removed field is sent by the public frontend.
- Public JavaScript evaluates successfully and the full Maven suite passes.

## Required Report

- `SDD/ImplementationReport/2026-07-26-003f-public-booking-data-minimization.md`
