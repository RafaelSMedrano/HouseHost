# Task 032f DONE — Build Checkout Rating Stars

## Status

Completed and verified on 2026-08-13.

## Objective

Replace checkout's legacy overall rating with six required five-star controls
and optional observations.

## Dependencies

- `SDD/tasks/backendSpecs/025b-DONE-integrate-ratings-with-checkout.md`
- `SDD/tasks/frontendSpecs/031f-DONE-create-ratings-navigation-and-api.md`

## Required Specs And Plan

- `SDD/specs/sddSpec.md`
- `SDD/specs/bookingServiceRatingSpec.md` and all its prerequisites
- `SDD/plans/frontendSpecs/bookingServiceRatingFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- current checkout form/controller/API, CSS and checkout tests

## Scope

- Render six empty five-star groups only in completed checkout.
- Fill every star through the selected position and store scores `1..5`.
- Add optional observation textarea with 4,000-character limit.
- Send the exact nested rating payload.
- Remove the generic guest/checkout rating control, preview, state, payload
  member, formatter and tests.
- Validate/focus unanswered groups and preserve values after failed requests.
- Keep duplicate-submit protection and accessible live feedback.
- Add interaction, keyboard, payload, failure and responsive tests.

## Acceptance Criteria

- Every group is named, keyboard operable and exposes `n de 5`.
- Completed checkout cannot submit with an unanswered criterion.
- Selecting star four produces score `4` and four filled stars.
- Failure preserves all scores and observations.
- No generic rating UI, state or payload member remains.
- Focused tests, full frontend suite and `git diff --check` pass.

## Required Report

`SDD/ImplementationReport/YYYY-MM-DD-032f-build-checkout-rating-stars.md`
