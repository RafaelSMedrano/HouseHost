# Task 024b DONE — Build Ratings Use Cases And API

## Status

Completed and verified on 2026-08-12.

## Objective

Expose validated ratings creation and paginated list/history contracts.

## Dependencies

- `SDD/tasks/backendSpecs/023b-DONE-create-ratings-domain-and-persistence.md`

## Required Specs And Plan

- `SDD/specs/sddSpec.md`
- `SDD/specs/bookingServiceRatingSpec.md` and all its prerequisites
- `SDD/plans/backendSpecs/bookingServiceRatingBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- ratings domain/persistence delivered by task `023b` and current REST/security patterns

## Scope

- Create request, response and paginated summary DTOs.
- Create `RatingUseCase`, `RatingService` and `RatingValidationService`.
- Add REST endpoints for ratings list and guest history plus the application
  operation used by completed checkout.
- Validate booking existence, completed checkout eligibility and uniqueness.
- Normalize observations without truncating them.
- Add service/controller validation and error-semantic tests.

## Acceptance Criteria

- Contracts contain the exact six scores, observation and booking context.
- Duplicate or ineligible ratings are rejected with stable business errors.
- Ordinary rating update and deletion endpoints do not exist.
- No endpoint or use case exists solely to open an individual rating detail.
- Controllers depend on the use case and contain no business logic.
- Pagination is bounded and deterministic.
- Focused tests, full backend suite and `git diff --check` pass.

## Required Report

`SDD/ImplementationReport/YYYY-MM-DD-024b-build-ratings-use-cases-and-api.md`
