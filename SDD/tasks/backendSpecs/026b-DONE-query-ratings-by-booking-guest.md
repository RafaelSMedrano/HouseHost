# Task 026b DONE — Query Ratings By Booking Guest

## Status

Completed and verified on 2026-08-13 after explicit execution approval.

## Objective

Harden the existing booking-derived rating queries and protect rated bookings
from deletion.

## Dependencies

- `SDD/tasks/backendSpecs/024b-DONE-build-ratings-use-cases-and-api.md`

## Required Specs And Plan

- `SDD/specs/sddSpec.md`
- `SDD/specs/bookingServiceRatingSpec.md` and all its prerequisites
- `SDD/plans/backendSpecs/bookingServiceRatingBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- current ratings persistence/API, booking deletion flow and query tests

## Scope

- Verify and refine the existing paginated newest-first general rating
  summaries and guest query through `Rating → Booking → Guest`.
- Prove that the existing summaries return only guest/booking link identifiers,
  evaluation date, six scores and observation without N+1 behavior.
- Integrate the existing `existsByBookingId` operation with rated-booking
  deletion conflict handling.
- Avoid adding rating collections to Guest or Booking domain models.
- Test deterministic order, pagination, query count/N+1 behavior and missing records.

## Acceptance Criteria

- Guest history is resolved through booking ownership.
- Responses expose no unrelated contact, document or financial information.
- Rated booking deletion is blocked without deleting the rating.
- Query behavior is bounded and avoids per-row service/API calls.
- Focused tests, full backend suite and `git diff --check` pass.

## Required Report

`SDD/ImplementationReport/YYYY-MM-DD-026b-query-ratings-by-booking-guest.md`
