# Task 034f DONE — Link Rating Table Participants

## Status

Completed and verified on 2026-08-13.

## Objective

Integrate the ratings table with the related guest and booking navigation
without creating an individual rating profile.

## Dependencies

- `SDD/tasks/frontendSpecs/031f-DONE-create-ratings-navigation-and-api.md`
- `SDD/tasks/frontendSpecs/033f-DONE-build-ratings-list-page.md`

## Required Specs And Plan

- `SDD/specs/sddSpec.md`
- `SDD/specs/bookingServiceRatingSpec.md` and all its prerequisites
- `SDD/plans/frontendSpecs/bookingServiceRatingFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- ratings controller/list from prior tasks, guest/booking navigation callbacks
  and navigation tests

## Scope

- Make the guest name in the first column a semantic link to that guest.
- Make the booking reference in the second column a semantic link to that
  booking.
- Preserve the ratings list as the immediate predecessor for both links.
- Keep the row and every remaining cell free of links and click actions.
- Provide clear accessible link names and keyboard focus behavior.
- Add linking, navigation-history and non-clickable-row tests.

## Acceptance Criteria

- Only the first and second columns contain navigation links.
- Guest and booking links open their correct existing profiles.
- Back navigation restores the ratings list and its retained state.
- No individual rating profile, detail view, route or row action exists.
- Focused tests, full frontend suite and `git diff --check` pass.

## Required Report

`SDD/ImplementationReport/YYYY-MM-DD-034f-link-rating-table-participants.md`
