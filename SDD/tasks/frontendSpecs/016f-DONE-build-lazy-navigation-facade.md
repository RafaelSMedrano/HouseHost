# Task 016f DONE — Build The Lazy Navigation Facade

## Status

Completed on 10 August 2026 after implementation, automated verification and
prerequisite review under the then-current plan. The facade design was later
identified as an architectural error and is superseded by task `017f`; this
file remains a historical completion record.

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
- `frontend/admin/tests/navigationFacade.test.mjs`

## Dependencies

- Task `015f` completed and reviewed.

## Scope Executed

Updated `UICOntroller` to construct navigation entries lazily through
`createAdministrativeNavigationFacade`. The facade creates entries only when a
navigation method is called and delegates them to `goTo` or `reset`:

```js
{
    name: "transactionProfile",
    params: { transactionId },
    render: () => financeController.renderTransactionProfilePanel(
        transactionId,
        back,
    )
}
```

Primary pages use `reset`; profile and form pages use `goTo`. The composition
root creates the separated domain controllers once, provides the dynamic
facade to them and supplies the dashboard as `fallbackPage` to the navigation
core.

The complete startup route registry and `createAdministrativeRoutes` export
were removed from the active implementation. The former route-registry test
was replaced by facade tests.

## Acceptance Criteria Review

- Controllers are still created once by `UICOntroller`: passed.
- No complete route registry is required during startup: passed.
- Facade methods create valid lazy page entries: passed.
- Dashboard and sidebar roots use `reset`: passed.
- Detail/form actions use `goTo` or explicit `replace`: passed for the facade
  scope.
- Sidebar, topbar and main-panel shell wiring remains present: passed by syntax
  and full test verification.

## Files Created

- `frontend/admin/tests/navigationFacade.test.mjs`

## Files Changed

- `frontend/admin/js/controllers/UICOntroller.js`
- `frontend/admin/tests/navigationFacade.test.mjs`
- `frontend/admin/tests/navigationRoutes.test.mjs` was replaced by the facade
  test file.

## Tests And Verification

```text
node --test frontend/admin/tests/navigationFacade.test.mjs frontend/admin/tests/navigationController.test.mjs
node --test frontend/admin/tests/*.mjs
node --check frontend/admin/js/controllers/UICOntroller.js
git diff --check
```

Results:

- Navigation core and facade tests: 11 passed, 0 failed.
- Full frontend test suite: 32 passed, 0 failed.
- JavaScript syntax check: passed.
- Git whitespace check: passed.

Unavailable commands: None.

## Technical And MVP Decisions

- The facade uses a mutable controller reference object so factories can be
  created once before all controller properties are assigned; entries are only
  rendered after initialization is complete.
- `navigationBridge` exposes only navigation operations to the facade and
  keeps the navigation controller implementation hidden from domain
  controllers.
- The dashboard is supplied as the fallback entry because the core no longer
  assumes an administrative route name.
- Legacy callback arguments accepted by existing controller methods are
  ignored by facade methods where the centralized history now determines back.
The facade reversal belongs to task `017f`; full controller callback migration
continues in tasks `018f` and `019f`.

## Difficulties And Resolutions

The existing `UICOntroller` used a complete route descriptor table and the old
`navigate(name, params)` API. It was replaced with one facade that constructs
entries on demand while preserving the existing controller factory pattern.
The domain controllers themselves were not broadly refactored because that is
outside this task's scope.

## Prerequisite Review

- Governing SDD, frontend spec and frontend plan were read.
- The implementation uses the ordered dynamic-entry model from task `015f`.
- No second history store or complete route registry was introduced.
- Existing separated controllers and dependency injection were preserved.
- No backend, permission, storage or browser-history behavior changed.
- All acceptance criteria of the former facade task passed and the task was
  renamed with `DONE`; the final architecture is defined by task `017f`.

Final status: complete.
