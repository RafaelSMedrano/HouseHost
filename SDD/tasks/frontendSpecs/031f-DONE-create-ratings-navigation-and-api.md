# Task 031f DONE — Create Ratings Navigation And API

## Status

Completed and verified on 2026-08-13.

## Objective

Create the frontend ratings API boundary, controller and accessible sidebar root.

## Dependencies

- `SDD/tasks/backendSpecs/024b-DONE-build-ratings-use-cases-and-api.md`
- `SDD/tasks/backendSpecs/026b-DONE-query-ratings-by-booking-guest.md`

## Required Specs And Plan

- `SDD/specs/sddSpec.md`
- `SDD/specs/bookingServiceRatingSpec.md` and all its prerequisites
- `SDD/plans/frontendSpecs/bookingServiceRatingFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- current API adapter, permissions, sidebar, UI/domain controllers and navigation tests

## Scope

- Add ratings-list and guest-history API helpers.
- Create `ratingController.js` with one lazy ratings-list root entry.
- Add operational `ratings` permission and `Avaliações` sidebar item.
- Register the ratings root action and current-root synchronization.
- Inject guest/booking profile callbacks without importing their views.
- Add permission, API, sidebar and navigation tests.

## Acceptance Criteria

- `Avaliações` starts an independent sidebar root for authorized roles only.
- API identifiers/queries are encoded and observations never enter URLs/logs.
- Guest and booking links preserve the ratings list as their immediate
  navigation predecessor.
- Focused tests, full frontend suite and `git diff --check` pass.

## Required Report

`SDD/ImplementationReport/YYYY-MM-DD-031f-create-ratings-navigation-and-api.md`
