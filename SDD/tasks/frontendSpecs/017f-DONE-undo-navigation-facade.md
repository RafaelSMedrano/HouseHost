# Task 017f DONE — Undo The Navigation Facade

## Status

Completed on 10 August 2026 after implementation, automated verification and
prerequisite review.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`

## Required Plans

- `SDD/plans/frontendSpecs/administrativeNavigationHistoryFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `frontend/admin/js/controllers/UICOntroller.js`
- `frontend/admin/js/controllers/navigationController.js`
- domain controller factories receiving the navigation dependency

## Dependencies

- Task `016f` completed and reviewed.

## Scope Executed

Removed `createAdministrativeNavigationFacade` and the page-by-page facade
object from `UICOntroller`. The dynamic navigation controller is now created by
the composition root and injected directly into the domain controllers.

Domain controllers create entries themselves and call the navigation contract:

```js
navigation.goTo({
    name: "guestProfile",
    params: { guestId },
    render: () => renderGuestProfilePanel(guestId),
});
```

Primary sidebar callbacks remain simple composition-root callbacks that call
`navigation.reset`; no complete route registry or profile-page registry was
created there. Existing views continue to receive semantic callbacks and do
not import or inspect the navigation controller.

The obsolete `topbarContext` return-callback state was removed because topbar
actions now open domain entries directly and back behavior belongs to the
navigation history.

## Acceptance Criteria Review

- `createAdministrativeNavigationFacade` no longer exists in active code:
  passed.
- Domain controller factories receive navigation directly: passed.
- Domain controllers submit dynamic entries themselves: passed.
- No complete route registry or page-by-page facade remains in
  `UICOntroller`: passed.
- Views still receive semantic callbacks only: passed.
- Sidebar primary navigation continues to use `reset`: passed.
- Dynamic navigation core and shell wiring remain testable: passed.

## Files Created

- `frontend/admin/tests/directNavigationInjection.test.mjs`

## Files Changed

- `frontend/admin/js/controllers/UICOntroller.js`
- `frontend/admin/js/controllers/dashboardController.js`
- `frontend/admin/js/controllers/guestController.js`
- `frontend/admin/js/controllers/reservationController.js`
- `frontend/admin/js/controllers/operationsController.js`
- `frontend/admin/js/controllers/financeController.js`
- `frontend/admin/js/controllers/roomController.js`
- `frontend/admin/js/controllers/supplierController.js`
- `frontend/admin/js/controllers/privacyController.js`
- `frontend/admin/js/controllers/userController.js`
- `frontend/admin/tests/directNavigationInjection.test.mjs`
- `frontend/admin/tests/navigationFacade.test.mjs` was removed as obsolete.

## Tests And Verification

```text
node --test frontend/admin/tests/navigationController.test.mjs frontend/admin/tests/directNavigationInjection.test.mjs
node --test frontend/admin/tests/*.mjs
node --check frontend/admin/js/controllers/UICOntroller.js
node --check frontend/admin/js/controllers/*Controller.js
git diff --check
```

Results:

- Direct-injection and navigation-core tests: 10 passed, 0 failed.
- Full frontend test suite: 31 passed, 0 failed.
- All controller syntax checks: passed.
- Git whitespace check: passed.

Unavailable commands: None.

## Technical And MVP Decisions

- Domain controllers receive the actual navigation controller instead of an
  adapter with one method per page.
- Cross-module relationships receive only their required renderer functions;
  no global controller collection is injected into domain controllers.
- The composition root owns only shell setup, controller construction,
  fallback configuration and simple sidebar root callbacks.
- Cross-module entries retain their renderer closure while the owning
  controller remains responsible for the page composition.

## Difficulties And Resolutions

Removing the facade required updating existing `navigate.*` calls across the
domain controllers. Each was converted to a direct `navigation.goTo`,
`navigation.reset`, `navigation.replace` or `navigation.back` call. The
facade-specific test was replaced with direct-injection coverage.

## Prerequisite Review

- Governing SDD, frontend spec and frontend plan were read.
- The implementation follows the direct-injection architecture defined after
  the facade was rejected.
- No application-wide facade, complete route registry or second history store
  remains in active code.
- Views remain independent from the navigation controller.
- No backend, permission, storage or browser-history behavior changed.
- All acceptance criteria passed and the task was renamed with `DONE`.

Final status: complete.
