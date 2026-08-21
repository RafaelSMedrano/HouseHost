# Task 019f DONE — Migrate Operational And Governance Navigation

## Status

Complete.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`

## Required Plans

- `SDD/plans/frontendSpecs/administrativeNavigationHistoryFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- operational and governance controller factories
- related operational and governance views

## Dependencies

- Task `018f` completed and reviewed.

## Scope

Apply direct navigation-controller injection to rooms, operations, suppliers
and privacy governance without introducing a second origin-tracking system.
Controllers create entries for their own pages, preserve identifiers and
relationship context, and pass semantic `onBack` callbacks to views.

Forms use `back` on cancellation and choose `replace` versus `goTo` explicitly
after successful saves.

## Acceptance Criteria

- Operational forms return to their initiating entries.
- Supplier profile/edit flows preserve the predecessor.
- Operation -> assessment returns to operation.
- Assessment -> operation preserves the assessment predecessor.
- Permission failures do not add rejected entries.
- Existing governance lifecycle behavior remains unchanged.
- No operational or governance controller uses the removed facade.

## Expected Files

```text
frontend/admin/js/controllers/roomController.js
frontend/admin/js/controllers/operationsController.js
frontend/admin/js/controllers/supplierController.js
frontend/admin/js/controllers/privacyController.js
frontend/admin/js/views/roomFormView.js
frontend/admin/js/views/supplierFormView.js
frontend/admin/js/views/supplierProfileView.js
frontend/admin/js/views/legalBasisAssessmentProfileView.js
frontend/admin/tests/operationalNavigationFlows.test.mjs
```

## Required Report

Create `SDD/ImplementationReport/YYYY-MM-DD-019f-migrate-operational-governance-navigation.md`.
