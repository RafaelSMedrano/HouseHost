# Task 013f — Integrate Sidebar Topbar And Accessibility

## Status

Superseded by task `019f`. Not authorized for implementation; retained as the
former route-registry task for historical traceability.

## Implementation Area

Frontend (`f`).

## Objective

Finish application-wide integration so primary navigation starts clean flows,
topbar actions preserve return behavior and restored pages remain accessible.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
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
- `frontend/admin/js/views/*` files whose back controls are changed

## Dependencies

- Tasks `011f` and `012f` completed and reviewed.

## Scope

Update sidebar behavior so every primary module calls `reset`. Update user
profile navigation and topbar actions for new reservation, new guest, check-in
and check-out to use route navigation while preserving the current screen as
the return point.

Review all back controls for:

- keyboard operation;
- accessible name;
- visible focus;
- predictable focus restoration after `back`;
- explicit loading and error behavior when a previous route is restored.

Update cache-busting versions for changed modules. Do not change permission
semantics or add browser persistence.

## Acceptance Criteria

- Selecting every sidebar primary item clears the unrelated prior flow.
- Opening a profile after a sidebar reset returns to that module's list.
- Topbar-created forms return to the screen that launched them.
- User profile has a safe dashboard fallback.
- Back buttons are keyboard operable and visibly named.
- Restored pages expose loading/error states and predictable focus.
- No route state is written to `localStorage`, `sessionStorage` or console.
- Changed imports receive compatible cache-busting versions.

## Expected Files

```text
frontend/admin/js/controllers/sidebarController.js
frontend/admin/js/controllers/userController.js
frontend/admin/js/controllers/UICOntroller.js
frontend/admin/js/widgets/dashboardTopbarWidget.js
frontend/admin/js/views/* relevant back-control files
```

## Required Report

Create `SDD/ImplementationReport/YYYY-MM-DD-013f-integrate-sidebar-topbar-and-accessibility.md`
after implementation and verification.
