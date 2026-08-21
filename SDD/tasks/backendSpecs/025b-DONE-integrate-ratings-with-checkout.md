# Task 025b DONE — Integrate Ratings With Checkout

## Status

Completed and verified on 2026-08-13.

## Objective

Create exactly one complete booking rating atomically when checkout completes.

## Dependencies

- `SDD/tasks/backendSpecs/024b-DONE-build-ratings-use-cases-and-api.md`

## Required Specs And Plan

- `SDD/specs/sddSpec.md`
- `SDD/specs/bookingServiceRatingSpec.md` and all its prerequisites
- `SDD/plans/backendSpecs/bookingServiceRatingBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- current checkout service, DTO, participant notifier, resolvers and transaction tests

## Scope

- Replace new checkout aggregate-rating input with a nested six-criterion rating.
- Add `CheckOutRatingResolver` to `CheckOutParticipantNotifier`.
- Create ratings only for newly completed checkout.
- Make rating failure roll back checkout and participant effects.
- Preserve duplicate-safe repeated/update behavior.
- Remove the obsolete generic rating from Guest and CheckOut domain state,
  DTOs, validation, services, persistence mappings and JPA entities.
- Drop the obsolete `guests.rating` and `check_outs.rating` columns
  idempotently, discarding their legacy values without backfill.
- Remove generic-rating compatibility aliases and tests.
- Add completed/pending/cancelled, rollback and idempotence tests.

## Acceptance Criteria

- Flow is `CheckOutService → CheckOutParticipantNotifier → CheckOutRatingResolver → RatingUseCase`.
- Completed checkout requires all six scores and produces one rating.
- Pending/cancelled checkout produces none.
- Failure is atomic and retry cannot duplicate the rating.
- No generic Guest/CheckOut rating member, contract or database column remains.
- Focused tests, architecture tests, full backend suite and `git diff --check` pass.

## Required Report

`SDD/ImplementationReport/YYYY-MM-DD-025b-integrate-ratings-with-checkout.md`
