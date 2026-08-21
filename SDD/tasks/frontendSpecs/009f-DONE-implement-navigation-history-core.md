# Task 009f DONE — Implement Navigation History Core

## Status

Completed on 9 August 2026 after implementation, automated verification and
prerequisite review under the former route-registry design. Superseded by
task `015f` for the dynamic page-entry design; retained as historical record.

## Implementation Area

Frontend (`f`).

## Objective

Create the in-memory navigation-history module and its isolated stack contract,
without migrating the application routes yet.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`

## Required Plans

- `SDD/plans/frontendSpecs/administrativeNavigationHistoryFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`

## Dependencies

None beyond the required SDD documents.

## Scope

Create `frontend/admin/js/controllers/navigationController.js` with:

- private navigation stack;
- route registry supplied at construction;
- `navigate`, `back`, `replace`, `reset`, `current` and `canGoBack`;
- route-name validation;
- required-parameter validation delegated to route metadata or renderer
  contracts;
- defensive copies of route parameters and returned current state;
- safe root fallback;
- controlled errors for missing renderers and malformed entries;
- no DOM access and no dependency on a specific domain controller.

The module must not migrate `UICOntroller` or views in this task.

## Acceptance Criteria

- A new route is appended and rendered exactly once.
- `back` removes the current entry and renders its predecessor.
- `replace` does not increase stack depth.
- `reset` leaves exactly one root entry.
- `current` cannot mutate the internal stack through its returned object.
- `back` at the root uses the configured fallback without throwing.
- Unknown route names are rejected without corrupting the previous stack.
- The module contains no direct import of domain controllers or views.
- Unit tests cover normal, root, malformed-route and parameter-preservation cases.

## Expected Files

```text
frontend/admin/js/controllers/navigationController.js
frontend/admin/tests/navigationController.test.mjs
```

## Required Report

Create `SDD/ImplementationReport/YYYY-MM-DD-009f-navigation-history-core.md`
after implementation and verification.
