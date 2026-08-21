# Task 022b DONE — Remove Guest Referrer Name

## Status

Completed and verified on 2026-08-12 after explicit user authorization.

## Implementation Area

Backend (`b`).

## Objective

Remove the guest referrer-name attribute from contracts, domain and persistence.

## Required Specs And Plans

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/guestRegistrationPolishSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/plans/backendSpecs/guestRegistrationPolishBackendPlan.md`

## Scope

- Remove `referredBy` from guest request/response, domain, mapper and entity.
- Ignore the obsolete request member during compatibility rollout.
- Drop `guests.referred_by` idempotently, discarding legacy values.
- Preserve `originChannel` and reservation-origin behavior.
- Add focused contract, mapper, domain and migration tests.

## Acceptance Criteria

- Guest contracts and model expose no referrer-name property.
- The obsolete database column is dropped only when present.
- Guest origin channel remains functional.
- Focused backend tests and `git diff --check` pass.

## Required Report

`SDD/ImplementationReport/2026-08-12-022b-remove-guest-referrer-name.md`
