# Task 010f — Register Administrative Navigation Routes

## Status

Completed on 9 August 2026 after implementation, automated verification and
prerequisite review under the former route-registry design. Superseded by
task `016f` for the lazy page-entry design; retained as historical record.

## Implementation Area

Frontend (`f`).

## Objective

Connect the navigation-history core to the administrative shell and define one
route renderer for every sidebar module and supported detail/form destination.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`

## Required Plans

- `SDD/plans/frontendSpecs/administrativeNavigationHistoryFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `frontend/admin/js/controllers/UICOntroller.js`
- `frontend/admin/js/controllers/navigationController.js`

## Dependencies

- Task `009f` completed and reviewed.

## Scope

Update `UICOntroller.js` to:

- preserve the existing `create...Controller` factory pattern for every
  separated module;
- preserve the existing controller instances and their injected dependencies;
- replace the current direct-delegation `navigate` facade implementation with
  calls to the centralized navigation controller;
- construct the route table after domain controllers are available;
- create the navigation controller once;
- expose route calls to sidebar and topbar wiring;
- register the complete route set from the frontend plan;
- render the initial dashboard through `reset`;
- keep shell, topbar and sidebar responsibilities in the UI controller;
- remove route-specific back-destination logic from the UI controller;
- update cache-busting query versions for changed controller imports.

The task must not move module rendering functions back into `UICOntroller` or
create a second set of module controllers. It must preserve the existing
factory/controller pattern while ensuring that a route is not executed before
its controller dependency exists.

## Acceptance Criteria

- Every accessible sidebar module has one registered root route.
- Every supported profile/form route has its required parameter contract.
- The initial render creates a single dashboard history entry.
- Sidebar integration is prepared to call `reset` for primary modules.
- Topbar actions use route navigation rather than direct rendering.
- `UICOntroller` does not contain fixed returns such as `onBack` to a specific
  unrelated module list.
- An unknown route has a controlled fallback.
- Existing shell and permission behavior remains unchanged.

## Expected Files

```text
frontend/admin/js/controllers/UICOntroller.js
frontend/admin/js/controllers/main.js
frontend/admin/tests/navigationRoutes.test.mjs
```

## Required Report

Create `SDD/ImplementationReport/YYYY-MM-DD-010f-register-navigation-routes.md`
after implementation and verification.
