# Task 020f DONE — Integrate Sidebar, Topbar And Accessibility

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
- `frontend/admin/js/controllers/sidebarController.js`
- `frontend/admin/js/controllers/userController.js`
- `frontend/admin/js/controllers/UICOntroller.js`
- `frontend/admin/js/widgets/dashboardTopbarWidget.js`
- relevant views containing back controls

## Dependencies

- Tasks `018f` and `019f` completed and reviewed.

## Scope

Make every sidebar primary action create a root entry through `reset`. Make
topbar actions call the injected navigation controller while preserving the
current page. Review back controls for keyboard operation, accessible naming,
focus and loading/error behavior after restoring a previous entry. Preserve
permissions and avoid browser persistence.

## Acceptance Criteria

- Sidebar selection clears unrelated history.
- Topbar forms return to their launching page.
- User profile has a safe root fallback.
- Back controls are keyboard-operable and visibly named.
- Restored pages expose predictable loading/error states and focus.
- No navigation state is written to storage or console.
- No facade or duplicate navigation adapter is reintroduced.

## Expected Files

```text
frontend/admin/js/controllers/sidebarController.js
frontend/admin/js/controllers/userController.js
frontend/admin/js/controllers/UICOntroller.js
frontend/admin/js/widgets/dashboardTopbarWidget.js
frontend/admin/js/views/* relevant back-control files
```

## Required Report

Create `SDD/ImplementationReport/YYYY-MM-DD-020f-integrate-sidebar-topbar-accessibility.md`.
