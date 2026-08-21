# Task 033f DONE — Build Ratings List Page

## Status

Completed and verified on 2026-08-13.

## Objective

Build the sidebar ratings table with guest, booking and complete feedback data.

## Dependencies

- `SDD/tasks/frontendSpecs/031f-DONE-create-ratings-navigation-and-api.md`

## Required Specs And Plan

- `SDD/specs/sddSpec.md`
- `SDD/specs/bookingServiceRatingSpec.md` and all its prerequisites
- `SDD/plans/frontendSpecs/bookingServiceRatingFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- rating controller/API from task `031f`, current table patterns, CSS and view tests

## Scope

- Create `ratingsView.js` and reusable read-only star presentation.
- Render guest, booking, date, six criteria and observation columns in that
  order.
- Link only guest names in the first column and booking references in the
  second column through controller callbacks.
- Keep each rating row and every remaining cell non-clickable.
- Add bounded pagination and newest-first presentation.
- Implement loading, empty, error, overflow and responsive behavior.
- Escape every backend-provided label/observation.
- Add DOM, accessibility, linking and responsive tests.

## Acceptance Criteria

- Table contains all required columns and real semantic headings.
- Every score includes stars and accessible `n de 5` text.
- Guest and booking links preserve the ratings list as navigation predecessor.
- No row selection, rating-detail action or rating-profile link exists.
- Narrow layouts remain usable without clipping data.
- Focused tests, full frontend suite and `git diff --check` pass.

## Required Report

`SDD/ImplementationReport/YYYY-MM-DD-033f-build-ratings-list-page.md`
