# Task 035f DONE — Add Collapsible Guest Rating History

## Status

Completed after explicit user authorization on 2026-08-13.

## Objective

Show an existing guest's booking-derived rating history inline during new
reservation creation and verify the complete ratings experience.

## Dependencies

- `SDD/tasks/frontendSpecs/032f-DONE-build-checkout-rating-stars.md`
- `SDD/tasks/frontendSpecs/034f-DONE-link-rating-table-participants.md`

## Required Specs And Plan

- `SDD/specs/sddSpec.md`
- `SDD/specs/bookingServiceRatingSpec.md` and all its prerequisites
- `SDD/plans/frontendSpecs/bookingServiceRatingFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- current reservation form/lookup/controller, rating API/views, CSS and frontend suites

## Scope

- Retain exact selected guest ID in reservation lookup state.
- Load booking-derived history only after exact guest selection.
- Show the text toggle after exact selection, including for an empty history,
  with correct expanded state and accessible empty feedback.
- Render booking/date/six-score/observation table and booking links.
- Clear stale history when guest changes; ignore obsolete async responses.
- Preserve all reservation fields while opening/hiding history or navigating back.
- Complete cache busting and integrated checkout/sidebar/list/history tests.
- Run manual checkout-to-history smoke flow when local backend is available.

## Acceptance Criteria

- Link is absent without a selected guest and remains present when the selected
  guest has no ratings.
- Toggle opens/closes an accessible table without losing form state.
- Rows belong only to the currently selected guest and link to bookings.
- Stale requests cannot display another guest's ratings.
- Complete frontend suite, integrated flow tests and `git diff --check` pass.
- Smoke result or environment limitation is recorded in the report.

## Required Report

`SDD/ImplementationReport/YYYY-MM-DD-035f-add-collapsible-guest-rating-history.md`
