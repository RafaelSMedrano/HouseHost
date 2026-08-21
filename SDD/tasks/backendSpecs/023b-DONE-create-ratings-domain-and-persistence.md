# Task 023b DONE — Create Ratings Domain And Persistence

## Status

Completed and verified on 2026-08-12.

## Objective

Create the independent ratings module, `Rating` domain model and idempotent
one-to-one booking persistence.

## Required Specs And Plan

- `SDD/specs/sddSpec.md`
- `SDD/specs/bookingServiceRatingSpec.md` and all its prerequisites
- `SDD/plans/backendSpecs/bookingServiceRatingBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- current booking persistence, schema compatibility and module architecture tests

## Scope

- Create ratings module package structure and domain exception/model.
- Model six required integer scores, optional observations and timestamps.
- Create persistence port, JPA entity, mapper, repository and adapter.
- Add idempotent `ratings` schema with score checks and unique non-null booking FK.
- Do not map obsolete generic guest/checkout rating values into criteria.
- Add domain, mapper and schema compatibility tests.

## Acceptance Criteria

- Domain has no JPA/Spring dependency.
- Every score accepts only integers `1..5`; observation limit is 4,000.
- Database enforces one rating per booking and repeated migration is safe.
- Focused tests, full backend suite and `git diff --check` pass.

## Required Report

`SDD/ImplementationReport/2026-08-12-023b-create-ratings-domain-and-persistence.md`

